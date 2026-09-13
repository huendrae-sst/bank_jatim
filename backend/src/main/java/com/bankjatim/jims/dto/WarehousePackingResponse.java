package com.bankjatim.jims.dto;

import com.bankjatim.jims.domain.WarehousePacking;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WarehousePackingResponse(
        Long id,
        String packingNumber,
        WarehouseQueueResponse order,
        PurchaseOrderResponse.WarehouseSummary warehouse,
        OrderResponse.UserSummary packedByUser,
        Integer koliCount,
        BigDecimal totalWeightKg,
        String dimensionsCm,
        String status,
        LocalDateTime packedAt
) {
    public static WarehousePackingResponse from(WarehousePacking packing) {
        return new WarehousePackingResponse(
                packing.getId(),
                packing.getPackingNumber(),
                WarehouseQueueResponse.from(packing.getOrder()),
                PurchaseOrderResponse.WarehouseSummary.from(packing.getWarehouse()),
                OrderResponse.UserSummary.from(packing.getPackedByUser()),
                packing.getKoliCount(),
                packing.getTotalWeightKg(),
                packing.getDimensionsCm(),
                packing.getStatus(),
                packing.getPackedAt()
        );
    }
}
