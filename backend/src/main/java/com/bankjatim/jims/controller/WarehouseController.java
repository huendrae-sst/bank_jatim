package com.bankjatim.jims.controller;

import com.bankjatim.jims.common.ApiResponse;
import com.bankjatim.jims.domain.User;
import com.bankjatim.jims.dto.WarehousePackingResponse;
import com.bankjatim.jims.dto.WarehousePickingResponse;
import com.bankjatim.jims.dto.WarehouseQueueResponse;
import com.bankjatim.jims.repository.UserRepository;
import com.bankjatim.jims.security.UserPrincipal;
import com.bankjatim.jims.service.WarehouseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/warehouse")
@RequiredArgsConstructor
@Tag(name = "Warehouse Operations", description = "Endpoints untuk Operasional Antrean Picking dan Packing Gudang")
public class WarehouseController {

    private final WarehouseService warehouseService;
    private final UserRepository userRepository;

    @GetMapping("/picking")
    @Operation(summary = "Daftar Antrean Picking Gudang")
    public ResponseEntity<ApiResponse<List<WarehouseQueueResponse>>> getPickingQueue(
            @RequestParam(required = false) String type) {
        return ResponseEntity.ok(ApiResponse.ok(warehouseService.getPickingQueue(type)));
    }

    @GetMapping("/packing")
    @Operation(summary = "Daftar Antrean Packing Gudang")
    public ResponseEntity<ApiResponse<List<WarehouseQueueResponse>>> getPackingQueue(
            @RequestParam(required = false) String type) {
        return ResponseEntity.ok(ApiResponse.ok(warehouseService.getPackingQueue(type)));
    }

    @PostMapping("/picking/{id}/process")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'WAREHOUSE_OFFICER')")
    @Operation(summary = "Proses Selesai Picking Barang")
    public ResponseEntity<ApiResponse<WarehousePickingResponse>> processPicking(
            @PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        User user = userRepository.getReferenceById(principal.getId());
        WarehousePickingResponse picking = warehouseService.completePicking(id, user);
        return ResponseEntity.ok(ApiResponse.ok("Picking selesai diproses", picking));
    }

    @PostMapping("/picking/batch-process")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'WAREHOUSE_OFFICER')")
    @Operation(summary = "Proses Selesai Picking Barang Massal (Bulk/Wave Picking)")
    public ResponseEntity<ApiResponse<List<WarehousePickingResponse>>> processBatchPicking(
            @RequestBody com.bankjatim.jims.dto.BulkOperationDtos.BatchPickingRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        User user = userRepository.getReferenceById(principal.getId());
        List<WarehousePickingResponse> result = warehouseService.completeBatchPicking(request.orderIds(), user);
        return ResponseEntity.ok(ApiResponse.ok("Batch picking selesai diproses", result));
    }

    @PostMapping("/packing/{id}/process")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'WAREHOUSE_OFFICER')")
    @Operation(summary = "Proses Selesai Packing Koli")
    public ResponseEntity<ApiResponse<WarehousePackingResponse>> processPacking(
            @PathVariable Long id, @RequestBody PackingRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        User user = userRepository.getReferenceById(principal.getId());
        WarehousePackingResponse packing = warehouseService.completePacking(
                id, request.getKoliCount(), request.getTotalWeightKg(), request.getDimensionsCm(), user);
        return ResponseEntity.ok(ApiResponse.ok("Packing selesai dan siap kirim", packing));
    }

    @PostMapping("/packing/batch-process")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'WAREHOUSE_OFFICER')")
    @Operation(summary = "Proses Selesai Packing Koli Massal (Batch Packing)")
    public ResponseEntity<ApiResponse<List<WarehousePackingResponse>>> processBatchPacking(
            @RequestBody com.bankjatim.jims.dto.BulkOperationDtos.BatchPackingRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        User user = userRepository.getReferenceById(principal.getId());
        List<WarehousePackingResponse> result = warehouseService.completeBatchPacking(request.items(), user);
        return ResponseEntity.ok(ApiResponse.ok("Batch packing selesai diproses", result));
    }

    @Data
    public static class PackingRequest {
        private int koliCount = 1;
        private BigDecimal totalWeightKg = new BigDecimal("1.00");
        private String dimensionsCm = "30x20x15";
    }
}
