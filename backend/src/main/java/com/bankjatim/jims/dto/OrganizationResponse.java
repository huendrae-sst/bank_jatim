package com.bankjatim.jims.dto;

import com.bankjatim.jims.domain.Organization;
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
public class OrganizationResponse {

    private Long id;
    private String code;
    private String name;
    private String type;

    @JsonProperty("parent_id")
    private Long parentId;

    private OrganizationSummary parent;
    private RegionSummary region;
    private String address;
    private String city;
    private String phone;

    @JsonProperty("cost_center_code")
    private String costCenterCode;

    @JsonProperty("is_active")
    private Boolean isActive;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrganizationSummary {
        private Long id;
        private String code;
        private String name;
        private String type;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegionSummary {
        private Long id;
        private String code;
        private String name;
    }

    public static OrganizationResponse from(Organization org) {
        if (org == null) {
            return null;
        }

        OrganizationSummary parentSummary = null;
        if (org.getParent() != null) {
            parentSummary = OrganizationSummary.builder()
                    .id(org.getParent().getId())
                    .code(org.getParent().getCode())
                    .name(org.getParent().getName())
                    .type(org.getParent().getType())
                    .build();
        }

        RegionSummary regionSummary = null;
        if (org.getRegion() != null) {
            regionSummary = RegionSummary.builder()
                    .id(org.getRegion().getId())
                    .code(org.getRegion().getCode())
                    .name(org.getRegion().getName())
                    .build();
        }

        return OrganizationResponse.builder()
                .id(org.getId())
                .code(org.getCode())
                .name(org.getName())
                .type(org.getType())
                .parentId(org.getParent() != null ? org.getParent().getId() : null)
                .parent(parentSummary)
                .region(regionSummary)
                .address(org.getAddress())
                .city(org.getCity())
                .phone(org.getPhone())
                .costCenterCode(org.getCostCenterCode())
                .isActive(org.getIsActive())
                .createdAt(org.getCreatedAt())
                .updatedAt(org.getUpdatedAt())
                .build();
    }
}
