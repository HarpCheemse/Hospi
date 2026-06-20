package com.hospi.manage.features.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hotel_id", nullable = false)
    private Long hotelId;

    @Column(name = "role_id")
    private Short roleId;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "body", length = 1000)
    private String body;

    @Column(name = "related_module", length = 50)
    private String relatedModule;

    @Column(name = "related_id", length = 50)
    private String relatedId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}