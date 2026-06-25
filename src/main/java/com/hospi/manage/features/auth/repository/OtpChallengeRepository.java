package com.hospi.manage.features.auth.repository;

import com.hospi.manage.features.auth.entity.OtpChallenge;
import com.hospi.manage.features.auth.enums.OtpType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for {@link OtpChallenge} entity — provides OTP lookup, verification, and cleanup.
 */
public interface OtpChallengeRepository extends JpaRepository<OtpChallenge, Long> {

    /**
     * Return the newest unverified OTP challenge for the given email and type.
     */
    Optional<OtpChallenge> findTopByEmailAndTypeAndVerifiedFalseOrderByCreatedAtDesc(
            String email,
            OtpType otpType
    );

    /**
     * Return the newest verified OTP challenge for the given email and type.
     */
    Optional<OtpChallenge> findTopByEmailAndTypeAndVerifiedTrueOrderByCreatedAtDesc(String email, OtpType type);

    Optional<OtpChallenge> findByToken(String token);

    void deleteByEmail(String email);
}
