package com.bankjatim.jims.controller;

import com.bankjatim.jims.common.ApiResponse;
import com.bankjatim.jims.domain.Discrepancy;
import com.bankjatim.jims.domain.User;
import com.bankjatim.jims.dto.DiscrepancyResponse;
import com.bankjatim.jims.dto.ReceivingResponse;
import com.bankjatim.jims.repository.UserRepository;
import com.bankjatim.jims.security.UserPrincipal;
import com.bankjatim.jims.service.ReceivingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/receiving")
@RequiredArgsConstructor
@Tag(name = "Receiving & Discrepancies", description = "Endpoints untuk Konfirmasi Penerimaan Barang Unit dan Berita Acara Kerusakan")
public class ReceivingController {

    private final ReceivingService receivingService;
    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "Daftar Penerimaan Cabang")
    public ResponseEntity<ApiResponse<List<ReceivingResponse>>> getReceivings(
            @RequestParam(required = false) Long organizationId) {
        return ResponseEntity.ok(ApiResponse.ok(receivingService.getReceivings(organizationId)));
    }

    @PostMapping("/confirm/{shipmentId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'RECEIVING_OFFICER')")
    @Operation(summary = "Konfirmasi Penerimaan Fisik Barang")
    public ResponseEntity<ApiResponse<ReceivingResponse>> confirmReceipt(
            @PathVariable Long shipmentId, @RequestBody ReceiptConfirmationRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        User user = userRepository.getReferenceById(principal.getId());
        ReceivingResponse receiving = receivingService.confirmReceipt(
                shipmentId, request.getPodSignature(), request.getNotes(), request.getDiscrepancies(), user);
        return ResponseEntity.ok(ApiResponse.ok("Penerimaan barang berhasil dikonfirmasi", receiving));
    }

    @GetMapping("/discrepancies")
    @Operation(summary = "Daftar Selisih / Kerusakan Fisik")
    public ResponseEntity<ApiResponse<List<DiscrepancyResponse>>> getDiscrepancies() {
        return ResponseEntity.ok(ApiResponse.ok(receivingService.getDiscrepancies()));
    }

    @GetMapping("/discrepancies/{id}")
    @Operation(summary = "Detail Selisih / Kerusakan Fisik")
    public ResponseEntity<ApiResponse<DiscrepancyResponse>> getDiscrepancyById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(receivingService.getDiscrepancyById(id)));
    }

    @Data
    public static class ReceiptConfirmationRequest {
        private String podSignature;
        private String notes;
        private List<Discrepancy> discrepancies;
    }
}
