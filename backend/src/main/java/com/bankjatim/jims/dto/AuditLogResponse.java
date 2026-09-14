package com.bankjatim.jims.dto;

import com.bankjatim.jims.domain.AuditLog;

import java.time.LocalDateTime;

public record AuditLogResponse(
        Long id,
        LocalDateTime timestamp,
        String actor,
        String role,
        String module,
        String action,
        String ref,
        String ip,
        String details
) {
    public static AuditLogResponse from(AuditLog log) {
        String actor = log.getUser() != null ? log.getUser().getName() : "Sistem";
        String role = log.getUser() != null && log.getUser().getRole() != null ? log.getUser().getRole().getCode() : "-";
        String module = log.getAuditableType() != null ? log.getAuditableType() : "-";
        String ref = module + "-" + (log.getAuditableId() != null ? log.getAuditableId() : "-");
        String details = log.getNewValues() != null ? log.getNewValues() : log.getOldValues();

        return new AuditLogResponse(
                log.getId(),
                log.getCreatedAt(),
                actor,
                role,
                module,
                log.getAction(),
                ref,
                log.getIpAddress(),
                details != null ? details : "-"
        );
    }
}
