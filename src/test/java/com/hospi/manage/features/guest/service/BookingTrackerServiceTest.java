package com.hospi.manage.features.guest.service;

import com.hospi.manage.common.exception.ResourceNotFoundException;
import com.hospi.manage.features.reservation.entity.Reservation;
import com.hospi.manage.features.reservation.entity.Review;
import com.hospi.manage.features.reservation.enums.ReservationStatus;
import com.hospi.manage.features.reservation.repository.ReservationRepository;
import com.hospi.manage.features.reservation.repository.ReviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingTrackerServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private BookingTrackerService service;

    @Test
    void lookupByEmailAndCode_shouldReturnReservation_whenFound() {
        Reservation reservation = new Reservation();
        when(reservationRepository.findByGuestEmailAndConfirmationCode("a@b.com", "CODE"))
                .thenReturn(Optional.of(reservation));

        Reservation result = service.lookupByEmailAndCode("a@b.com", "CODE");

        assertSame(reservation, result);
    }

    @Test
    void lookupByEmailAndCode_shouldThrow_whenNotFound() {
        when(reservationRepository.findByGuestEmailAndConfirmationCode("a@b.com", "CODE"))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.lookupByEmailAndCode("a@b.com", "CODE"));
    }

    @Test
    void resolveByCodes_shouldReturnMatchingReservations() {
        Reservation r1 = new Reservation();
        r1.setConfirmationCode("C1");
        r1.setId(1L);
        Reservation r2 = new Reservation();
        r2.setConfirmationCode("C2");
        r2.setId(2L);
        when(reservationRepository.findByConfirmationCodeIn(Set.of("C1", "C2")))
                .thenReturn(List.of(r1, r2));

        List<Reservation> result = service.resolveByCodes(Set.of("C1", "C2"));

        assertEquals(2, result.size());
    }

    @Test
    void buildPillClasses_shouldReturnSuccess_forCheckedOut() {
        Reservation r = new Reservation();
        r.setId(1L);
        r.setStatus(ReservationStatus.CHECKED_OUT);

        Map<Long, String> result = service.buildPillClasses(List.of(r));

        assertEquals("pill-success", result.get(1L));
    }

    @Test
    void buildPillClasses_shouldReturnSuccess_forCheckedIn() {
        Reservation r = new Reservation();
        r.setId(1L);
        r.setStatus(ReservationStatus.CHECKED_IN);

        Map<Long, String> result = service.buildPillClasses(List.of(r));

        assertEquals("pill-success", result.get(1L));
    }

    @Test
    void buildPillClasses_shouldReturnWarning_forConfirmed() {
        Reservation r = new Reservation();
        r.setId(1L);
        r.setStatus(ReservationStatus.CONFIRMED);

        Map<Long, String> result = service.buildPillClasses(List.of(r));

        assertEquals("pill-warning", result.get(1L));
    }

    @Test
    void buildPillClasses_shouldReturnDanger_forCancelled() {
        Reservation r = new Reservation();
        r.setId(1L);
        r.setStatus(ReservationStatus.CANCELLED);

        Map<Long, String> result = service.buildPillClasses(List.of(r));

        assertEquals("pill-danger", result.get(1L));
    }

    @Test
    void buildPillClasses_shouldReturnMuted_forPending() {
        Reservation r = new Reservation();
        r.setId(1L);
        r.setStatus(ReservationStatus.PENDING);

        Map<Long, String> result = service.buildPillClasses(List.of(r));

        assertEquals("pill-muted", result.get(1L));
    }

    @Test
    void submitReview_shouldSaveReview_whenValid() {
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setStatus(ReservationStatus.CHECKED_OUT);
        when(reservationRepository.findByConfirmationCodeIn(Set.of("CODE")))
                .thenReturn(List.of(reservation));
        when(reviewRepository.existsByReservationId(1L)).thenReturn(false);

        service.submitReview(1L, 4, Set.of("CODE"));

        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void submitReview_shouldThrow_whenAlreadyReviewed() {
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        when(reservationRepository.findByConfirmationCodeIn(Set.of("CODE")))
                .thenReturn(List.of(reservation));
        when(reviewRepository.existsByReservationId(1L)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> service.submitReview(1L, 4, Set.of("CODE")));
    }

    @Test
    void submitReview_shouldThrow_whenNotCheckedOut() {
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setStatus(ReservationStatus.CONFIRMED);
        when(reservationRepository.findByConfirmationCodeIn(Set.of("CODE")))
                .thenReturn(List.of(reservation));
        when(reviewRepository.existsByReservationId(1L)).thenReturn(false);

        assertThrows(IllegalStateException.class, () -> service.submitReview(1L, 4, Set.of("CODE")));
    }

    @Test
    void submitReview_shouldThrow_whenNotOwned() {
        Reservation reservation = new Reservation();
        reservation.setId(2L);
        when(reservationRepository.findByConfirmationCodeIn(Set.of("CODE")))
                .thenReturn(List.of(reservation));

        assertThrows(IllegalStateException.class, () -> service.submitReview(1L, 4, Set.of("CODE")));
    }
}
