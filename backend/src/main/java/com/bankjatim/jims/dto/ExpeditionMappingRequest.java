package com.bankjatim.jims.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ExpeditionMappingRequest(
        @NotNull Long destinationOrganizationId,
        @NotNull Long courierId,
        String serviceType,
        @NotNull @Min(1) Integer estimatedLeadDays
) {
}
