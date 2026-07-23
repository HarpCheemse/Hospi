package com.hospi.manage.features.reservation.service;

import com.hospi.manage.features.reservation.entity.Reservation;
import com.hospi.manage.features.reservation.enums.CancellationReason;
import com.hospi.manage.features.reservation.enums.ReservationStatus;
import com.hospi.manage.features.reservation.repository.ReservationRepository;
import com.hospi.manage.features.reservation.repository.RoomAssignmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Periodically marks CONFIRMED reservations that missed their check-in date as NO_SHOW
 * and releases their room assignments. Runs daily at 6:00 AM.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NoShowCleanupService {

    private final ReservationRepository reservationRepository;
    private final RoomAssignmentRepository roomAssignmentRepository;

    @Scheduled(cron = "0 0 6 * * *")
    @Transactional
    public void cancelNoShowReservations() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        List<Reservation> noShows = reservationRepository.findByStatusAndCheckInAtBefore(
                ReservationStatus.CONFIRMED, yesterday);

        if (noShows.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        for (Reservation r : noShows) {
            r.setStatus(ReservationStatus.CANCELLED);
            r.setCancelledAt(now);
            r.setCancellationReason(CancellationReason.NO_SHOW);
            roomAssignmentRepository.deleteByReservationId(r.getId());
            log.warn("Marked CONFIRMED reservation {} (guest {}) as NO_SHOW — missed check-in {}",
                    r.getId(), r.getGuestName(), r.getCheckInAt());
        }

        reservationRepository.saveAll(noShows);
    }
}
