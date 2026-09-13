package com.bankjatim.jims.controller;

import com.bankjatim.jims.common.ApiResponse;
import com.bankjatim.jims.domain.User;
import com.bankjatim.jims.dto.InventoryReturnRequest;
import com.bankjatim.jims.dto.InventoryReturnResponse;
import com.bankjatim.jims.repository.UserRepository;
import com.bankjatim.jims.security.UserPrincipal;
import com.bankjatim.jims.service.InventoryReturnService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/returns")
@RequiredArgsConstructor
@Tag(name = "Inventory Returns", description = "Endpoints untuk Permohonan dan Eksekusi Retur Barang Cabang ke Pusat")
public class InventoryReturnController {

    private final InventoryReturnService returnService;
    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "Daftar Permohonan Retur")
    public ResponseEntity<ApiResponse<List<InventoryReturnResponse>>> getReturns(
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(ApiResponse.ok(returnService.getAllReturns(status)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detail Permohonan Retur")
    public ResponseEntity<ApiResponse<InventoryReturnResponse>> getReturnDetail(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(returnService.getReturnById(id)));
    }

    @PostMapping
    @Operation(summary = "Buat Pengajuan Retur Barang")
    public ResponseEntity<ApiResponse<InventoryReturnResponse>> createReturn(
            @Valid @RequestBody InventoryReturnRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        User user = userRepository.getReferenceById(principal.getId());
        InventoryReturnResponse created = returnService.createReturn(request, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Pengajuan retur berhasil dibuat", created));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'INVENTORY_OFFICER')")
    @Operation(summary = "Setujui Permohonan Retur")
    public ResponseEntity<ApiResponse<InventoryReturnResponse>> approveReturn(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        User user = userRepository.getReferenceById(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Permohonan retur disetujui", returnService.approveReturn(id, user)));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'INVENTORY_OFFICER')")
    @Operation(summary = "Tolak Permohonan Retur")
    public ResponseEntity<ApiResponse<InventoryReturnResponse>> rejectReturn(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body,
            @AuthenticationPrincipal UserPrincipal principal) {
        User user = userRepository.getReferenceById(principal.getId());
        String reason = body != null ? body.get("reason") : null;
        return ResponseEntity.ok(ApiResponse.ok("Permohonan retur ditolak", returnService.rejectReturn(id, reason, user)));
    }

    @PostMapping("/{id}/ship")
    @Operation(summary = "Konfirmasi Pengiriman Retur")
    public ResponseEntity<ApiResponse<InventoryReturnResponse>> shipReturn(
            @PathVariable Long id,
            @RequestBody ShipRequest body) {
        return ResponseEntity.ok(ApiResponse.ok("Pengiriman retur berhasil dikonfirmasi",
                returnService.shipReturn(id, body.getCourierName(), body.getTrackingNumber())));
    }

    @PostMapping("/{id}/receive")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'WAREHOUSE_OFFICER')")
    @Operation(summary = "Konfirmasi Penerimaan Retur di Gudang Pusat")
    public ResponseEntity<ApiResponse<InventoryReturnResponse>> receiveReturn(
            @PathVariable Long id,
            @RequestBody ReceiveRequest body,
            @AuthenticationPrincipal UserPrincipal principal) {
        User user = userRepository.getReferenceById(principal.getId());
        List<InventoryReturnService.ReceiveLine> lines = null;
        if (body.getItems() != null) {
            lines = body.getItems().stream()
                    .map(i -> new InventoryReturnService.ReceiveLine(i.getItemId(), i.getQtyGood(), i.getQtyDamaged()))
                    .toList();
        }
        return ResponseEntity.ok(ApiResponse.ok("Penerimaan retur berhasil dicatat",
                returnService.receiveReturn(id, lines, user)));
    }

    @Data
    public static class ShipRequest {
        private String courierName;
        private String trackingNumber;
    }

    @Data
    public static class ReceiveRequest {
        private List<ItemLine> items;

        @Data
        public static class ItemLine {
            private Long itemId;
            private Integer qtyGood = 0;
            private Integer qtyDamaged = 0;
        }
    }
}
