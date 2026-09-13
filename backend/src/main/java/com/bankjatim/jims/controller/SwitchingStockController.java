package com.bankjatim.jims.controller;

import com.bankjatim.jims.common.ApiResponse;
import com.bankjatim.jims.dto.SwitchingStockRequest;
import com.bankjatim.jims.dto.SwitchingStockResponse;
import com.bankjatim.jims.security.UserPrincipal;
import com.bankjatim.jims.service.SwitchingStockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/inventory/switching")
@RequiredArgsConstructor
@Tag(name = "Switching Stock", description = "Endpoint pengajuan dan approval switching stock antar-cabang")
public class SwitchingStockController {

    private final SwitchingStockService switchingStockService;

    @GetMapping
    @Operation(summary = "Daftar Pengajuan Switching Stock")
    public ResponseEntity<ApiResponse<List<SwitchingStockResponse>>> getSwitchingStocks(
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(ApiResponse.ok(switchingStockService.getSwitchingStocks(status)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'INVENTORY_OFFICER', 'WAREHOUSE_OFFICER')")
    @Operation(summary = "Buat Pengajuan Switching Stock")
    public ResponseEntity<ApiResponse<SwitchingStockResponse>> createSwitching(
            @Valid @RequestBody SwitchingStockRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok("Pengajuan switching berhasil dibuat",
                switchingStockService.createSwitching(request, principal.getId())));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SWITCHING_APPROVER')")
    @Operation(summary = "Setujui Pengajuan Switching Stock")
    public ResponseEntity<ApiResponse<SwitchingStockResponse>> approveSwitching(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok("Pengajuan switching berhasil disetujui",
                switchingStockService.approveSwitching(id, principal.getId())));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SWITCHING_APPROVER')")
    @Operation(summary = "Tolak Pengajuan Switching Stock")
    public ResponseEntity<ApiResponse<SwitchingStockResponse>> rejectSwitching(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> request,
            @AuthenticationPrincipal UserPrincipal principal) {
        String reason = request != null ? request.get("reason") : null;
        return ResponseEntity.ok(ApiResponse.ok("Pengajuan switching berhasil ditolak",
                switchingStockService.rejectSwitching(id, reason, principal.getId())));
    }
}
