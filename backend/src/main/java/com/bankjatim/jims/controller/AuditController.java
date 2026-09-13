package com.bankjatim.jims.controller;

import com.bankjatim.jims.common.ApiResponse;
import com.bankjatim.jims.dto.AuditLogResponse;
import com.bankjatim.jims.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/audit")
@RequiredArgsConstructor
@Tag(name = "Audit Trail", description = "Endpoint audit trail aktivitas sistem")
public class AuditController {

    private final AuditLogService auditLogService;

    @GetMapping("/logs")
    @Operation(summary = "Daftar Log Audit")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getAuditLogs(
            @RequestParam(required = false) String module) {
        return ResponseEntity.ok(ApiResponse.ok(auditLogService.getAuditLogs(module)));
    }
}
