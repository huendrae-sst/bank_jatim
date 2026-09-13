package com.bankjatim.jims.service;

import com.bankjatim.jims.domain.*;
import com.bankjatim.jims.dto.EmbossFileResponse;
import com.bankjatim.jims.dto.EmbossRecordResponse;
import com.bankjatim.jims.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmbossService {

    private final EmbossFileRepository embossFileRepository;
    private final EmbossRecordRepository embossRecordRepository;
    private final ItemRepository itemRepository;
    private final OrganizationRepository organizationRepository;
    private final OrderRepository orderRepository;

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
    public EmbossFileResponse processEmbossData(String filename, List<String[]> csvRows, User uploader) {
        EmbossFile file = EmbossFile.builder()
                .filename(filename)
                .uploadedByUser(uploader)
                .status("PARSED")
                .totalRecords(csvRows.size())
                .validRecords(0)
                .rejectedRecords(0)
                .build();

        EmbossFile savedFile = embossFileRepository.save(file);

        Item gpnItem = itemRepository.findBySku("SKU-BLANK-GPN").orElse(null);
        Item mcItem = itemRepository.findBySku("SKU-BLANK-MC").orElse(null);
        Item pinItem = itemRepository.findBySku("SKU-PIN-ENV").orElse(null);

        int validCount = 0;
        int rejectCount = 0;

        for (String[] row : csvRows) {
            // Format: [accountNumber, customerName, cardNumberMasked, cardType, branchCode]
            if (row.length < 5) {
                rejectCount++;
                continue;
            }

            String cardType = row[3].toUpperCase();
            Item selectedItem = cardType.contains("MASTERCARD") || cardType.contains("MC") ? mcItem : gpnItem;

            String customerName = row[1] != null ? row[1].trim() : "";
            boolean isRejected = customerName.length() > 26 || customerName.isEmpty();
            String recordStatus = isRejected ? "REJECTED" : "VALID";
            String reason = isRejected ? (customerName.isEmpty() ? "Nama nasabah kosong" : "Karakter nama melebihi batas emboss (max 26 karakter)") : null;

            EmbossRecord record = EmbossRecord.builder()
                    .embossFile(savedFile)
                    .accountNumber(row[0])
                    .customerName(customerName)
                    .cardNumberMasked(row[2])
                    .cardType(cardType)
                    .branchCode(row[4])
                    .item(selectedItem)
                    .pinEnvelopeItem(pinItem)
                    .status(recordStatus)
                    .rejectionReason(reason)
                    .build();

            embossRecordRepository.save(record);
            if (isRejected) {
                rejectCount++;
            } else {
                validCount++;
            }
        }

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
            List<EmbossRecord> records = embossRecordRepository.findByEmbossFileId(file.getId());
            long valid = records.stream().filter(r -> "VALID".equalsIgnoreCase(r.getStatus())).count();
            long rejected = records.stream().filter(r -> "REJECTED".equalsIgnoreCase(r.getStatus())).count();
            file.setValidRecords((int) valid);
            file.setRejectedRecords((int) rejected);
            embossFileRepository.save(file);
        }
        return EmbossRecordResponse.from(saved);
    }

    @Transactional
    public void generateOrdersFromEmboss(Long fileId, User user) {
        EmbossFile file = embossFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("Berkas Emboss tidak ditemukan: " + fileId));

        Organization defaultBranch = organizationRepository.findByCode("KC-SBY")
                .orElseGet(() -> organizationRepository.findAll().get(0));

        Order order = Order.builder()
                .orderNumber("ORD-EMBOSS-" + System.currentTimeMillis())
                .requestingOrganization(defaultBranch)
                .createdByUser(user)
                .priority("HIGH")
                .requiredDate(LocalDate.now().plusDays(1))
                .status("SUBMITTED")
                .totalItems(file.getValidRecords())
                .totalEstimatedValue(new BigDecimal("25000.00").multiply(BigDecimal.valueOf(file.getValidRecords())))
                .notes("Generated automatically from Emboss File: " + file.getFilename())
                .build();

        orderRepository.save(order);
        file.setStatus("ORDERS_GENERATED");
        embossFileRepository.save(file);
    }
}
