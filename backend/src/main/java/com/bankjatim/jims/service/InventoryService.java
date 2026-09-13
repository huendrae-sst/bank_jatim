package com.bankjatim.jims.service;

import com.bankjatim.jims.domain.*;
import com.bankjatim.jims.dto.StockBalanceResponse;
import com.bankjatim.jims.dto.StockLedgerResponse;
import com.bankjatim.jims.repository.ItemRepository;
import com.bankjatim.jims.repository.StockBalanceRepository;
import com.bankjatim.jims.repository.StockLedgerRepository;
import com.bankjatim.jims.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final StockBalanceRepository stockBalanceRepository;
    private final StockLedgerRepository stockLedgerRepository;
    private final ItemRepository itemRepository;
    private final WarehouseRepository warehouseRepository;

    @Transactional(readOnly = true)
    public Page<StockBalance> getStockBalances(Long warehouseId, Pageable pageable) {
        return stockBalanceRepository.findAllWithDetails(warehouseId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<StockLedgerResponse> getStockCard(Long itemId, Pageable pageable) {
        return stockLedgerRepository.findByItemIdOrderByCreatedAtDesc(itemId, pageable)
                .map(StockLedgerResponse::from);
    }

    @Transactional
    public void recordStockMovement(Long warehouseId, Long itemId, String transactionType,
                                    String referenceNumber, int qtyIn, int qtyOut,
                                    BigDecimal unitCost, String notes, User user) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new RuntimeException("Gudang tidak ditemukan: " + warehouseId));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Barang tidak ditemukan: " + itemId));

        StockBalance balance = stockBalanceRepository.findByWarehouseIdAndItemId(warehouseId, itemId)
                .orElseGet(() -> StockBalance.builder()
                        .warehouse(warehouse)
                        .item(item)
                        .onHand(0)
                        .reserved(0)
                        .allocated(0)
                        .inTransit(0)
                        .hold(0)
                        .damaged(0)
                        .build());

        int currentOnHand = balance.getOnHand();
        int newOnHand = currentOnHand + qtyIn - qtyOut;
        balance.setOnHand(newOnHand);
        stockBalanceRepository.save(balance);

        BigDecimal effectiveUnitCost = unitCost != null && unitCost.compareTo(BigDecimal.ZERO) > 0
                ? unitCost
                : item.getEstimatedUnitPrice();
        BigDecimal totalValue = effectiveUnitCost.multiply(BigDecimal.valueOf(qtyIn > 0 ? qtyIn : qtyOut));

        StockLedger ledger = StockLedger.builder()
                .warehouse(warehouse)
                .item(item)
                .transactionType(transactionType)
                .referenceNumber(referenceNumber)
                .qtyIn(qtyIn)
                .qtyOut(qtyOut)
                .balanceAfter(newOnHand)
                .unitCost(effectiveUnitCost)
                .totalValue(totalValue)
                .notes(notes)
                .createdByUser(user)
                .build();

        stockLedgerRepository.save(ledger);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getEarlyWarningAnalysis() {
        List<StockBalance> lowStocks = stockBalanceRepository.findLowStockItems();
        Map<String, Object> result = new HashMap<>();
        result.put("criticalCount", lowStocks.size());
        result.put("lowStockItems", lowStocks.stream().map(StockBalanceResponse::from).toList());
        return result;
    }

    @Transactional(readOnly = true)
    public Page<StockLedgerResponse> getStockLedgers(Long warehouseId, String transactionType, Pageable pageable) {
        return stockLedgerRepository.findByWarehouseAndTransactionType(warehouseId, transactionType, pageable)
                .map(StockLedgerResponse::from);
    }

    @Transactional
    public StockLedgerResponse recordInitialStock(Long warehouseId, Long itemId, Integer qty, BigDecimal unitCost, String notes, User user) {
        String ref = "INIT-" + System.currentTimeMillis();
        recordStockMovement(warehouseId, itemId, "INITIAL_STOCK", ref, qty, 0, unitCost, notes, user);
        StockLedger ledger = stockLedgerRepository.findByReferenceNumber(ref).get(0);
        return StockLedgerResponse.from(ledger);
    }

    @Transactional
    public Map<String, Object> recordStockOpname(Long warehouseId, String docNo, List<OpnameItemRequest> items, User user) {
        String effectiveDocNo = (docNo != null && !docNo.isBlank()) ? docNo : "SO-" + System.currentTimeMillis();
        int matched = 0;
        int adjusted = 0;

        for (OpnameItemRequest itemReq : items) {
            int diff = itemReq.actualQty() - itemReq.systemQty();
            if (diff != 0) {
                if (diff > 0) {
                    recordStockMovement(warehouseId, itemReq.itemId(), "OPNAME_ADJUSTMENT", effectiveDocNo, diff, 0, null, itemReq.notes(), user);
                } else {
                    recordStockMovement(warehouseId, itemReq.itemId(), "OPNAME_ADJUSTMENT", effectiveDocNo, 0, -diff, null, itemReq.notes(), user);
                }
                adjusted++;
            } else {
                recordStockMovement(warehouseId, itemReq.itemId(), "STOCK_OPNAME", effectiveDocNo, 0, 0, null, itemReq.notes() != null ? itemReq.notes() : "Fisik cocok (Match)", user);
                matched++;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("docNo", effectiveDocNo);
        result.put("totalItems", items.size());
        result.put("matchedCount", matched);
        result.put("adjustedCount", adjusted);
        result.put("status", "COMPLETED");
        return result;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> runReconciliation() {
        List<StockBalance> balances = stockBalanceRepository.findAll();
        int totalSku = balances.size();
        int totalPhysical = balances.stream().mapToInt(StockBalance::getOnHand).sum();
        int totalReserved = balances.stream().mapToInt(StockBalance::getReserved).sum();

        Map<String, Object> result = new HashMap<>();
        result.put("skuCount", totalSku);
        result.put("totalPhysical", totalPhysical);
        result.put("totalReserved", totalReserved);
        result.put("status", "RECONCILED");
        result.put("timestamp", LocalDateTime.now());
        result.put("balances", balances.stream().map(StockBalanceResponse::from).toList());
        return result;
    }

    @Transactional
    public StockLedgerResponse recordDestruction(Long warehouseId, Long itemId, Integer qty, String baNo, String notes, User user) {
        String effectiveBaNo = (baNo != null && !baNo.isBlank()) ? baNo : "BA-DST-" + System.currentTimeMillis();
        recordStockMovement(warehouseId, itemId, "DESTRUCTION", effectiveBaNo, 0, qty, null, notes, user);
        StockLedger ledger = stockLedgerRepository.findByReferenceNumber(effectiveBaNo).get(0);
        return StockLedgerResponse.from(ledger);
    }

    public record OpnameItemRequest(Long itemId, Integer systemQty, Integer actualQty, String notes) {}
}
