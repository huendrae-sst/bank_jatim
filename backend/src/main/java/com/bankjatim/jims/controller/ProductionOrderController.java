package com.bankjatim.jims.controller;

import com.bankjatim.jims.common.ApiResponse;
import com.bankjatim.jims.domain.User;
import com.bankjatim.jims.dto.*;
import com.bankjatim.jims.repository.UserRepository;
import com.bankjatim.jims.security.UserPrincipal;
import com.bankjatim.jims.service.ProductionOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/production-orders")
@RequiredArgsConstructor
@Tag(name = "Card Emboss Production", description = "Endpoints untuk Manajemen Siklus Hidup Produksi & Personalisasi Kartu ATM/Debit")
public class ProductionOrderController {

    private final ProductionOrderService productionOrderService;
    private final UserRepository userRepository;

    private User getUserFromPrincipal(UserPrincipal principal) {
        if (principal == null || principal.getId() == null) {
            return null;
        }
        return userRepository.findById(principal.getId()).orElse(null);
    }

    @GetMapping
    @Operation(summary = "Daftar Semua SPK Produksi Kartu")
    public ResponseEntity<ApiResponse<List<ProductionOrderResponse>>> getAllProductionOrders() {
        return ResponseEntity.ok(ApiResponse.ok(productionOrderService.getAllProductionOrders()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detail SPK Produksi Kartu")
    public ResponseEntity<ApiResponse<ProductionOrderResponse>> getProductionOrder(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(productionOrderService.getProductionOrder(id)));
    }

    @GetMapping("/{id}/cards")
    @Operation(summary = "Daftar Kartu Nasabah Terkait SPK Produksi (Traceability Per-Kartu)")
    public ResponseEntity<ApiResponse<List<EmbossRecordResponse>>> getProductionCards(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(productionOrderService.getProductionCards(id)));
    }

    @GetMapping("/consolidation")
    @Operation(summary = "Data Konsolidasi Order Emboss dari Berbagai Cabang")
    public ResponseEntity<ApiResponse<List<ConsolidationViewResponse>>> getConsolidationView() {
        return ResponseEntity.ok(ApiResponse.ok(productionOrderService.getConsolidationView()));
    }

    @GetMapping("/bom")
    @Operation(summary = "Daftar Bill of Materials (BOM) Kartu Emboss")
    public ResponseEntity<ApiResponse<List<ItemBomResponse>>> getAllBoms() {
        return ResponseEntity.ok(ApiResponse.ok(productionOrderService.getAllBoms()));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'WAREHOUSE_OFFICER', 'INVENTORY_OFFICER')")
    @Operation(summary = "Terbitkan SPK Produksi dari Konsolidasi Order")
    public ResponseEntity<ApiResponse<ProductionOrderResponse>> createProductionOrder(
            @RequestBody CreateProductionOrderRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        User user = getUserFromPrincipal(principal);
        ProductionOrderResponse created = productionOrderService.createProductionOrder(request, user);
        return ResponseEntity.ok(ApiResponse.ok("SPK Produksi berhasil diterbitkan", created));
    }

    @PostMapping("/{id}/request-material")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'WAREHOUSE_OFFICER')")
    @Operation(summary = "Ajukan Permintaan Pengeluaran Kartu Blank dari Gudang")
    public ResponseEntity<ApiResponse<ProductionOrderResponse>> requestMaterialIssue(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        User user = getUserFromPrincipal(principal);
        ProductionOrderResponse updated = productionOrderService.requestMaterialIssue(id, user);
        return ResponseEntity.ok(ApiResponse.ok("Pengeluaran bahan baku berhasil diajukan ke approver", updated));
    }

    @PostMapping("/{id}/approve-material")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'INVENTORY_OFFICER')")
    @Operation(summary = "Setujui Pengeluaran Bahan Baku Kartu Blank (INVENTORY_OFFICER)")
    public ResponseEntity<ApiResponse<ProductionOrderResponse>> approveMaterialIssue(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        User approver = getUserFromPrincipal(principal);
        ProductionOrderResponse updated = productionOrderService.approveMaterialIssue(id, approver);
        return ResponseEntity.ok(ApiResponse.ok("Pengeluaran bahan baku disetujui", updated));
    }

    @PostMapping("/{id}/issue-material")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'WAREHOUSE_OFFICER', 'INVENTORY_OFFICER')")
    @Operation(summary = "Keluarkan Kartu Blank dari Gudang (Stok On-Hand Berkurang & Catat Ledger)")
    public ResponseEntity<ApiResponse<ProductionOrderResponse>> issueMaterial(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        User user = getUserFromPrincipal(principal);
        ProductionOrderResponse updated = productionOrderService.issueMaterial(id, user);
        return ResponseEntity.ok(ApiResponse.ok("Kartu blank berhasil dikeluarkan dari gudang ke mesin cetak", updated));
    }

    @PostMapping("/{id}/record-result")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'WAREHOUSE_OFFICER', 'INVENTORY_OFFICER')")
    @Operation(summary = "Input Hasil Cetak / Emboss (Bagus vs Gagal Per Kartu / Per Item)")
    public ResponseEntity<ApiResponse<ProductionOrderResponse>> recordProductionResult(
            @PathVariable Long id,
            @RequestBody RecordProductionResultRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        User user = getUserFromPrincipal(principal);
        ProductionOrderResponse updated = productionOrderService.recordProductionResult(id, request, user);
        return ResponseEntity.ok(ApiResponse.ok("Hasil cetak berhasil dicatat", updated));
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'WAREHOUSE_OFFICER', 'INVENTORY_OFFICER')")
    @Operation(summary = "Selesaikan QC & Masukkan Kartu Emboss Jadi ke Gudang (Stok On-Hand Bertambah)")
    public ResponseEntity<ApiResponse<ProductionOrderResponse>> completeProduction(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        User user = getUserFromPrincipal(principal);
        ProductionOrderResponse updated = productionOrderService.completeProduction(id, user);
        return ResponseEntity.ok(ApiResponse.ok("Produksi selesai dan kartu emboss telah masuk stok gudang", updated));
    }

    @PostMapping("/{id}/fulfill-orders")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'WAREHOUSE_OFFICER', 'INVENTORY_OFFICER')")
    @Operation(summary = "Bulk Pemenuhan Order Emboss Cabang dari SPK Produksi")
    public ResponseEntity<ApiResponse<ProductionOrderResponse>> fulfillOrders(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, List<Long>> request,
            @AuthenticationPrincipal UserPrincipal principal) {
        User user = getUserFromPrincipal(principal);
        List<Long> orderIds = request != null ? request.get("orderIds") : null;
        ProductionOrderResponse updated = productionOrderService.fulfillOrders(id, orderIds, user);
        return ResponseEntity.ok(ApiResponse.ok("Order cabang berhasil dipenuhi dan siap diproses di picking gudang", updated));
    }
}
