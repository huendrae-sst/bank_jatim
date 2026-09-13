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

    @Data
    public static class CreateShipmentRequest {
        private Long orderId;
        private Long courierId;
        private String serviceType;
        private String trackingNumber;
        private BigDecimal shippingCost;
    }
}
