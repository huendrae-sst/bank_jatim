package com.bankjatim.jims.dto;

import com.bankjatim.jims.domain.WarehousePicking;

import java.time.LocalDateTime;

public record WarehousePickingResponse(
        Long id,
        String pickingNumber,
        WarehouseQueueResponse order,
        PurchaseOrderResponse.WarehouseSummary warehouse,
        OrderResponse.UserSummary pickedByUser,
        String status,
        LocalDateTime pickedAt
) {
    public static WarehousePickingResponse from(WarehousePicking picking) {
        return new WarehousePickingResponse(
                picking.getId(),
                picking.getPickingNumber(),
                WarehouseQueueResponse.from(picking.getOrder()),
                PurchaseOrderResponse.WarehouseSummary.from(picking.getWarehouse()),
                OrderResponse.UserSummary.from(picking.getPickedByUser()),
                picking.getStatus(),
                picking.getPickedAt()
        );
    }
}
