package com.hospi.manage.features.auth.service;

import com.hospi.manage.core.security.hash.Sha256HashingService;
import com.hospi.manage.features.auth.entity.OtpChallenge;
import com.hospi.manage.features.auth.enums.OtpType;
import com.hospi.manage.features.auth.repository.OtpChallengeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/** Business logic for OTP creation, verification, and token issuance for email-based authentication. */
@Service
@RequiredArgsConstructor
public class OtpService {
    private final OtpChallengeRepository otpChallengeRepository;
    private final Sha256HashingService hashingService;

    private static final int OTP_EXPIRED_MINUTES = 10;
    private static final int OTP_COOLDOWN_SECONDS = 60;
    private static final int MAX_ATTEMPTS = 5;

    /** Create and persist a new OTP challenge for the given email. Returns null if the cooldown period is still active. */
    @Transactional
    public String createOtp(String email, OtpType type) {
        boolean recentOtp = otpChallengeRepository
                .findTopByEmailAndTypeAndVerifiedFalseOrderByCreatedAtDesc(email, type)
                .filter(ch -> ch.getCreatedAt().isAfter(LocalDateTime.now().minusSeconds(OTP_COOLDOWN_SECONDS)))
                .isPresent();

        if (recentOtp) {
            return null;
        }

        String rawOtp = OtpGenerator.generate6DigitOtp();

        OtpChallenge challenge = new OtpChallenge();
        challenge.setEmail(email);
        challenge.setOtpHash(hashingService.hash(rawOtp));
        challenge.setExpiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRED_MINUTES));
        challenge.setType(type);

        otpChallengeRepository.save(challenge);
        return rawOtp;
    }

    /** Verify an OTP against the most recent challenge. */
    @Transactional
    public boolean verifyOtp(String email, String inputOtp, OtpType type) {
        var challenge = otpChallengeRepository
                .findTopByEmailAndTypeAndVerifiedFalseOrderByCreatedAtDesc(email, type)
                .orElse(null);

        if (challenge == null || challenge.isOtpExpired()) return false;

        if (challenge.getAttempts() >= MAX_ATTEMPTS) return false;

        if (!hashingService.matches(inputOtp, challenge.getOtpHash())) {
            challenge.setAttempts(challenge.getAttempts() + 1);
            otpChallengeRepository.save(challenge);
            return false;
        }

        challenge.setVerified(true);
        otpChallengeRepository.save(challenge);
        return true;
    }

    /** Invalidate the most recent unverified OTP for the given email. */
    @Transactional
    public void invalidateOtp(String email, OtpType type) {
        otpChallengeRepository
                .findTopByEmailAndTypeAndVerifiedFalseOrderByCreatedAtDesc(email, type)
                .ifPresent(challenge -> {
                    challenge.setExpiresAt(LocalDateTime.now());
                    otpChallengeRepository.save(challenge);
                });
    }

    /** Issue a bearer token tied to a verified OTP. */
    @Transactional
    public String issueToken(String email, OtpType type) {
        OtpChallenge challenge = otpChallengeRepository
                .findTopByEmailAndTypeAndVerifiedTrueOrderByCreatedAtDesc(email, type)
                .orElseThrow(() -> new IllegalStateException("No verified OTP found"));

        String token = java.util.UUID.randomUUID().toString();
        challenge.setToken(token);
        challenge.setTokenExpiresAt(LocalDateTime.now().plusMinutes(15));
        challenge.setTokenUsed(false);
        otpChallengeRepository.save(challenge);
        return token;
    }

    /** Check whether the given token is valid and not expired. */
    public boolean isValidToken(String token) {
        return otpChallengeRepository.findByToken(token)
                .map(OtpChallenge::isTokenValid)
                .orElse(false);
    }

    /** Return the email associated with a valid token. */
    public String getEmailByToken(String token) {
        return otpChallengeRepository.findByToken(token)
                .filter(OtpChallenge::isTokenValid)
                .map(OtpChallenge::getEmail)
                .orElseThrow(() -> new IllegalStateException("Invalid or expired reset token"));
    }

    /** Mark the given token as used. */
    @Transactional
    public void invalidateToken(String token) {
        otpChallengeRepository.findByToken(token).ifPresent(challenge -> {
            challenge.setTokenUsed(true);
            otpChallengeRepository.save(challenge);
        });
    }
}
