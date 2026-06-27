package com.hospi.manage.features.account.entity;

import com.hospi.manage.features.account.enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * JPA entity representing a staff account with authentication and role information.
 */
@Entity
@Table(name = "staffs")
@Getter
@Setter
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hotel_id")
    private Long hotelId;

    @Column(name = "full_name", length = 100)
    private String fullName;

    @Column(name = "email", unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 50)
    private Role role;

    @Column(name = "active")
    private boolean active = true;

    /**
     * Set the creation timestamp before persisting.
     */
    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
