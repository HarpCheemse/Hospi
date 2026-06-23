package com.hospi.manage.features.reservation.service;

import com.hospi.manage.features.admin.config.entity.SystemConfig;
import com.hospi.manage.features.admin.config.service.SystemConfigService;
import com.hospi.manage.features.reservation.entity.Reservation;
import com.hospi.manage.features.reservation.enums.ReservationStatus;
import com.hospi.manage.features.reservation.repository.ReservationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class ReservationCleanupServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private SystemConfigService systemConfigService;

    @InjectMocks
    private ReservationCleanupService reservationCleanupService;

    @Test
    void cancelExpiredPendingReservations_shouldCancel_whenExpired() {
        SystemConfig config = new SystemConfig();
        config.setPendingBookingExpiryMinutes(30);
        when(systemConfigService.getConfig()).thenReturn(config);

        Reservation expired = new Reservation();
        expired.setId(1L);
        expired.setStatus(ReservationStatus.PENDING);
        expired.setCreatedAt(LocalDateTime.now().minusMinutes(40));
        expired.setGuestName("John Doe");

        when(reservationRepository.findByStatusAndCreatedAtBefore(
                eq(ReservationStatus.PENDING), any())).thenReturn(List.of(expired));

        reservationCleanupService.cancelExpiredPendingReservations();

        assertEquals(ReservationStatus.CANCELLED, expired.getStatus());
    }

    @Test
    void cancelExpiredPendingReservations_shouldSkip_whenConfigNull() {
        SystemConfig config = new SystemConfig();
        config.setPendingBookingExpiryMinutes(null);
        when(systemConfigService.getConfig()).thenReturn(config);

        reservationCleanupService.cancelExpiredPendingReservations();

        verify(reservationRepository, never()).findByStatusAndCreatedAtBefore(any(), any());
    }

    @Test
    void cancelExpiredPendingReservations_shouldSkip_whenConfigZero() {
        SystemConfig config = new SystemConfig();
        config.setPendingBookingExpiryMinutes(0);
        when(systemConfigService.getConfig()).thenReturn(config);

        reservationCleanupService.cancelExpiredPendingReservations();

        verify(reservationRepository, never()).findByStatusAndCreatedAtBefore(any(), any());
    }

    @Test
    void cancelExpiredPendingReservations_shouldSkip_whenConfigNegative() {
        SystemConfig config = new SystemConfig();
        config.setPendingBookingExpiryMinutes(-1);
        when(systemConfigService.getConfig()).thenReturn(config);

        reservationCleanupService.cancelExpiredPendingReservations();

        verify(reservationRepository, never()).findByStatusAndCreatedAtBefore(any(), any());
    }
}
