package com.bankjatim.jims.dto;

import com.bankjatim.jims.domain.Role;

import java.time.LocalDateTime;

public record RoleResponse(
        Long id,
        String code,
        String name,
        String description,
        Boolean systemRole,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static RoleResponse from(Role role) {
        return new RoleResponse(
                role.getId(),
                role.getCode(),
                role.getName(),
                role.getDescription(),
                Boolean.TRUE.equals(role.getSystemRole()),
                role.getCreatedAt(),
                role.getUpdatedAt()
        );
    }
}
