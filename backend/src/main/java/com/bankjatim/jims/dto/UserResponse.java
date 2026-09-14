package com.bankjatim.jims.dto;

import com.bankjatim.jims.domain.Organization;
import com.bankjatim.jims.domain.User;
import com.bankjatim.jims.domain.Warehouse;

import java.math.BigDecimal;

public record UserResponse(
        Long id,
        String name,
        String email,
        String nip,
        String role,
        BigDecimal approvalLimit,
        String phone,
        Boolean isActive,
        OrganizationSummary organization,
        WarehouseSummary warehouse
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getNip(),
                user.getRole().getCode(),
                user.getApprovalLimit(),
                user.getPhone(),
                user.getIsActive(),
                OrganizationSummary.from(user.getOrganization()),
                WarehouseSummary.from(user.getWarehouse())
        );
    }

    public record OrganizationSummary(Long id, String code, String name) {
        static OrganizationSummary from(Organization organization) {
            if (organization == null) {
                return null;
            }
            return new OrganizationSummary(organization.getId(), organization.getCode(), organization.getName());
        }
    }

    public record WarehouseSummary(Long id, String code, String name) {
        static WarehouseSummary from(Warehouse warehouse) {
            if (warehouse == null) {
                return null;
            }
            return new WarehouseSummary(warehouse.getId(), warehouse.getCode(), warehouse.getName());
        }
    }
}
