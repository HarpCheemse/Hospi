package com.hospi.manage.features.auth.entity;

import com.hospi.manage.features.auth.enums.OtpType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "otp_challenges")
@Getter
@Setter
public class OtpChallenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255, nullable = false)
    private String email;

    @Column(name = "otp_hash", length = 255, nullable = false)
    private String otpHash;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private Boolean verified = false;

    @Column(nullable = false)
    private Integer attempts = 0;

    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = false)
    private OtpType type;

    @Column(name = "token", length = 36)
    private String token;

    @Column(name = "token_expires_at")
    private LocalDateTime tokenExpiresAt;

    @Column(name = "token_used", nullable = false)
    private Boolean tokenUsed = false;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }

    public boolean isOtpExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isTokenExpired() {
        return tokenExpiresAt == null || LocalDateTime.now().isAfter(tokenExpiresAt);
    }

    public boolean isTokenValid() {
        return token != null && !tokenUsed && !isTokenExpired();
    }
}