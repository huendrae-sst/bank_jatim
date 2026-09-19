package com.bankjatim.jims.service;

import com.bankjatim.jims.domain.*;
import com.bankjatim.jims.dto.EmbossFileResponse;
import com.bankjatim.jims.dto.EmbossRecordResponse;
import com.bankjatim.jims.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class EmbossService {

    private final EmbossFileRepository embossFileRepository;
    private final EmbossRecordRepository embossRecordRepository;
    private final ItemRepository itemRepository;
    private final OrganizationRepository organizationRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final JdbcTemplate jdbcTemplate;

    private record EmbossRecordRow(
            String externalReferenceId,
            String productCode,
            String accountNumber,
            String customerName,
            String cardNumberMasked,
            String cardType,
            String branchCode,
            Long itemId,
            Long pinEnvelopeItemId,
            String status,
            String rejectionReason
    ) {}

    @Transactional(readOnly = true)
    public List<EmbossFileResponse> getAllEmbossFiles() {
        return embossFileRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(EmbossFileResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public EmbossFileResponse getEmbossFile(Long id) {
        EmbossFile file = embossFileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Berkas Emboss tidak ditemukan: " + id));
        return EmbossFileResponse.from(file);
    }

    @Transactional(readOnly = true)
    public List<EmbossRecordResponse> getEmbossRecords(Long fileId, String status) {
        String normalizedStatus = status != null && !status.isBlank() && !"ALL".equals(status) ? status : null;
        return embossRecordRepository.findByEmbossFileIdWithDetails(fileId, normalizedStatus).stream()
                .map(EmbossRecordResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EmbossRecordResponse> getRejectedRecords() {
        return embossRecordRepository.findRejectedWithDetails().stream()
                .map(EmbossRecordResponse::from)
                .toList();
    }

    @Transactional
    private Item getGpnItem() {
        return itemRepository.findBySku("ATM-INST-001")
                .or(() -> itemRepository.findBySku("SKU-BLANK-GPN"))
                .or(() -> itemRepository.findAll().stream().filter(i -> i.getName() != null && i.getName().toLowerCase().contains("gpn")).findFirst())
                .orElseGet(() -> itemRepository.findAll().isEmpty() ? null : itemRepository.findAll().get(0));
    }

    private Item getMcItem() {
        return itemRepository.findBySku("ATM-NAME-001")
                .or(() -> itemRepository.findBySku("SKU-BLANK-MC"))
                .or(() -> itemRepository.findAll().stream().filter(i -> i.getName() != null && (i.getName().toLowerCase().contains("mastercard") || i.getName().toLowerCase().contains("mc"))).findFirst())
                .orElseGet(this::getGpnItem);
    }

    private Item getPinItem() {
        return itemRepository.findBySku("SKU-PIN-ENV")
                .or(() -> itemRepository.findAll().stream().filter(i -> i.getName() != null && i.getName().toLowerCase().contains("pin")).findFirst())
                .orElse(null);
    }

    @Transactional
    public EmbossFileResponse processEmbossData(String filename, List<String[]> csvRows, User uploader) {
        String generatedFileId = "EB-" + java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd").format(java.time.LocalDate.now()) + "-" + String.format("%05d", (System.currentTimeMillis() % 100000));
        String generatedHash = java.util.UUID.randomUUID().toString().replace("-", "");
        EmbossFile file = EmbossFile.builder()
                .fileId(generatedFileId)
                .fileHash(generatedHash)
                .filename(filename)
                .uploadedByUser(uploader != null ? uploader : userRepository.findAll().get(0))
                .status("PARSED")
                .totalRecords(csvRows.size())
                .validRecords(0)
                .rejectedRecords(0)
                .build();

        EmbossFile savedFile = embossFileRepository.save(file);

        Item gpnItem = getGpnItem();
        Item mcItem = getMcItem();
        Item pinItem = getPinItem();

        int validCount = 0;
        int rejectCount = 0;
        List<EmbossRecordRow> rowsToInsert = new ArrayList<>(csvRows.size());

        for (String[] row : csvRows) {
            // Check for empty row or EOF
            if (row == null || row.length == 0 || (row.length == 1 && (row[0] == null || row[0].trim().isEmpty()))) {
                continue;
            }

            // Skip header row if present
            if (row[0] != null && (row[0].toLowerCase().contains("rekening") || row[0].toLowerCase().contains("account"))) {
                continue;
            }

            // Format: [accountNumber, customerName, cardNumberMasked, cardType, branchCode]
            if (row.length < 5) {
                rejectCount++;
                continue;
            }

            String cardType = row[3] != null ? row[3].toUpperCase().trim() : "GPN";
            Item selectedItem = cardType.contains("MASTERCARD") || cardType.contains("MC") ? mcItem : gpnItem;

            String customerName = row[1] != null ? row[1].trim() : "";
            boolean isRejected = customerName.length() > 26 || customerName.isEmpty();
            String recordStatus = isRejected ? "REJECTED" : "VALID";
            String reason = isRejected ? (customerName.isEmpty() ? "Nama nasabah kosong" : "Karakter nama melebihi batas emboss (max 26 karakter)") : null;

            int currentIdx = validCount + rejectCount + 1;
            String branchCode = row[4] != null && !row[4].trim().isEmpty() ? row[4].trim() : "KC-SBY";

            rowsToInsert.add(new EmbossRecordRow(
                    "EXT-" + savedFile.getId() + "-" + currentIdx,
                    cardType,
                    row[0] != null ? row[0].trim() : "",
                    customerName,
                    row[2] != null ? row[2].trim() : "",
                    cardType,
                    branchCode,
                    selectedItem != null ? selectedItem.getId() : null,
                    pinItem != null ? pinItem.getId() : null,
                    recordStatus,
                    reason
            ));

            if (isRejected) {
                rejectCount++;
            } else {
                validCount++;
            }
        }

        if (!rowsToInsert.isEmpty()) {
            String insertSql = "INSERT INTO emboss_records (" +
                    "emboss_file_id, external_reference_id, product_code, account_number, " +
                    "customer_name, card_number_masked, card_type, branch_code, " +
                    "item_id, pin_envelope_item_id, status, rejection_reason, created_at, updated_at" +
                    ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";

            int batchSize = 1000;
            for (int i = 0; i < rowsToInsert.size(); i += batchSize) {
                List<EmbossRecordRow> batch = rowsToInsert.subList(i, Math.min(i + batchSize, rowsToInsert.size()));
                jdbcTemplate.batchUpdate(insertSql, new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int idx) throws SQLException {
                        EmbossRecordRow r = batch.get(idx);
                        ps.setLong(1, savedFile.getId());
                        ps.setString(2, r.externalReferenceId());
                        ps.setString(3, r.productCode());
                        ps.setString(4, r.accountNumber());
                        ps.setString(5, r.customerName());
                        ps.setString(6, r.cardNumberMasked());
                        ps.setString(7, r.cardType());
                        ps.setString(8, r.branchCode());
                        if (r.itemId() != null) {
                            ps.setLong(9, r.itemId());
                        } else {
                            ps.setNull(9, Types.BIGINT);
                        }
                        if (r.pinEnvelopeItemId() != null) {
                            ps.setLong(10, r.pinEnvelopeItemId());
                        } else {
                            ps.setNull(10, Types.BIGINT);
                        }
                        ps.setString(11, r.status());
                        ps.setString(12, r.rejectionReason());
                    }

                    @Override
                    public int getBatchSize() {
                        return batch.size();
                    }
                });
            }
        }

        savedFile.setTotalRecords(validCount + rejectCount);
        savedFile.setValidRecords(validCount);
        savedFile.setRejectedRecords(rejectCount);
        savedFile.setStatus("VALIDATED");

        return EmbossFileResponse.from(embossFileRepository.save(savedFile));
    }

    @Transactional
    public EmbossRecordResponse reprocessRecord(Long recordId, String customerName) {
        EmbossRecord record = embossRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Rekaman tidak ditemukan: " + recordId));
        if (customerName != null && !customerName.isBlank()) {
            record.setCustomerName(customerName.trim());
        }
        record.setStatus("VALID");
        record.setRejectionReason(null);
        EmbossRecord saved = embossRecordRepository.save(record);

        EmbossFile file = saved.getEmbossFile();
        if (file != null) {
            Integer valid = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM emboss_records WHERE emboss_file_id = ? AND status = 'VALID'",
                    Integer.class, file.getId()
            );
            Integer rejected = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM emboss_records WHERE emboss_file_id = ? AND status = 'REJECTED'",
                    Integer.class, file.getId()
            );
            file.setValidRecords(valid != null ? valid : 0);
            file.setRejectedRecords(rejected != null ? rejected : 0);
            embossFileRepository.save(file);
        }
        return EmbossRecordResponse.from(saved);
    }

    @Transactional
    public void generateOrdersFromEmboss(Long fileId, User user) {
        EmbossFile file = embossFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("Berkas Emboss tidak ditemukan: " + fileId));

        String statsSql = "SELECT branch_code, card_type, COUNT(*) as count " +
                "FROM emboss_records " +
                "WHERE emboss_file_id = ? AND status = 'VALID' " +
                "GROUP BY branch_code, card_type";

        List<Map<String, Object>> branchStats = jdbcTemplate.queryForList(statsSql, fileId);
        if (branchStats.isEmpty()) {
            throw new RuntimeException("Tidak ada rekaman valid pada berkas ini untuk dijadikan order");
        }

        if (user == null) {
            user = userRepository.findAll().stream().findFirst().orElse(null);
        }

        Organization defaultBranch = organizationRepository.findByCode("KC_SBY")
                .or(() -> organizationRepository.findByCode("KC-SBY"))
                .orElseGet(() -> organizationRepository.findAll().stream().findFirst().orElse(null));

        Item gpnItem = getGpnItem();
        Item mcItem = getMcItem();
        Item pinItem = getPinItem();

        Map<String, Map<String, Long>> branchCardCounts = new LinkedHashMap<>();
        for (Map<String, Object> row : branchStats) {
            String bCode = row.get("branch_code") != null ? ((String) row.get("branch_code")).trim() : defaultBranch.getCode();
            String cType = row.get("card_type") != null ? ((String) row.get("card_type")).trim().toUpperCase() : "GPN";
            long cnt = ((Number) row.get("count")).longValue();
            branchCardCounts.computeIfAbsent(bCode, k -> new HashMap<>()).put(cType, cnt);
        }

        for (Map.Entry<String, Map<String, Long>> entry : branchCardCounts.entrySet()) {
            String branchCode = entry.getKey();
            Map<String, Long> cardMap = entry.getValue();

            long gpnCount = 0;
            long mcCount = 0;
            for (Map.Entry<String, Long> ce : cardMap.entrySet()) {
                if (ce.getKey().contains("MASTERCARD") || ce.getKey().contains("MC")) {
                    mcCount += ce.getValue();
                } else {
                    gpnCount += ce.getValue();
                }
            }
            long totalBranchRecords = gpnCount + mcCount;

            Organization branchOrg = organizationRepository.findByCode(branchCode)
                    .or(() -> organizationRepository.findByCode(branchCode.replace('_', '-')))
                    .or(() -> organizationRepository.findByCode(branchCode.replace('-', '_')))
                    .orElse(defaultBranch);

            Order order = Order.builder()
                    .orderNumber("ORD-EMBOSS-" + branchOrg.getCode() + "-" + System.currentTimeMillis())
                    .orderType("EMBOSS_ORDER")
                    .fulfillmentStatus("UNFULFILLED")
                    .embossFile(file)
                    .requestingOrganization(branchOrg)
                    .requestingWarehouse(user != null ? user.getWarehouse() : null)
                    .createdByUser(user)
                    .approvedByUser(null)
                    .priority("HIGH")
                    .requiredDate(LocalDate.now().plusDays(1))
                    .status("DRAFT") // Status awal DRAFT, disetujui di persetujuan order
                    .submittedAt(java.time.LocalDateTime.now())
                    .approvedAt(null)
                    .notes("Order Personalisasi Kartu dari Berkas Core Banking: " + file.getFilename())
                    .totalItems((int) totalBranchRecords)
                    .totalEstimatedValue(new BigDecimal("25000.00").multiply(BigDecimal.valueOf(totalBranchRecords)))
                    .build();

            if (gpnCount > 0 && gpnItem != null) {
                BigDecimal unitPrice = gpnItem.getEstimatedUnitPrice() != null ? gpnItem.getEstimatedUnitPrice() : new BigDecimal("20000.00");
                OrderItem oiGpn = OrderItem.builder()
                        .order(order)
                        .item(gpnItem)
                        .qtyRequested((int) gpnCount)
                        .qtyApproved((int) gpnCount)
                        .qtyAllocated((int) gpnCount)
                        .unitPriceRef(unitPrice)
                        .subtotalRef(unitPrice.multiply(BigDecimal.valueOf(gpnCount)))
                        .notes("Kartu Debit GPN Emboss Nasabah")
                        .build();
                order.getItems().add(oiGpn);
            }

            if (mcCount > 0 && mcItem != null) {
                BigDecimal unitPrice = mcItem.getEstimatedUnitPrice() != null ? mcItem.getEstimatedUnitPrice() : new BigDecimal("25000.00");
                OrderItem oiMc = OrderItem.builder()
                        .order(order)
                        .item(mcItem)
                        .qtyRequested((int) mcCount)
                        .qtyApproved((int) mcCount)
                        .qtyAllocated((int) mcCount)
                        .unitPriceRef(unitPrice)
                        .subtotalRef(unitPrice.multiply(BigDecimal.valueOf(mcCount)))
                        .notes("Kartu Debit Mastercard Emboss Nasabah")
                        .build();
                order.getItems().add(oiMc);
            }

            if (pinItem != null) {
                BigDecimal unitPrice = pinItem.getEstimatedUnitPrice() != null ? pinItem.getEstimatedUnitPrice() : new BigDecimal("2500.00");
                OrderItem oiPin = OrderItem.builder()
                        .order(order)
                        .item(pinItem)
                        .qtyRequested((int) totalBranchRecords)
                        .qtyApproved((int) totalBranchRecords)
                        .qtyAllocated((int) totalBranchRecords)
                        .unitPriceRef(unitPrice)
                        .subtotalRef(unitPrice.multiply(BigDecimal.valueOf(totalBranchRecords)))
                        .notes("Amplop PIN Mailer Keamanan Nasabah")
                        .build();
                order.getItems().add(oiPin);
            }

            Order savedOrder = orderRepository.save(order);

            // Update emboss_records order_id in bulk
            jdbcTemplate.update(
                    "UPDATE emboss_records SET order_id = ?, updated_at = CURRENT_TIMESTAMP " +
                    "WHERE emboss_file_id = ? AND (branch_code = ? OR COALESCE(branch_code, '') = ?) AND status = 'VALID'",
                    savedOrder.getId(), fileId, branchCode, branchCode
            );
        }

        file.setStatus("ORDERS_GENERATED");
        file.setFulfillmentStatus("UNFULFILLED");
        embossFileRepository.save(file);
    }

    @Transactional(readOnly = true)
    public List<com.bankjatim.jims.dto.OrderResponse> getEmbossOrders() {
        return orderRepository.findEmbossOrdersWithDetails().stream()
                .map(com.bankjatim.jims.dto.OrderResponse::from)
                .toList();
    }
}
