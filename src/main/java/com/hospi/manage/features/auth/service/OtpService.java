package com.hospi.manage.features.auth.service;

import com.hospi.manage.core.security.hash.Sha256HashingService;
import com.hospi.manage.features.auth.entity.OtpChallenge;
import com.hospi.manage.features.auth.enums.OtpType;
import com.hospi.manage.features.auth.repository.OtpChallengeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class OtpService {
    private final OtpChallengeRepository otpChallengeRepository;
    private final Sha256HashingService hashingService;

    public OtpService(OtpChallengeRepository otpChallengeRepository, Sha256HashingService hashingService) {
        this.otpChallengeRepository = otpChallengeRepository;
        this.hashingService = hashingService;
    }

    public String createOtp(String email, OtpType type) {
        String rawOtp = OtpGenerator.generate6DigitOtp();

        OtpChallenge challenge = new OtpChallenge();
        challenge.setEmail(email);
        challenge.setOtpHash(hashingService.hash(rawOtp));
        challenge.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        challenge.setType(type);

        otpChallengeRepository.save(challenge);
        return rawOtp;
    }

    public boolean verifyOtp(String email, String inputOtp, OtpType type) {
        OtpChallenge challenge = otpChallengeRepository
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

    public String issueResetToken(String email, OtpType type) {
        OtpChallenge challenge = otpChallengeRepository
                .findTopByEmailAndTypeAndVerifiedTrueOrderByCreatedAtDesc(email,
                        type)
                .orElseThrow(() -> new IllegalStateException("No verified OTP found"));

        String token = UUID.randomUUID().toString();
        challenge.setResetToken(token);
        challenge.setResetTokenExpiresAt(LocalDateTime.now().plusMinutes(15));
        challenge.setResetTokenUsed(false);
        otpChallengeRepository.save(challenge);
        return token;
    }

    public boolean isValidResetToken(String token) {
        return otpChallengeRepository.findByResetToken(token)
                .map(OtpChallenge::isResetTokenValid)
                .orElse(false);
    }

    public String getEmailByResetToken(String token) {
        return otpChallengeRepository.findByResetToken(token)
                .filter(OtpChallenge::isResetTokenValid)
                .map(OtpChallenge::getEmail)
                .orElseThrow(() -> new IllegalStateException("Invalid or expired reset token"));
    }

    public void invalidateResetToken(String token) {
        otpChallengeRepository.findByResetToken(token).ifPresent(challenge -> {
            challenge.setResetTokenUsed(true);
            otpChallengeRepository.save(challenge);
        });
    }
}