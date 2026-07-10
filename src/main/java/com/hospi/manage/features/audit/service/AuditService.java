package com.hospi.manage.features.audit.service;

import com.hospi.manage.features.audit.entity.AuditLog;
import com.hospi.manage.features.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/** Service for recording and retrieving audit log entries. */
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    /** Record an audit entry. */
    public void log(Long staffId, String staffName, String action, String entityType, Long entityId, String details) {
        var log = new AuditLog();
        log.setStaffId(staffId);
        log.setStaffName(staffName);
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setDetails(details);
        auditLogRepository.save(log);
    }

    /** Return the 20 most recent audit entries. */
    public List<AuditLog> getRecent() {
        return auditLogRepository.findTop20ByOrderByCreatedAtDesc();
    }
}
