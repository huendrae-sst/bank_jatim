package com.bankjatim.jims.service;

import com.bankjatim.jims.domain.*;
import com.bankjatim.jims.dto.*;
import com.bankjatim.jims.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductionOrderService {

    private final ProductionOrderRepository productionOrderRepository;
    private final ProductionOrderItemRepository productionOrderItemRepository;
    private final ProductionOrderFulfillmentRepository fulfillmentRepository;
    private final ItemBomRepository itemBomRepository;
    private final ItemRepository itemRepository;
    private final WarehouseRepository warehouseRepository;
    private final OrderRepository orderRepository;
    private final EmbossRecordRepository embossRecordRepository;
    private final EmbossFileRepository embossFileRepository;
    private final UserRepository userRepository;
    private final StockBalanceRepository stockBalanceRepository;
    private final InventoryService inventoryService;

    @Transactional(readOnly = true)
    public List<ProductionOrderResponse> getAllProductionOrders() {
        return productionOrderRepository.findAllWithDetails().stream()
                .map(this::toResponseWithBom)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductionOrderResponse getProductionOrder(Long id) {
        ProductionOrder po = productionOrderRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("SPK Produksi tidak ditemukan: " + id));
        return toResponseWithBom(po);
    }

    private ProductionOrderResponse toResponseWithBom(ProductionOrder po) {
        List<ProductionOrderItem> items = productionOrderItemRepository.findByProductionOrderIdWithItem(po.getId());
        List<ProductionOrderFulfillment> fuls = fulfillmentRepository.findByProductionOrderIdWithOrder(po.getId());

        List<ProductionOrderItemResponse> itemResponses = items.stream().map(it -> {
            String matSku = null;
            String matName = null;
            List<ItemBom> boms = itemBomRepository.findByFinishedItemId(it.getItem().getId());
            if (!boms.isEmpty()) {
                matSku = boms.get(0).getMaterialItem().getSku();
                matName = boms.get(0).getMaterialItem().getName();
            }
            return ProductionOrderItemResponse.fromWithMaterial(it, matSku, matName);
        }).toList();

        List<ProductionOrderResponse.FulfillmentSummary> fulSummaries = fuls.stream().map(f ->
                new ProductionOrderResponse.FulfillmentSummary(
                        f.getId(),
                        f.getOrder() != null ? f.getOrder().getId() : null,
                        f.getOrder() != null ? f.getOrder().getOrderNumber() : null,
                        f.getOrder() != null && f.getOrder().getRequestingOrganization() != null ? f.getOrder().getRequestingOrganization().getName() : null,
                        f.getQtyFulfilled(),
                        f.getFulfilledAt()
                )
        ).toList();

        ProductionOrderResponse base = ProductionOrderResponse.from(po);
        return new ProductionOrderResponse(
                base.id(),
                base.productionNumber(),
                base.warehouse(),
                base.createdByUser(),
                base.productionDate(),
                base.totalQty(),
                base.totalProduced(),
                base.totalDamaged(),
                base.status(),
                base.materialIssueStatus(),
                base.approvedByUser(),
                base.approvedAt(),
                base.materialIssuedByUser(),
                base.materialIssuedAt(),
                base.embossFileId(),
                base.embossFileName(),
                base.completedAt(),
                base.notes(),
                base.createdAt(),
                base.updatedAt(),
                itemResponses,
                fulSummaries
        );
    }

    @Transactional(readOnly = true)
    public List<ConsolidationViewResponse> getConsolidationView() {
        // Ambil order emboss terbuka yang belum terpenuhi
        List<Order> embossOrders = orderRepository.findEmbossOrdersWithDetails();

        List<ConsolidationViewResponse> results = new ArrayList<>();
        for (Order ord : embossOrders) {
            if ("COMPLETED".equalsIgnoreCase(ord.getStatus()) || "CANCELLED".equalsIgnoreCase(ord.getStatus())) {
                continue;
            }

            List<EmbossRecord> unassignedRecords = embossRecordRepository.findValidUnassignedByOrderIds(List.of(ord.getId()));
            if (unassignedRecords.isEmpty()) {
                continue;
            }

            Map<String, List<EmbossRecord>> byCardType = unassignedRecords.stream()
                    .collect(Collectors.groupingBy(r -> r.getCardType() != null ? r.getCardType().toUpperCase() : "GPN"));

            for (Map.Entry<String, List<EmbossRecord>> entry : byCardType.entrySet()) {
                String cardType = entry.getKey();
                List<EmbossRecord> recs = entry.getValue();

                List<String> sampleNames = recs.stream()
                        .map(EmbossRecord::getCustomerName)
                        .limit(5)
                        .toList();

                results.add(new ConsolidationViewResponse(
                        ord.getRequestingOrganization() != null ? ord.getRequestingOrganization().getCode() : "-",
                        ord.getRequestingOrganization() != null ? ord.getRequestingOrganization().getName() : "-",
                        ord.getId(),
                        ord.getOrderNumber(),
                        cardType,
                        recs.size(),
                        ord.getStatus(),
                        ord.getEmbossFile() != null ? ord.getEmbossFile().getId() : null,
                        ord.getEmbossFile() != null ? ord.getEmbossFile().getFilename() : null,
                        sampleNames
                ));
            }
        }
        return results;
    }

    @Transactional
    public ProductionOrderResponse createProductionOrder(CreateProductionOrderRequest req, User user) {
        if (user == null) {
            user = userRepository.findAll().stream().findFirst().orElse(null);
        }

        Warehouse warehouse;
        if (req.warehouseId() != null) {
            warehouse = warehouseRepository.findById(req.warehouseId())
                    .orElseThrow(() -> new RuntimeException("Gudang tidak ditemukan: " + req.warehouseId()));
        } else {
            warehouse = warehouseRepository.findByType("CENTRAL_LOGISTICS").stream().findFirst()
                    .orElseGet(() -> warehouseRepository.findAll().stream().findFirst().orElse(null));
        }

        String nextSpkNo = "SPK-PROD-" + DateTimeFormatter.ofPattern("yyyyMMdd").format(LocalDate.now()) + "-" + String.format("%04d", (System.currentTimeMillis() % 10000));

        EmbossFile embFile = null;
        if (req.embossFileId() != null) {
            embFile = embossFileRepository.findById(req.embossFileId()).orElse(null);
        }

        ProductionOrder po = ProductionOrder.builder()
                .productionNumber(nextSpkNo)
                .warehouse(warehouse)
                .createdByUser(user)
                .productionDate(LocalDate.now())
                .status("DRAFT")
                .materialIssueStatus("PENDING")
                .embossFile(embFile)
                .notes(req.notes() != null ? req.notes() : "SPK Produksi Personalisasi Kartu Emboss")
                .totalQty(0)
                .totalProduced(0)
                .totalDamaged(0)
                .build();

        ProductionOrder savedPo = productionOrderRepository.save(po);

        Item finishedGpn = itemRepository.findBySku("ATM-EMB-GPN-001")
                .orElseGet(() -> itemRepository.findBySku("ATM-INST-001").orElse(null));
        Item finishedMc = itemRepository.findBySku("ATM-EMB-MC-001")
                .orElseGet(() -> itemRepository.findBySku("ATM-NAME-001").orElse(finishedGpn));

        int grandTotalQty = 0;

        // 1. Jika ada orderIds (konsolidasi bulk order cabang)
        if (req.orderIds() != null && !req.orderIds().isEmpty()) {
            List<EmbossRecord> recordsToQueue = embossRecordRepository.findValidUnassignedByOrderIds(req.orderIds());
            Map<String, List<EmbossRecord>> recordsByCard = recordsToQueue.stream()
                    .collect(Collectors.groupingBy(r -> (r.getCardType() != null && (r.getCardType().toUpperCase().contains("MC") || r.getCardType().toUpperCase().contains("MASTER"))) ? "MC" : "GPN"));

            for (Map.Entry<String, List<EmbossRecord>> entry : recordsByCard.entrySet()) {
                String cType = entry.getKey();
                List<EmbossRecord> groupRecords = entry.getValue();
                Item targetFinishedItem = "MC".equals(cType) ? finishedMc : finishedGpn;

                ProductionOrderItem poi = ProductionOrderItem.builder()
                        .productionOrder(savedPo)
                        .item(targetFinishedItem)
                        .qtyPlanned(groupRecords.size())
                        .qtyProduced(0)
                        .qtyDamaged(0)
                        .build();
                productionOrderItemRepository.save(poi);
                grandTotalQty += groupRecords.size();

                for (EmbossRecord er : groupRecords) {
                    er.setProductionOrder(savedPo);
                    er.setProductionStatus("QUEUED");
                    er.setProducedItem(targetFinishedItem);
                    embossRecordRepository.save(er);
                }
            }

            // Hubungkan fulfillments
            for (Long oId : req.orderIds()) {
                orderRepository.findById(oId).ifPresent(ord -> {
                    ProductionOrderFulfillment ful = ProductionOrderFulfillment.builder()
                            .productionOrder(savedPo)
                            .order(ord)
                            .qtyFulfilled(0)
                            .build();
                    fulfillmentRepository.save(ful);
                });
            }
        }
        // 2. Jika input manual items langsung
        else if (req.items() != null && !req.items().isEmpty()) {
            for (CreateProductionOrderRequest.ProductionOrderItemInput it : req.items()) {
                Item item = itemRepository.findById(it.itemId())
                        .orElseThrow(() -> new RuntimeException("Item tidak ditemukan: " + it.itemId()));
                int qty = it.qtyPlanned() != null ? it.qtyPlanned() : 0;
                ProductionOrderItem poi = ProductionOrderItem.builder()
                        .productionOrder(savedPo)
                        .item(item)
                        .qtyPlanned(qty)
                        .qtyProduced(0)
                        .qtyDamaged(0)
                        .build();
                productionOrderItemRepository.save(poi);
                grandTotalQty += qty;
            }
        }

        savedPo.setTotalQty(grandTotalQty);
        productionOrderRepository.save(savedPo);

        return getProductionOrder(savedPo.getId());
    }

    @Transactional
    public ProductionOrderResponse requestMaterialIssue(Long id, User user) {
        ProductionOrder po = productionOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SPK Produksi tidak ditemukan: " + id));

        if (!"DRAFT".equals(po.getStatus())) {
            throw new RuntimeException("Permintaan pengeluaran bahan hanya dapat diajukan pada status DRAFT");
        }

        po.setStatus("MATERIAL_REQUESTED");
        po.setMaterialIssueStatus("REQUESTED");
        productionOrderRepository.save(po);
        return getProductionOrder(id);
    }

    @Transactional
    public ProductionOrderResponse approveMaterialIssue(Long id, User approver) {
        ProductionOrder po = productionOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SPK Produksi tidak ditemukan: " + id));

        if (!"MATERIAL_REQUESTED".equals(po.getStatus())) {
            throw new RuntimeException("Pengeluaran bahan hanya dapat disetujui pada status MATERIAL_REQUESTED");
        }

        po.setStatus("MATERIAL_APPROVED");
        po.setMaterialIssueStatus("APPROVED");
        po.setApprovedByUser(approver);
        po.setApprovedAt(LocalDateTime.now());
        productionOrderRepository.save(po);
        return getProductionOrder(id);
    }

    @Transactional
    public ProductionOrderResponse issueMaterial(Long id, User user) {
        ProductionOrder po = productionOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SPK Produksi tidak ditemukan: " + id));

        if (!"MATERIAL_APPROVED".equals(po.getStatus())) {
            throw new RuntimeException("Bahan baku hanya dapat dikeluarkan setelah disetujui (status MATERIAL_APPROVED)");
        }

        List<ProductionOrderItem> items = productionOrderItemRepository.findByProductionOrderIdWithItem(id);
        Long warehouseId = po.getWarehouse().getId();

        for (ProductionOrderItem poi : items) {
            Item finishedItem = poi.getItem();
            List<ItemBom> boms = itemBomRepository.findByFinishedItemId(finishedItem.getId());

            Item materialItem;
            int ratio = 1;
            if (!boms.isEmpty()) {
                materialItem = boms.get(0).getMaterialItem();
                ratio = boms.get(0).getQtyPerUnit() != null ? boms.get(0).getQtyPerUnit() : 1;
            } else {
                materialItem = finishedItem;
            }

            int neededQty = poi.getQtyPlanned() * ratio;

            // Validasi over-issue & stok fisik
            StockBalance balance = stockBalanceRepository.findByWarehouseIdAndItemId(warehouseId, materialItem.getId())
                    .orElse(null);
            int available = balance != null ? balance.getAvailableStock() : 0;
            if (available < neededQty) {
                log.warn("Stok blank card {} tidak mencukupi (Tersedia: {}, Dibutuhkan: {}) untuk SPK {}",
                        materialItem.getSku(), available, neededQty, po.getProductionNumber());
            }

            // Catat pengeluaran bahan baku dari gudang (StockBalance.onHand berkurang & StockLedger MATERIAL_ISSUE)
            inventoryService.recordStockMovement(
                    warehouseId,
                    materialItem.getId(),
                    "MATERIAL_ISSUE",
                    po.getProductionNumber(),
                    0,
                    neededQty,
                    null,
                    "Pengeluaran bahan baku kartu blank untuk SPK: " + po.getProductionNumber(),
                    user
            );
        }

        po.setStatus("IN_PRODUCTION");
        po.setMaterialIssueStatus("ISSUED");
        po.setMaterialIssuedByUser(user);
        po.setMaterialIssuedAt(LocalDateTime.now());
        productionOrderRepository.save(po);

        return getProductionOrder(id);
    }

    @Transactional
    public ProductionOrderResponse recordProductionResult(Long id, RecordProductionResultRequest req, User user) {
        ProductionOrder po = productionOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SPK Produksi tidak ditemukan: " + id));

        if (!"IN_PRODUCTION".equals(po.getStatus()) && !"QC_REVIEW".equals(po.getStatus())) {
            throw new RuntimeException("Hasil cetak hanya dapat dicatat saat status IN_PRODUCTION atau QC_REVIEW");
        }

        int totalProduced = 0;
        int totalDamaged = 0;

        // Mode 2: Granular per-card nasabah update
        if (req.cards() != null && !req.cards().isEmpty()) {
            Map<Long, Integer> itemProducedMap = new HashMap<>();
            Map<Long, Integer> itemDamagedMap = new HashMap<>();

            for (RecordProductionResultRequest.CardResult cr : req.cards()) {
                EmbossRecord record = embossRecordRepository.findById(cr.embossRecordId()).orElse(null);
                if (record != null && record.getProductionOrder() != null && record.getProductionOrder().getId().equals(id)) {
                    record.setProductionStatus(cr.result());
                    embossRecordRepository.save(record);

                    Long poiId = record.getProducedItem() != null ? record.getProducedItem().getId() : null;
                    if ("PRODUCED".equalsIgnoreCase(cr.result())) {
                        totalProduced++;
                        if (poiId != null) itemProducedMap.merge(poiId, 1, Integer::sum);
                    } else if ("PRODUCTION_FAILED".equalsIgnoreCase(cr.result())) {
                        totalDamaged++;
                        if (poiId != null) itemDamagedMap.merge(poiId, 1, Integer::sum);
                    }
                }
            }

            // Sync ke ProductionOrderItems
            List<ProductionOrderItem> pois = productionOrderItemRepository.findByProductionOrderIdWithItem(id);
            for (ProductionOrderItem poi : pois) {
                Long itemId = poi.getItem().getId();
                if (itemProducedMap.containsKey(itemId)) poi.setQtyProduced(itemProducedMap.get(itemId));
                if (itemDamagedMap.containsKey(itemId)) poi.setQtyDamaged(itemDamagedMap.get(itemId));
                productionOrderItemRepository.save(poi);
            }
        }
        // Mode 1: Aggregate input per item
        else if (req.items() != null && !req.items().isEmpty()) {
            for (RecordProductionResultRequest.ItemResult ir : req.items()) {
                ProductionOrderItem poi = productionOrderItemRepository.findById(ir.productionOrderItemId())
                        .orElseThrow(() -> new RuntimeException("Item produksi tidak ditemukan: " + ir.productionOrderItemId()));
                poi.setQtyProduced(ir.qtyProduced());
                poi.setQtyDamaged(ir.qtyDamaged());
                productionOrderItemRepository.save(poi);

                totalProduced += ir.qtyProduced();
                totalDamaged += ir.qtyDamaged();

                // Auto-mark queued emboss_records for traceability
                List<EmbossRecord> queuedRecords = embossRecordRepository.findByProductionOrderIdAndProductionStatus(id, "QUEUED");
                int markedProduced = 0;
                int markedDamaged = 0;
                for (EmbossRecord rec : queuedRecords) {
                    if (poi.getItem().equals(rec.getProducedItem())) {
                        if (markedProduced < ir.qtyProduced()) {
                            rec.setProductionStatus("PRODUCED");
                            markedProduced++;
                        } else if (markedDamaged < ir.qtyDamaged()) {
                            rec.setProductionStatus("PRODUCTION_FAILED");
                            markedDamaged++;
                        }
                        embossRecordRepository.save(rec);
                    }
                }
            }
        }

        // Catat kartu gagal cetak ke stok DAMAGED blank card di gudang
        if (totalDamaged > 0) {
            List<ProductionOrderItem> items = productionOrderItemRepository.findByProductionOrderIdWithItem(id);
            for (ProductionOrderItem poi : items) {
                if (poi.getQtyDamaged() > 0) {
                    List<ItemBom> boms = itemBomRepository.findByFinishedItemId(poi.getItem().getId());
                    Item rawBlank = !boms.isEmpty() ? boms.get(0).getMaterialItem() : poi.getItem();
                    stockBalanceRepository.findByWarehouseIdAndItemId(po.getWarehouse().getId(), rawBlank.getId())
                            .ifPresent(bal -> {
                                bal.setDamaged(bal.getDamaged() + poi.getQtyDamaged());
                                stockBalanceRepository.save(bal);
                            });
                }
            }
        }

        po.setTotalProduced(totalProduced);
        po.setTotalDamaged(totalDamaged);
        po.setStatus("QC_REVIEW");
        productionOrderRepository.save(po);

        return getProductionOrder(id);
    }

    @Transactional
    public ProductionOrderResponse completeProduction(Long id, User user) {
        ProductionOrder po = productionOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SPK Produksi tidak ditemukan: " + id));

        if (!"QC_REVIEW".equals(po.getStatus()) && !"IN_PRODUCTION".equals(po.getStatus())) {
            throw new RuntimeException("Produksi hanya dapat diselesaikan dari status QC_REVIEW atau IN_PRODUCTION");
        }

        List<ProductionOrderItem> items = productionOrderItemRepository.findByProductionOrderIdWithItem(id);
        Long warehouseId = po.getWarehouse().getId();

        // Kartu bagus masuk ke gudang sebagai kartu emboss (StockBalance.onHand bertambah & StockLedger PRODUCTION_IN)
        for (ProductionOrderItem poi : items) {
            if (poi.getQtyProduced() > 0) {
                inventoryService.recordStockMovement(
                        warehouseId,
                        poi.getItem().getId(),
                        "PRODUCTION_IN",
                        po.getProductionNumber(),
                        poi.getQtyProduced(),
                        0,
                        null,
                        "Penerimaan hasil cetak kartu debit emboss dari SPK: " + po.getProductionNumber(),
                        user
                );
            }
        }

        // Update status nasabah record yang PRODUCED menjadi PROCESSED
        List<EmbossRecord> producedRecords = embossRecordRepository.findByProductionOrderIdAndProductionStatus(id, "PRODUCED");
        for (EmbossRecord er : producedRecords) {
            er.setStatus("PROCESSED");
            embossRecordRepository.save(er);
        }

        po.setStatus("COMPLETED");
        po.setCompletedAt(LocalDateTime.now());
        productionOrderRepository.save(po);

        return getProductionOrder(id);
    }

    @Transactional
    public ProductionOrderResponse fulfillOrders(Long id, List<Long> orderIds, User user) {
        ProductionOrder po = productionOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SPK Produksi tidak ditemukan: " + id));

        if (!"COMPLETED".equals(po.getStatus())) {
            throw new RuntimeException("Pemenuhan order cabang hanya dapat dilakukan setelah produksi berstatus COMPLETED");
        }

        List<ProductionOrderFulfillment> fulfillments = fulfillmentRepository.findByProductionOrderIdWithOrder(id);
        List<Long> targetOrderIds = (orderIds != null && !orderIds.isEmpty())
                ? orderIds
                : fulfillments.stream().map(f -> f.getOrder().getId()).toList();

        for (Long oId : targetOrderIds) {
            orderRepository.findById(oId).ifPresent(ord -> {
                ord.setStatus("APPROVED");
                ord.setFulfillmentStatus("FULLY_FULFILLED");
                ord.setApprovedByUser(user);
                ord.setApprovedAt(LocalDateTime.now());

                // Alokasikan stok kartu emboss untuk order cabang
                for (OrderItem oi : ord.getItems()) {
                    oi.setQtyApproved(oi.getQtyRequested());
                    oi.setQtyAllocated(oi.getQtyRequested());

                    // Reservasi stok kartu emboss
                    stockBalanceRepository.findByWarehouseIdAndItemId(po.getWarehouse().getId(), oi.getItem().getId())
                            .ifPresent(sb -> {
                                sb.setReserved(sb.getReserved() + oi.getQtyRequested());
                                stockBalanceRepository.save(sb);
                            });
                }
                orderRepository.save(ord);

                // Update fulfillment record
                fulfillments.stream()
                        .filter(f -> f.getOrder().getId().equals(oId))
                        .findFirst()
                        .ifPresent(f -> {
                            f.setQtyFulfilled(ord.getTotalItems());
                            f.setFulfilledAt(LocalDateTime.now());
                            fulfillmentRepository.save(f);
                        });
            });
        }

        return getProductionOrder(id);
    }

    @Transactional(readOnly = true)
    public List<EmbossRecordResponse> getProductionCards(Long id) {
        return embossRecordRepository.findByProductionOrderIdWithDetails(id).stream()
                .map(EmbossRecordResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ItemBomResponse> getAllBoms() {
        return itemBomRepository.findAllWithDetails().stream()
                .map(ItemBomResponse::from)
                .toList();
    }
}
