package com.bankjatim.jims.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record OrderRequest(
        @NotNull Long organizationId,
        String priority,
        LocalDate requiredDate,
        String notes,
        @NotEmpty List<ItemRequest> items
) {
    public record ItemRequest(
            @NotNull Long itemId,
            @NotNull Integer qty
    ) {
    }
}
