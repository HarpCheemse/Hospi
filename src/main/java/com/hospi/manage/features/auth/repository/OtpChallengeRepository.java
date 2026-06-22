package com.hospi.manage.features.auth.repository;

import com.hospi.manage.features.auth.entity.OtpChallenge;
import com.hospi.manage.features.auth.enums.OtpType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpChallengeRepository extends JpaRepository<OtpChallenge, Long> {
    // this get the newest otp challenge
    Optional<OtpChallenge> findTopByEmailAndTypeAndVerifiedFalseOrderByCreatedAtDesc(
            String email,
            OtpType otpType
    );

    Optional<OtpChallenge> findTopByEmailAndTypeAndVerifiedTrueOrderByCreatedAtDesc(String email, OtpType type);

    Optional<OtpChallenge> findByToken(String token);

    void deleteByEmail(String email);
}
