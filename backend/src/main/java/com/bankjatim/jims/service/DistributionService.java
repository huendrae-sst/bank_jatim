package com.bankjatim.jims.service;

import com.bankjatim.jims.domain.*;
import com.bankjatim.jims.dto.ShipmentResponse;
import com.bankjatim.jims.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DistributionService {

    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;
    private final CourierRepository courierRepository;
    private final WarehouseRepository warehouseRepository;
    private final ExpeditionMappingRepository expeditionMappingRepository;

    @Transactional(readOnly = true)
    public List<ShipmentResponse> getAllShipments() {
        return shipmentRepository.findAll().stream()
                .map(ShipmentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ShipmentResponse getShipmentById(Long id) {
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pengiriman / Manifest tidak ditemukan: " + id));
        return ShipmentResponse.from(shipment);
    }

    @Transactional
    public ShipmentResponse createShipment(Long orderId, Long courierId, String serviceType,
                                   String trackingNumber, BigDecimal shippingCost, User dispatcher) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order tidak ditemukan: " + orderId));

        Warehouse originWarehouse = warehouseRepository.findByType("CENTRAL_LOGISTICS")
                .orElseGet(() -> warehouseRepository.findAll().get(0));

        Courier courier = null;
        if (courierId != null) {
            courier = courierRepository.findById(courierId).orElse(null);
        } else {
            ExpeditionMapping mapping = expeditionMappingRepository.findByDestinationOrganizationId(
                    order.getRequestingOrganization().getId()
            ).orElse(null);
            if (mapping != null) {
                courier = mapping.getCourier();
                serviceType = mapping.getServiceType();
            }
        }

        String manifestNumber = "MAN-" + System.currentTimeMillis();
        String effectiveTrackingNumber = trackingNumber != null ? trackingNumber : "AWB-BJ-" + System.currentTimeMillis();

        Shipment shipment = Shipment.builder()
                .manifestNumber(manifestNumber)
                .order(order)
                .originWarehouse(originWarehouse)
                .destinationOrganization(order.getRequestingOrganization())
                .courier(courier)
                .serviceType(serviceType != null ? serviceType : "REGULER")
                .trackingNumber(effectiveTrackingNumber)
                .dispatchedByUser(dispatcher)
                .koliCount(1)
                .totalWeightKg(new BigDecimal("2.50"))
                .shippingCost(shippingCost != null ? shippingCost : BigDecimal.ZERO)
                .etaDate(LocalDate.now().plusDays(2))
                .status("DISPATCHED")
                .dispatchedAt(LocalDateTime.now())
                .build();

        for (OrderItem oi : order.getItems()) {
            oi.setQtyShipped(oi.getQtyPacked());
        }
        order.setStatus("IN_TRANSIT");
        orderRepository.save(order);

        return ShipmentResponse.from(shipmentRepository.save(shipment));
    }
}
