package com.hospi.manage.features.reservation.service;

import com.hospi.manage.features.reservation.entity.Reservation;
import com.hospi.manage.features.reservation.enums.CancellationReason;
import com.hospi.manage.features.reservation.enums.ReservationStatus;
import com.hospi.manage.features.reservation.repository.ReservationRepository;
import com.hospi.manage.features.reservation.repository.RoomAssignmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class NoShowCleanupServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private RoomAssignmentRepository roomAssignmentRepository;

    @InjectMocks
    private NoShowCleanupService noShowCleanupService;

    @Test
    void cancelNoShowReservations_shouldCancelAndFreeRooms() {
        Reservation noShow = new Reservation();
        noShow.setId(1L);
        noShow.setStatus(ReservationStatus.CONFIRMED);
        noShow.setCheckInAt(LocalDate.now().minusDays(2));
        noShow.setGuestName("John Doe");

        when(reservationRepository.findByStatusAndCheckInAtBefore(
                eq(ReservationStatus.CONFIRMED), any())).thenReturn(List.of(noShow));

        noShowCleanupService.cancelNoShowReservations();

        assertEquals(ReservationStatus.CANCELLED, noShow.getStatus());
        assertEquals(CancellationReason.NO_SHOW, noShow.getCancellationReason());
        verify(roomAssignmentRepository).deleteByReservationId(1L);
        verify(reservationRepository).saveAll(List.of(noShow));
    }

    @Test
    void cancelNoShowReservations_shouldSkip_whenNone() {
        when(reservationRepository.findByStatusAndCheckInAtBefore(
                eq(ReservationStatus.CONFIRMED), any())).thenReturn(List.of());

        noShowCleanupService.cancelNoShowReservations();

        verify(roomAssignmentRepository, never()).deleteByReservationId(any());
        verify(reservationRepository, never()).saveAll(any());
    }
}
