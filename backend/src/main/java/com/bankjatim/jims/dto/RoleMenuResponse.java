package com.bankjatim.jims.dto;

import com.bankjatim.jims.domain.RoleMenuPermission;

public record RoleMenuResponse(String roleCode, String menuCode) {
    public static RoleMenuResponse from(RoleMenuPermission permission) {
        return new RoleMenuResponse(permission.getRole().getCode(), permission.getMenu().getCode());
    }
}
