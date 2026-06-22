package com.hospi.manage.features.auth.service;

import com.hospi.manage.core.security.hash.Sha256HashingService;
import com.hospi.manage.features.auth.entity.OtpChallenge;
import com.hospi.manage.features.auth.enums.OtpType;
import com.hospi.manage.features.auth.repository.OtpChallengeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OtpService {
    private final OtpChallengeRepository otpChallengeRepository;
    private final Sha256HashingService hashingService;

    private static int OTP_EXPIRED_MINUTES = 10;

    @Transactional
    public String createOtp(String email, OtpType type) {
        String rawOtp = OtpGenerator.generate6DigitOtp();

        OtpChallenge challenge = new OtpChallenge();
        challenge.setEmail(email);
        challenge.setOtpHash(hashingService.hash(rawOtp));
        challenge.setExpiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRED_MINUTES));
        challenge.setType(type);

        otpChallengeRepository.save(challenge);
        return rawOtp;
    }

    @Transactional
    public boolean verifyOtp(String email, String inputOtp, OtpType type) {
        var challenge = otpChallengeRepository
                .findTopByEmailAndTypeAndVerifiedFalseOrderByCreatedAtDesc(email,
                        type)
                .orElse(null);

        if (challenge == null || challenge.isOtpExpired()) return false;

        if (!hashingService.matches(inputOtp,
                challenge.getOtpHash())) return false;

        challenge.setVerified(true);
        otpChallengeRepository.save(challenge);
        return true;
    }

    @Transactional
    public String issueToken(String email, OtpType type) {
        OtpChallenge challenge = otpChallengeRepository
                .findTopByEmailAndTypeAndVerifiedTrueOrderByCreatedAtDesc(email,
                        type)
                .orElseThrow(() -> new IllegalStateException("No verified OTP found"));

        String token = UUID.randomUUID().toString();
        challenge.setToken(token);
        challenge.setTokenExpiresAt(LocalDateTime.now().plusMinutes(15));
        challenge.setTokenUsed(false);
        otpChallengeRepository.save(challenge);
        return token;
    }

    public boolean isValidToken(String token) {
        return otpChallengeRepository.findByToken(token)
                .map(OtpChallenge::isTokenValid)
                .orElse(false);
    }

    public String getEmailByToken(String token) {
        return otpChallengeRepository.findByToken(token)
                .filter(OtpChallenge::isTokenValid)
                .map(OtpChallenge::getEmail)
                .orElseThrow(() -> new IllegalStateException("Invalid or expired reset token"));
    }

    @Transactional
    public void invalidateToken(String token) {
        otpChallengeRepository.findByToken(token).ifPresent(challenge -> {
            challenge.setTokenUsed(true);
            otpChallengeRepository.save(challenge);
        });
    }
}