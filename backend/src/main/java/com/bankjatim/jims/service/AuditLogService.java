package com.bankjatim.jims.service;

import com.bankjatim.jims.dto.AuditLogResponse;
import com.bankjatim.jims.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAuditLogs(String module) {
        if (module != null && !module.isBlank()) {
            return auditLogRepository.findByAuditableTypeOrderByCreatedAtDesc(module).stream()
                    .map(AuditLogResponse::from)
                    .toList();
        }

        return auditLogRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                .map(AuditLogResponse::from)
                .toList();
    }
}
