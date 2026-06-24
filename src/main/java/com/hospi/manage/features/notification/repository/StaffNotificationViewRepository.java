package com.hospi.manage.features.notification.repository;

import com.hospi.manage.features.notification.entity.StaffNotificationView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface StaffNotificationViewRepository extends JpaRepository<StaffNotificationView, Long> {

    Optional<StaffNotificationView> findByStaffId(Long staffId);

    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO staff_notification_views (staff_id, last_viewed_at)
        VALUES (:staffId, :now)
        ON CONFLICT (staff_id) DO UPDATE SET last_viewed_at = :now
        """, nativeQuery = true)
    void upsertLastViewedAt(@Param("staffId") Long staffId, @Param("now") LocalDateTime now);
}