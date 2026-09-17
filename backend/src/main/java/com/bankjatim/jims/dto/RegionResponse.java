package com.bankjatim.jims.dto;

import com.bankjatim.jims.domain.Organization;
import com.bankjatim.jims.domain.Region;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegionResponse {

    private Long id;
    private String code;
    private String name;
    private String description;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer branchCount;
    private List<BranchSummary> branches;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BranchSummary {
        private Long id;
        private String code;
        private String name;
        private String type;
        private String city;
    }

    public static RegionResponse from(Region region, List<Organization> organizations) {
        List<BranchSummary> branchSummaries = organizations != null
                ? organizations.stream()
                .map(org -> BranchSummary.builder()
                        .id(org.getId())
                        .code(org.getCode())
                        .name(org.getName())
                        .type(org.getType())
                        .city(org.getCity())
                        .build())
                .toList()
                : List.of();

        return RegionResponse.builder()
                .id(region.getId())
                .code(region.getCode())
                .name(region.getName())
                .description(region.getDescription())
                .isActive(region.getIsActive())
                .createdAt(region.getCreatedAt())
                .updatedAt(region.getUpdatedAt())
                .branchCount(branchSummaries.size())
                .branches(branchSummaries)
                .build();
    }
}
