package com.hospi.manage.features.notification.repository;

import com.hospi.manage.features.admin.account.entity.Account;
import com.hospi.manage.features.notification.entity.Notification;
import com.hospi.manage.features.notification.entity.StaffNotificationView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("""
        SELECT n FROM Notification n
        JOIN Account a ON a.hotelId = n.hotelId
        LEFT JOIN StaffNotificationView v ON v.staffId = a.id
        WHERE a.id = :staffId
          AND n.roleId = CASE a.role
              WHEN 'ADMIN' THEN 0
              WHEN 'LEADER' THEN 1
              WHEN 'RECEPTIONIST' THEN 2
              WHEN 'MANAGER' THEN 3
          END
          AND n.createdAt > COALESCE(v.lastViewedAt, '1970-01-01')
        ORDER BY n.createdAt DESC
        """)
    Page<Notification> findUnreadByStaffId(@Param("staffId") Long staffId, Pageable pageable);

    @Query("""
        SELECT COUNT(n) FROM Notification n
        JOIN Account a ON a.hotelId = n.hotelId
        LEFT JOIN StaffNotificationView v ON v.staffId = a.id
        WHERE a.id = :staffId
          AND n.roleId = CASE a.role
              WHEN 'ADMIN' THEN 0
              WHEN 'LEADER' THEN 1
              WHEN 'RECEPTIONIST' THEN 2
              WHEN 'MANAGER' THEN 3
          END
          AND n.createdAt > COALESCE(v.lastViewedAt, '1970-01-01')
        """)
    int countUnreadByStaffId(@Param("staffId") Long staffId);

    @Query("""
        SELECT n FROM Notification n
        JOIN Account a ON a.hotelId = n.hotelId
        WHERE a.id = :staffId
          AND n.roleId = CASE a.role
              WHEN 'ADMIN' THEN 0
              WHEN 'LEADER' THEN 1
              WHEN 'RECEPTIONIST' THEN 2
              WHEN 'MANAGER' THEN 3
          END
        ORDER BY n.createdAt DESC
        """)
    Page<Notification> findAllByStaffId(@Param("staffId") Long staffId, Pageable pageable);
}