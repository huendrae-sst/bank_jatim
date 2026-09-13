package com.bankjatim.jims.controller;

import com.bankjatim.jims.common.ApiResponse;
import com.bankjatim.jims.common.PageResponse;
import com.bankjatim.jims.domain.User;
import com.bankjatim.jims.dto.GoodsReceiptResponse;
import com.bankjatim.jims.dto.PurchaseOrderResponse;
import com.bankjatim.jims.dto.PurchaseRequestResponse;
import com.bankjatim.jims.repository.UserRepository;
import com.bankjatim.jims.security.UserPrincipal;
import com.bankjatim.jims.service.ProcurementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/procurement")
@RequiredArgsConstructor
@Tag(name = "Procurement", description = "Endpoints untuk Purchase Request, Konsolidasi PR ke PO, dan Penerimaan Vendor")
public class ProcurementController {

    private final ProcurementService procurementService;
    private final UserRepository userRepository;

    @GetMapping("/pr")
    @Operation(summary = "Daftar Purchase Request (PR)")
    public ResponseEntity<ApiResponse<PageResponse<PurchaseRequestResponse>>> getPrs(
            @RequestParam(required = false) Long organizationId, Pageable pageable) {
        Page<PurchaseRequestResponse> page = procurementService.getPurchaseRequests(organizationId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(page)));
    }

    @GetMapping("/pr/{id}")
    @Operation(summary = "Detail Purchase Request (PR)")
    public ResponseEntity<ApiResponse<PurchaseRequestResponse>> getPrById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(procurementService.getPurchaseRequestById(id)));
    }

    @PostMapping("/pr/{id}/reject")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PROCUREMENT_APPROVER')")
    @Operation(summary = "Reject Purchase Request")
    public ResponseEntity<ApiResponse<PurchaseRequestResponse>> rejectPr(
            @PathVariable Long id, @RequestBody(required = false) RejectPoRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        User user = userRepository.getReferenceById(principal.getId());
        String reason = request != null ? request.getReason() : "Ditolak oleh approver";
        PurchaseRequestResponse pr = procurementService.rejectPR(id, reason, user);
        return ResponseEntity.ok(ApiResponse.ok("PR berhasil ditolak", pr));
    }

    @PostMapping("/pr/{id}/approve")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PROCUREMENT_APPROVER')")
    @Operation(summary = "Approve Purchase Request")
    public ResponseEntity<ApiResponse<PurchaseRequestResponse>> approvePr(
            @PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        User user = userRepository.getReferenceById(principal.getId());
        PurchaseRequestResponse pr = procurementService.approvePR(id, user);
        return ResponseEntity.ok(ApiResponse.ok("PR berhasil disetujui", pr));
    }

    @GetMapping("/consolidation")
    @Operation(summary = "Approved PR Pool untuk Konsolidasi")
    public ResponseEntity<ApiResponse<List<PurchaseRequestResponse>>> getApprovedPool() {
        List<PurchaseRequestResponse> pool = procurementService.getApprovedPrPool();
        return ResponseEntity.ok(ApiResponse.ok(pool));
    }

    @PostMapping("/consolidate")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PROCUREMENT_OFFICER')")
    @Operation(summary = "Konsolidasi PR menjadi satu PO")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> consolidatePr(
            @RequestBody ConsolidateRequest request, @AuthenticationPrincipal UserPrincipal principal) {
        User user = userRepository.getReferenceById(principal.getId());
        PurchaseOrderResponse po = procurementService.consolidatePrsToPo(
                request.getVendorId(), request.getWarehouseId(), request.getPrItemIds(), user);
        return ResponseEntity.ok(ApiResponse.ok("Konsolidasi berhasil menjadi " + po.poNumber(), po));
    }

    @GetMapping("/po")
    @Operation(summary = "Daftar Purchase Order (PO)")
    public ResponseEntity<ApiResponse<List<PurchaseOrderResponse>>> getPos() {
        return ResponseEntity.ok(ApiResponse.ok(procurementService.getPurchaseOrders()));
    }

    @GetMapping("/po/{id}")
    @Operation(summary = "Detail Purchase Order (PO)")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> getPoById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(procurementService.getPurchaseOrderById(id)));
    }

    @PostMapping("/po/{id}/approve")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PROCUREMENT_APPROVER')")
    @Operation(summary = "Approve Purchase Order")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> approvePo(
            @PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        User user = userRepository.getReferenceById(principal.getId());
        PurchaseOrderResponse po = procurementService.approvePO(id, user);
        return ResponseEntity.ok(ApiResponse.ok("PO berhasil disetujui", po));
    }

    @PostMapping("/po/{id}/reject")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PROCUREMENT_APPROVER')")
    @Operation(summary = "Reject Purchase Order")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> rejectPo(
            @PathVariable Long id, @RequestBody RejectPoRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        User user = userRepository.getReferenceById(principal.getId());
        PurchaseOrderResponse po = procurementService.rejectPO(id, request.getReason(), user);
        return ResponseEntity.ok(ApiResponse.ok("PO berhasil ditolak", po));
    }

    @PostMapping("/po/{id}/receive-goods")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'WAREHOUSE_OFFICER')")
    @Operation(summary = "Penerimaan Barang Vendor (GRN)")
    public ResponseEntity<ApiResponse<GoodsReceiptResponse>> receiveGoods(
            @PathVariable Long id, @RequestParam String deliveryNoteNumber,
            @AuthenticationPrincipal UserPrincipal principal) {
        User user = userRepository.getReferenceById(principal.getId());
        GoodsReceiptResponse grn = procurementService.receiveGoodsFromVendor(id, deliveryNoteNumber, user);
        return ResponseEntity.ok(ApiResponse.ok("Penerimaan barang vendor berhasil dicatat", grn));
    }

    @Data
    public static class ConsolidateRequest {
        private Long vendorId;
        private Long warehouseId;
        private List<Long> prItemIds;
    }

    @Data
    public static class RejectPoRequest {
        private String reason;
    }
}
