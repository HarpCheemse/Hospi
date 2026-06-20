package com.hospi.manage.features.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "staff_notification_views")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffNotificationView {

    @Id
    @Column(name = "staff_id")
    private Long staffId;

    @Column(name = "last_viewed_at", nullable = false)
    private LocalDateTime lastViewedAt;
}