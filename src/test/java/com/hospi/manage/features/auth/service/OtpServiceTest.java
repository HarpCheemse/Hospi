package com.hospi.manage.features.auth.service;

import com.hospi.manage.core.security.hash.Sha256HashingService;
import com.hospi.manage.features.auth.entity.OtpChallenge;
import com.hospi.manage.features.auth.enums.OtpType;
import com.hospi.manage.features.auth.repository.OtpChallengeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    @Mock
    private OtpChallengeRepository otpChallengeRepository;

    @Mock
    private Sha256HashingService hashingService;

    @InjectMocks
    private OtpService otpService;

    private static final String EMAIL = "test@test.com";
    private static final OtpType TYPE = OtpType.EMAIL_VERIFY;

    // -- createOtp --

    @Test
    void shouldReturnOtp_whenNoRecentOtp() {
        when(otpChallengeRepository.findTopByEmailAndTypeAndVerifiedFalseOrderByCreatedAtDesc(EMAIL, TYPE))
                .thenReturn(Optional.empty());
        when(hashingService.hash(anyString())).thenReturn("hashed");

        String result = otpService.createOtp(EMAIL, TYPE);

        assertNotNull(result);
        assertEquals(6, result.length());
        assertTrue(result.matches("\\d{6}"));
        verify(hashingService).hash(result);
        verify(otpChallengeRepository).save(any(OtpChallenge.class));
    }

    @Test
    void shouldReturnNull_whenCooldownActive() {
        OtpChallenge challenge = mock(OtpChallenge.class);
        when(challenge.getCreatedAt()).thenReturn(LocalDateTime.now().minusSeconds(30));
        when(otpChallengeRepository.findTopByEmailAndTypeAndVerifiedFalseOrderByCreatedAtDesc(EMAIL, TYPE))
                .thenReturn(Optional.of(challenge));

        String result = otpService.createOtp(EMAIL, TYPE);

        assertNull(result);
        verify(otpChallengeRepository, never()).save(any());
    }

    @Test
    void shouldReturnOtp_whenRecentOtpButExpiredCooldown() {
        OtpChallenge challenge = mock(OtpChallenge.class);
        when(challenge.getCreatedAt()).thenReturn(LocalDateTime.now().minusSeconds(120));
        when(otpChallengeRepository.findTopByEmailAndTypeAndVerifiedFalseOrderByCreatedAtDesc(EMAIL, TYPE))
                .thenReturn(Optional.of(challenge));
        when(hashingService.hash(anyString())).thenReturn("hashed");

        String result = otpService.createOtp(EMAIL, TYPE);

        assertNotNull(result);
        assertEquals(6, result.length());
        verify(otpChallengeRepository).save(any(OtpChallenge.class));
    }

    // -- verifyOtp --

    @Test
    void shouldReturnTrue_whenOtpMatches() {
        OtpChallenge challenge = mock(OtpChallenge.class);
        when(otpChallengeRepository.findTopByEmailAndTypeAndVerifiedFalseOrderByCreatedAtDesc(EMAIL, TYPE))
                .thenReturn(Optional.of(challenge));
        when(challenge.isOtpExpired()).thenReturn(false);
        when(challenge.getAttempts()).thenReturn(0);
        when(challenge.getOtpHash()).thenReturn("hash");
        when(hashingService.matches("123456", "hash")).thenReturn(true);

        boolean result = otpService.verifyOtp(EMAIL, "123456", TYPE);

        assertTrue(result);
        verify(challenge).setVerified(true);
        verify(otpChallengeRepository).save(challenge);
    }

    @Test
    void shouldReturnFalse_whenNoChallenge() {
        when(otpChallengeRepository.findTopByEmailAndTypeAndVerifiedFalseOrderByCreatedAtDesc(EMAIL, TYPE))
                .thenReturn(Optional.empty());

        boolean result = otpService.verifyOtp(EMAIL, "123456", TYPE);

        assertFalse(result);
        verify(otpChallengeRepository, never()).save(any());
    }

    @Test
    void shouldReturnFalse_whenChallengeExpired() {
        OtpChallenge challenge = mock(OtpChallenge.class);
        when(otpChallengeRepository.findTopByEmailAndTypeAndVerifiedFalseOrderByCreatedAtDesc(EMAIL, TYPE))
                .thenReturn(Optional.of(challenge));
        when(challenge.isOtpExpired()).thenReturn(true);

        boolean result = otpService.verifyOtp(EMAIL, "123456", TYPE);

        assertFalse(result);
        verify(otpChallengeRepository, never()).save(any());
    }

    @Test
    void shouldReturnFalse_whenMaxAttemptsReached() {
        OtpChallenge challenge = mock(OtpChallenge.class);
        when(otpChallengeRepository.findTopByEmailAndTypeAndVerifiedFalseOrderByCreatedAtDesc(EMAIL, TYPE))
                .thenReturn(Optional.of(challenge));
        when(challenge.isOtpExpired()).thenReturn(false);
        when(challenge.getAttempts()).thenReturn(5);

        boolean result = otpService.verifyOtp(EMAIL, "123456", TYPE);

        assertFalse(result);
        verify(hashingService, never()).matches(anyString(), anyString());
        verify(otpChallengeRepository, never()).save(any());
    }

    @Test
    void shouldReturnFalse_whenOtpDoesNotMatch_andIncrementAttempts() {
        OtpChallenge challenge = mock(OtpChallenge.class);
        when(otpChallengeRepository.findTopByEmailAndTypeAndVerifiedFalseOrderByCreatedAtDesc(EMAIL, TYPE))
                .thenReturn(Optional.of(challenge));
        when(challenge.isOtpExpired()).thenReturn(false);
        when(challenge.getAttempts()).thenReturn(2);
        when(challenge.getOtpHash()).thenReturn("hash");
        when(hashingService.matches("wrong", "hash")).thenReturn(false);

        boolean result = otpService.verifyOtp(EMAIL, "wrong", TYPE);

        assertFalse(result);
        verify(challenge).setAttempts(3);
        verify(otpChallengeRepository).save(challenge);
    }

    // -- invalidateOtp --

    @Test
    void shouldExpireOtp_whenChallengeExists() {
        OtpChallenge challenge = mock(OtpChallenge.class);
        when(otpChallengeRepository.findTopByEmailAndTypeAndVerifiedFalseOrderByCreatedAtDesc(EMAIL, TYPE))
                .thenReturn(Optional.of(challenge));

        otpService.invalidateOtp(EMAIL, TYPE);

        verify(challenge).setExpiresAt(any(LocalDateTime.class));
        verify(otpChallengeRepository).save(challenge);
    }

    @Test
    void shouldDoNothing_whenNoChallenge() {
        when(otpChallengeRepository.findTopByEmailAndTypeAndVerifiedFalseOrderByCreatedAtDesc(EMAIL, TYPE))
                .thenReturn(Optional.empty());

        otpService.invalidateOtp(EMAIL, TYPE);

        verify(otpChallengeRepository, never()).save(any());
    }

    // -- issueToken --

    @Test
    void shouldReturnToken_whenVerifiedOtpExists() {
        OtpChallenge challenge = mock(OtpChallenge.class);
        when(otpChallengeRepository.findTopByEmailAndTypeAndVerifiedTrueOrderByCreatedAtDesc(EMAIL, TYPE))
                .thenReturn(Optional.of(challenge));

        String result = otpService.issueToken(EMAIL, TYPE);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        verify(challenge).setToken(anyString());
        verify(challenge).setTokenExpiresAt(any(LocalDateTime.class));
        verify(challenge).setTokenUsed(false);
        verify(otpChallengeRepository).save(challenge);
    }

    @Test
    void shouldThrow_whenNoVerifiedOtp() {
        when(otpChallengeRepository.findTopByEmailAndTypeAndVerifiedTrueOrderByCreatedAtDesc(EMAIL, TYPE))
                .thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> otpService.issueToken(EMAIL, TYPE));
        verify(otpChallengeRepository, never()).save(any());
    }

    // -- isValidToken --

    @Test
    void shouldReturnTrue_whenTokenValid() {
        OtpChallenge challenge = mock(OtpChallenge.class);
        when(otpChallengeRepository.findByToken("valid-token")).thenReturn(Optional.of(challenge));
        when(challenge.isTokenValid()).thenReturn(true);

        boolean result = otpService.isValidToken("valid-token");

        assertTrue(result);
    }

    @Test
    void shouldReturnFalse_whenTokenInvalid() {
        when(otpChallengeRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        boolean result = otpService.isValidToken("invalid-token");

        assertFalse(result);
    }

    // -- getEmailByToken --

    @Test
    void shouldReturnEmail_whenTokenValid() {
        OtpChallenge challenge = mock(OtpChallenge.class);
        when(otpChallengeRepository.findByToken("valid-token")).thenReturn(Optional.of(challenge));
        when(challenge.isTokenValid()).thenReturn(true);
        when(challenge.getEmail()).thenReturn(EMAIL);

        String result = otpService.getEmailByToken("valid-token");

        assertEquals(EMAIL, result);
    }

    @Test
    void shouldThrow_whenTokenInvalid() {
        when(otpChallengeRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> otpService.getEmailByToken("invalid-token"));
    }

    // -- invalidateToken --

    @Test
    void shouldMarkTokenUsed_whenTokenExists() {
        OtpChallenge challenge = mock(OtpChallenge.class);
        when(otpChallengeRepository.findByToken("token")).thenReturn(Optional.of(challenge));

        otpService.invalidateToken("token");

        verify(challenge).setTokenUsed(true);
        verify(otpChallengeRepository).save(challenge);
    }

    @Test
    void shouldDoNothing_whenTokenNotFound() {
        when(otpChallengeRepository.findByToken("unknown-token")).thenReturn(Optional.empty());

        otpService.invalidateToken("unknown-token");

        verify(otpChallengeRepository, never()).save(any());
    }
}
