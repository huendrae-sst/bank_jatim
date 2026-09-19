package com.bankjatim.jims.dto;

import java.util.List;

public record CreateProductionOrderRequest(
        Long warehouseId,
        Long embossFileId,
        List<Long> orderIds,
        List<ProductionOrderItemInput> items,
        String notes
) {
    public record ProductionOrderItemInput(
            Long itemId,
            Integer qtyPlanned
    ) {}
}
