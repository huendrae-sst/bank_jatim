package com.bankjatim.jims.controller;

import com.bankjatim.jims.common.ApiResponse;
import com.bankjatim.jims.domain.User;
import com.bankjatim.jims.dto.ShipmentResponse;
import com.bankjatim.jims.repository.UserRepository;
import com.bankjatim.jims.security.UserPrincipal;
import com.bankjatim.jims.service.DistributionService;
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
@RequestMapping("/distribution")
@RequiredArgsConstructor
@Tag(name = "Distribution & Ekspedisi", description = "Endpoints untuk Manifest Pengiriman dan Pelacakan Resi")
public class DistributionController {

    private final DistributionService distributionService;
    private final UserRepository userRepository;

    @GetMapping("/shipments")
    @Operation(summary = "Daftar Pengiriman / Manifest")
    public ResponseEntity<ApiResponse<List<ShipmentResponse>>> getShipments() {
        return ResponseEntity.ok(ApiResponse.ok(distributionService.getAllShipments()));
    }

    @GetMapping("/shipments/{id}")
    @Operation(summary = "Detail Pengiriman / Manifest")
    public ResponseEntity<ApiResponse<ShipmentResponse>> getShipmentById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(distributionService.getShipmentById(id)));
    }

    @PostMapping("/shipments")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'DISTRIBUTION_OFFICER')")
    @Operation(summary = "Buat Manifest Pengiriman & Penyerahan ke Kurir")
    public ResponseEntity<ApiResponse<ShipmentResponse>> createShipment(
            @RequestBody CreateShipmentRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        User user = userRepository.getReferenceById(principal.getId());
        ShipmentResponse shipment = distributionService.createShipment(
                request.getOrderId(), request.getCourierId(), request.getServiceType(),
                request.getTrackingNumber(), request.getShippingCost(), user);
        return ResponseEntity.ok(ApiResponse.ok("Manifest dan resi berhasil diterbitkan", shipment));
    }

    @PostMapping("/shipments/bulk")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'DISTRIBUTION_OFFICER')")
    @Operation(summary = "Terbitkan Manifest Massal (Bulk Dispatch / Multi-Cabang)")
    public ResponseEntity<ApiResponse<List<ShipmentResponse>>> createBulkShipments(
            @RequestBody com.bankjatim.jims.dto.BulkOperationDtos.BulkShipmentRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        List<ShipmentResponse> shipments = distributionService.createBulkShipments(request, principal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Batch manifest dan resi pengiriman berhasil diterbitkan", shipments));
    }

    @PostMapping("/routine-drops")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'DISTRIBUTION_OFFICER', 'WAREHOUSE_OFFICER', 'INVENTORY_OFFICER', 'ORDER_APPROVER', 'MANAGEMENT', 'USER_ADMIN', 'REQUESTER_CABANG')")
    @Operation(summary = "Inisiasi Distribusi Rutin / Kuota Pusat ke Multi-Cabang")
    public ResponseEntity<ApiResponse<List<com.bankjatim.jims.dto.OrderResponse>>> createRoutineDistribution(
            @RequestBody com.bankjatim.jims.dto.BulkOperationDtos.RoutineDistributionRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        List<com.bankjatim.jims.dto.OrderResponse> orders = distributionService.createRoutineDistribution(request, principal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Distribusi rutin berhasil diterbitkan dan siap diambil di gudang", orders));
    }

    @GetMapping("/routine-drops")
    @Operation(summary = "Daftar Riwayat Distribusi Rutin")
    public ResponseEntity<ApiResponse<List<com.bankjatim.jims.dto.OrderResponse>>> getRoutineDrops() {
        return ResponseEntity.ok(ApiResponse.ok(distributionService.getRoutineDistributions()));
    }

    @Data
    public static class CreateShipmentRequest {
        private Long orderId;
        private Long courierId;
        private String serviceType;
        private String trackingNumber;
        private BigDecimal shippingCost;
    }
}
