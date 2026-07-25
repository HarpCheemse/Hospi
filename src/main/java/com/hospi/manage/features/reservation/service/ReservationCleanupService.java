package com.hospi.manage.features.reservation.service;

import com.hospi.manage.features.config.service.SystemConfigService;
import com.hospi.manage.features.reservation.entity.Reservation;
import com.hospi.manage.features.reservation.enums.CancellationReason;
import com.hospi.manage.features.reservation.enums.ReservationStatus;
import com.hospi.manage.features.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Periodically cancels PENDING reservations that have exceeded the configured expiry window.
 * Runs every 60 seconds. The expiry threshold is {@code pendingBookingExpiryMinutes}
 * from {@link com.hospi.manage.features.config.entity.SystemConfig}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationCleanupService {

    private final ReservationRepository reservationRepository;
    private final SystemConfigService systemConfigService;

    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void cancelExpiredPendingReservations() {
        Integer expiryMinutes = systemConfigService.getConfig().getPendingBookingExpiryMinutes();
        if (expiryMinutes == null || expiryMinutes <= 0) {
            return;
        }

        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(expiryMinutes);
        List<Reservation> expired = reservationRepository.findByStatusAndCreatedAtBefore(ReservationStatus.PENDING,
                cutoff);

        LocalDateTime now = LocalDateTime.now();
        for (Reservation r : expired) {
            r.setStatus(ReservationStatus.CANCELLED);
            r.setCancelledAt(now);
            r.setCancellationReason(CancellationReason.EXPIRED_PENDING);
            log.warn("Cancelled expired PENDING reservation {} (created {}, guest {})",
                    r.getId(),
                    r.getCreatedAt(),
                    r.getGuestName());
        }

        reservationRepository.saveAll(expired);
    }
}
