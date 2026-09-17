package com.bankjatim.jims.dto;

import com.bankjatim.jims.domain.Warehouse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseResponse {

    private Long id;
    private String code;
    private String name;
    private String type;
    private String address;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("organization_id")
    private Long organizationId;

    private OrganizationResponse.OrganizationSummary organization;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static WarehouseResponse from(Warehouse wh) {
        if (wh == null) {
            return null;
        }

        OrganizationResponse.OrganizationSummary orgSummary = null;
        if (wh.getOrganization() != null) {
            orgSummary = OrganizationResponse.OrganizationSummary.builder()
                    .id(wh.getOrganization().getId())
                    .code(wh.getOrganization().getCode())
                    .name(wh.getOrganization().getName())
                    .type(wh.getOrganization().getType())
                    .build();
        }

        return WarehouseResponse.builder()
                .id(wh.getId())
                .code(wh.getCode())
                .name(wh.getName())
                .type(wh.getType())
                .address(wh.getAddress())
                .isActive(wh.getIsActive())
                .organizationId(wh.getOrganization() != null ? wh.getOrganization().getId() : null)
                .organization(orgSummary)
                .createdAt(wh.getCreatedAt())
                .updatedAt(wh.getUpdatedAt())
                .build();
    }
}
