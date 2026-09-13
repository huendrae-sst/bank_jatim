package com.bankjatim.jims.repository;

import com.bankjatim.jims.domain.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByAuditableTypeOrderByCreatedAtDesc(String auditableType);
}
