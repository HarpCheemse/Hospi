package com.hospi.manage.features.audit.repository;

import com.hospi.manage.features.audit.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Repository for audit log entries. */
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /** Return the most recent audit entries. */
    List<AuditLog> findTop20ByOrderByCreatedAtDesc();
}
