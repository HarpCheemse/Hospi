package com.hospi.manage.features.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import com.hospi.manage.features.account.enums.Role;
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
    @Column(name = "id")
    private Long id;


    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 50)
    private Role role;

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