package com.bankjatim.jims.dto;

import com.bankjatim.jims.domain.Courier;
import com.bankjatim.jims.domain.Shipment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ShipmentResponse(
        Long id,
        String manifestNumber,
        OrderResponse order,
        PurchaseOrderResponse.WarehouseSummary originWarehouse,
        OrderResponse.OrganizationSummary destinationOrganization,
        CourierSummary courier,
        String serviceType,
        String trackingNumber,
        OrderResponse.UserSummary dispatchedByUser,
        Integer koliCount,
        BigDecimal totalWeightKg,
        BigDecimal shippingCost,
        LocalDate etaDate,
        String status,
        LocalDateTime dispatchedAt,
        LocalDateTime deliveredAt
) {
    public static ShipmentResponse from(Shipment shipment) {
        return new ShipmentResponse(
                shipment.getId(),
                shipment.getManifestNumber(),
                shipment.getOrder() != null ? OrderResponse.from(shipment.getOrder()) : null,
                PurchaseOrderResponse.WarehouseSummary.from(shipment.getOriginWarehouse()),
                OrderResponse.OrganizationSummary.from(shipment.getDestinationOrganization()),
                CourierSummary.from(shipment.getCourier()),
                shipment.getServiceType(),
                shipment.getTrackingNumber(),
                OrderResponse.UserSummary.from(shipment.getDispatchedByUser()),
                shipment.getKoliCount(),
                shipment.getTotalWeightKg(),
                shipment.getShippingCost(),
                shipment.getEtaDate(),
                shipment.getStatus(),
                shipment.getDispatchedAt(),
                shipment.getDeliveredAt()
        );
    }

    public record CourierSummary(Long id, String code, String name, Integer slaDays) {
        static CourierSummary from(Courier courier) {
            if (courier == null) {
                return null;
            }
            return new CourierSummary(courier.getId(), courier.getCode(), courier.getName(), courier.getSlaDays());
        }
    }
}
