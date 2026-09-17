package com.bankjatim.jims.controller;

import com.bankjatim.jims.common.ApiResponse;
import com.bankjatim.jims.common.PageResponse;
import com.bankjatim.jims.domain.StockBalance;
import com.bankjatim.jims.dto.StockBalanceResponse;
import com.bankjatim.jims.dto.StockLedgerResponse;
import com.bankjatim.jims.repository.ItemRepository;
import com.bankjatim.jims.repository.StockBalanceRepository;
import com.bankjatim.jims.repository.UserRepository;
import com.bankjatim.jims.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory & Stock Ledgers", description = "Endpoints untuk Saldo Stok (6-Bucket Engine) dan Buku Besar Persediaan")
public class InventoryController {

    private final InventoryService inventoryService;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final StockBalanceRepository stockBalanceRepository;

    @GetMapping("/stock-balances")
    @Operation(summary = "Daftar Saldo Stok Barang (Multi-Bucket)")
    public ResponseEntity<ApiResponse<PageResponse<StockBalanceResponse>>> getStockBalances(
            @RequestParam(required = false) Long warehouseId, Pageable pageable) {
        Page<StockBalance> page = inventoryService.getStockBalances(warehouseId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(page.map(StockBalanceResponse::from))));
    }

    @GetMapping("/stock-card/{itemId}")
    @Operation(summary = "Kartu Stok / Buku Besar Persediaan Barang")
    public ResponseEntity<ApiResponse<PageResponse<StockLedgerResponse>>> getStockCard(
            @PathVariable Long itemId, Pageable pageable) {
        Page<StockLedgerResponse> page = inventoryService.getStockCard(itemId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(page)));
    }

    @GetMapping("/ledgers")
    @Operation(summary = "Log Buku Besar / Mutasi Stok")
    public ResponseEntity<ApiResponse<PageResponse<StockLedgerResponse>>> getStockLedgers(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) String transactionType,
            Pageable pageable) {
        Page<StockLedgerResponse> page = inventoryService.getStockLedgers(warehouseId, transactionType, pageable);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(page)));
    }

    @GetMapping("/early-warning")
    @Operation(summary = "Deteksi Dini / Early Warning System (EWS)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getEarlyWarning() {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getEarlyWarningAnalysis()));
    }

    @PostMapping("/stock-opname")
    @Operation(summary = "Simpan Hasil Hitung Fisik Stock Opname")
    public ResponseEntity<ApiResponse<Map<String, Object>>> saveStockOpname(
            @RequestBody StockOpnameRequest request,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.bankjatim.jims.security.UserPrincipal principal) {
        com.bankjatim.jims.domain.User user = principal != null ? userRepository.getReferenceById(principal.getId()) : null;
        Map<String, Object> result = inventoryService.recordStockOpname(
                request.warehouseId(), request.docNo(), request.items(), user);
        return ResponseEntity.ok(ApiResponse.ok("Hasil Stock Opname berhasil dicatat", result));
    }

    @GetMapping("/initial-stock/worksheet")
    @Operation(summary = "Lembar Kerja Input Saldo Awal Gudang (Daftar Item & Saldo Berjalan)")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getInitialStockWorksheet(
            @RequestParam Long warehouseId) {
        List<com.bankjatim.jims.domain.Item> items = itemRepository.findAllWithCategory();
        List<StockBalance> balances = stockBalanceRepository.findByWarehouseIdWithItem(warehouseId);
        Map<Long, StockBalance> balanceMap = new HashMap<>();
        for (StockBalance sb : balances) {
            if (sb.getItem() != null) {
                balanceMap.put(sb.getItem().getId(), sb);
            }
        }

        List<Map<String, Object>> rows = new ArrayList<>();
        for (com.bankjatim.jims.domain.Item it : items) {
            if (it.getIsActive() != null && !it.getIsActive()) {
                continue;
            }
            StockBalance sb = balanceMap.get(it.getId());
            Map<String, Object> map = new HashMap<>();
            map.put("itemId", it.getId());
            map.put("sku", it.getSku());
            map.put("name", it.getName());
            map.put("uom", it.getUom());
            map.put("category", it.getCategory() != null ? it.getCategory().getName() : "Umum");
            map.put("unitCost", it.getEstimatedUnitPrice() != null ? it.getEstimatedUnitPrice() : BigDecimal.ZERO);
            map.put("qtyGood", sb != null ? sb.getOnHand() : 0);
            map.put("qtyDamaged", sb != null ? sb.getDamaged() : 0);
            rows.add(map);
        }

        return ResponseEntity.ok(ApiResponse.ok(rows));
    }

    @PostMapping("/initial-stock")
    @Operation(summary = "Input Saldo Awal Gudang (Single)")
    public ResponseEntity<ApiResponse<StockLedgerResponse>> createInitialStock(
            @RequestBody InitialStockRequest request,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.bankjatim.jims.security.UserPrincipal principal) {
        com.bankjatim.jims.domain.User user = principal != null ? userRepository.getReferenceById(principal.getId()) : null;
        StockLedgerResponse response = inventoryService.recordInitialStock(
                request.warehouseId(), request.itemId(), request.qty(), request.unitCost(), request.notes(), user);
        return ResponseEntity.ok(ApiResponse.ok("Saldo awal berhasil dicatat", response));
    }

    @PostMapping("/initial-stock/batch")
    @Operation(summary = "Simpan & Posting Saldo Awal Gudang Sekaligus (Batch Lembar Kerja)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createInitialStockBatch(
            @RequestBody BatchInitialStockRequest request,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.bankjatim.jims.security.UserPrincipal principal) {
        com.bankjatim.jims.domain.User user = principal != null ? userRepository.getReferenceById(principal.getId()) : null;
        Map<String, Object> response = inventoryService.recordInitialStockBatch(
                request.warehouseId(),
                request.cutoffDate(),
                request.notes(),
                request.items(),
                user
        );
        return ResponseEntity.ok(ApiResponse.ok("Saldo awal berhasil diposting ke sistem", response));
    }

    @PostMapping("/destructions")
    @Operation(summary = "Eksekusi Pemusnahan Barang Persediaan")
    public ResponseEntity<ApiResponse<StockLedgerResponse>> createDestruction(
            @RequestBody DestructionRequest request,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.bankjatim.jims.security.UserPrincipal principal) {
        com.bankjatim.jims.domain.User user = principal != null ? userRepository.getReferenceById(principal.getId()) : null;
        StockLedgerResponse response = inventoryService.recordDestruction(
                request.warehouseId(), request.itemId(), request.qty(), request.baNo(), request.notes(), user);
        return ResponseEntity.ok(ApiResponse.ok("Pemusnahan berhasil dieksekusi dan dicatat di buku besar", response));
    }

    @PostMapping("/reconciliation")
    @Operation(summary = "Jalankan Engine Rekonsiliasi Multi-Bucket")
    public ResponseEntity<ApiResponse<Map<String, Object>>> runReconciliation() {
        return ResponseEntity.ok(ApiResponse.ok("Rekonsiliasi selesai", inventoryService.runReconciliation()));
    }

    public record StockOpnameRequest(
            Long warehouseId,
            String docNo,
            java.util.List<InventoryService.OpnameItemRequest> items
    ) {}

    public record InitialStockRequest(
            Long warehouseId,
            Long itemId,
            Integer qty,
            java.math.BigDecimal unitCost,
            String notes
    ) {}

    public record DestructionRequest(
            Long warehouseId,
            Long itemId,
            Integer qty,
            String baNo,
            String notes
    ) {}

    public record BatchInitialStockRequest(
            Long warehouseId,
            String cutoffDate,
            String notes,
            java.util.List<InventoryService.InitialStockItemRow> items
    ) {}
}
