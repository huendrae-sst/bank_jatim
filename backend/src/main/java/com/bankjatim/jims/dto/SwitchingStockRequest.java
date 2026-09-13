package com.bankjatim.jims.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SwitchingStockRequest(
        Long orderId,
        @NotNull Long sourceOrganizationId,
        @NotNull Long sourceWarehouseId,
        @NotNull Long destinationOrganizationId,
        @NotNull Long destinationWarehouseId,
        String recommendationReason,
        @Valid @NotEmpty List<ItemRequest> items
) {
    public record ItemRequest(
            @NotNull Long itemId,
            @NotNull @Min(1) Integer qty,
            String notes
    ) {
    }
}
