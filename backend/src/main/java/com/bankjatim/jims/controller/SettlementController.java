package com.bankjatim.jims.controller;

import com.bankjatim.jims.common.ApiResponse;
import com.bankjatim.jims.domain.User;
import com.bankjatim.jims.dto.SettlementResponse;
import com.bankjatim.jims.repository.UserRepository;
import com.bankjatim.jims.security.UserPrincipal;
import com.bankjatim.jims.service.SettlementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/finance")
@RequiredArgsConstructor
@Tag(name = "Finance & Settlements", description = "Endpoints untuk Rekonsiliasi & Settlement Finansial Antarunit Bank Jatim")
public class SettlementController {

    private final SettlementService settlementService;
    private final UserRepository userRepository;

    @GetMapping("/settlements")
    @Operation(summary = "Daftar Settlement Antarunit")
    public ResponseEntity<ApiResponse<List<SettlementResponse>>> getSettlements() {
        return ResponseEntity.ok(ApiResponse.ok(settlementService.getAllSettlements()));
    }

    @PostMapping("/settlements/create/{orderId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FINANCE_OFFICER')")
    @Operation(summary = "Buat Draft Settlement dari Pesanan Selesai")
    public ResponseEntity<ApiResponse<SettlementResponse>> createSettlement(
            @PathVariable Long orderId, @AuthenticationPrincipal UserPrincipal principal) {
        User user = userRepository.getReferenceById(principal.getId());
        SettlementResponse settlement = settlementService.createSettlementFromOrder(orderId, user);
        return ResponseEntity.ok(ApiResponse.ok("Draft settlement berhasil dibuat", settlement));
    }

    @PostMapping("/settlements/{id}/approve-post")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FINANCE_APPROVER')")
    @Operation(summary = "Persetujuan Settlement & Posting Jurnal Otomatis")
    public ResponseEntity<ApiResponse<SettlementResponse>> approveAndPost(
            @PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        User user = userRepository.getReferenceById(principal.getId());
        SettlementResponse settlement = settlementService.approveAndPost(id, user);
        return ResponseEntity.ok(ApiResponse.ok("Settlement disetujui dan jurnal GL berhasil diposting", settlement));
    }
}
