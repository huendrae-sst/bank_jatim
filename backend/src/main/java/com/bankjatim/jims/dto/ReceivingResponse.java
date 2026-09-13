package com.bankjatim.jims.dto;

import com.bankjatim.jims.domain.Receiving;

import java.time.LocalDate;

public record ReceivingResponse(
        Long id,
        String receivingNumber,
        ShipmentResponse shipment,
        OrderResponse order,
        OrderResponse.OrganizationSummary organization,
        PurchaseOrderResponse.WarehouseSummary warehouse,
        OrderResponse.UserSummary receivedByUser,
        LocalDate receiptDate,
        String status,
        String podSignature,
        String notes
) {
    public static ReceivingResponse from(Receiving receiving) {
        return new ReceivingResponse(
                receiving.getId(),
                receiving.getReceivingNumber(),
                ShipmentResponse.from(receiving.getShipment()),
                receiving.getOrder() != null ? OrderResponse.from(receiving.getOrder()) : null,
                OrderResponse.OrganizationSummary.from(receiving.getOrganization()),
                PurchaseOrderResponse.WarehouseSummary.from(receiving.getWarehouse()),
                OrderResponse.UserSummary.from(receiving.getReceivedByUser()),
                receiving.getReceiptDate(),
                receiving.getStatus(),
                receiving.getPodSignature(),
                receiving.getNotes()
        );
    }
}
