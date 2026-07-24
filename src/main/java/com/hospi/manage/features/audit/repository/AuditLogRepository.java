package com.hospi.manage.features.audit.repository;

import com.hospi.manage.features.audit.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/** Repository for audit log entries. */
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /** Return the most recent audit entries. */
    List<AuditLog> findTop20ByOrderByCreatedAtDesc();

    /** Paginated audit log query, newest first. */
    Page<AuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /** Return filtered, paginated audit log entries. Uses COALESCE to avoid PostgreSQL null-type issues. */
    @Query("SELECT a FROM AuditLog a WHERE " +
           "a.action = COALESCE(:action, a.action) AND " +
           "a.createdAt >= COALESCE(:startDate, a.createdAt) AND " +
           "a.createdAt <= COALESCE(:endDate, a.createdAt) " +
           "ORDER BY a.createdAt DESC")
    Page<AuditLog> findFiltered(@Param("action") String action,
                                 @Param("startDate") LocalDateTime startDate,
                                 @Param("endDate") LocalDateTime endDate,
                                 Pageable pageable);
}
