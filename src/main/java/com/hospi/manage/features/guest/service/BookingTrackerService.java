package com.hospi.manage.features.guest.service;

import com.hospi.manage.common.exception.ResourceNotFoundException;
import com.hospi.manage.features.reservation.entity.Reservation;
import com.hospi.manage.features.reservation.entity.Review;
import com.hospi.manage.features.reservation.enums.ReservationStatus;
import com.hospi.manage.features.reservation.repository.ReservationRepository;
import com.hospi.manage.features.reservation.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.hospi.manage.features.reservation.enums.ReservationStatus.*;

/** Business logic for guest-side booking lookup, tracking, and review submission. */
@Service
@RequiredArgsConstructor
public class BookingTrackerService {

    private final ReservationRepository reservationRepository;
    private final ReviewRepository reviewRepository;

    /** Find a reservation by guest email and confirmation code. */
    @Transactional(readOnly = true)
    public Reservation lookupByEmailAndCode(String email, String code) {
        return reservationRepository.findByGuestEmailAndConfirmationCode(email, code)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation"));
    }

    /** Resolve multiple reservations by their confirmation codes. */
    @Transactional(readOnly = true)
    public List<Reservation> resolveByCodes(Set<String> codes) {
        return reservationRepository.findByConfirmationCodeIn(codes);
    }

    /** Build a map of reservation ID to existing review for a list of reservations. */
    @Transactional(readOnly = true)
    public Map<Long, Review> buildReviewMap(List<Reservation> reservations) {
        List<Long> ids = reservations.stream().map(Reservation::getId).toList();
        return reviewRepository.findByReservationIdIn(ids).stream()
                .collect(Collectors.toMap(r -> r.getReservation().getId(), r -> r));
    }

    /** Build a map of reservation ID to CSS pill class based on status. */
    public Map<Long, String> buildPillClasses(List<Reservation> reservations) {
        return reservations.stream().collect(Collectors.toMap(
                Reservation::getId,
                r -> pillClass(r.getStatus())
        ));
    }

    /** Submit a review for a checked-out reservation belonging to the tracked codes. */
    @Transactional
    public Review submitReview(Long reservationId, Integer rating, Set<String> trackedCodes) {
        List<Reservation> trackedReservations = resolveByCodes(trackedCodes);
        Reservation reservation = trackedReservations.stream()
                .filter(r -> r.getId().equals(reservationId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("You can only review your own bookings."));

        if (reviewRepository.existsByReservationId(reservationId)) {
            throw new IllegalStateException("You have already reviewed this booking.");
        }

        if (reservation.getStatus() != ReservationStatus.CHECKED_OUT) {
            throw new IllegalStateException("Reviews are only available for completed stays.");
        }

        Review review = new Review();
        review.setReservation(reservation);
        review.setRating(rating);
        return reviewRepository.save(review);
    }

    private static String pillClass(ReservationStatus status) {
        if (status == CHECKED_OUT || status == CHECKED_IN) return "pill-success";
        if (status == CONFIRMED) return "pill-warning";
        if (status == CANCELLED) return "pill-danger";
        return "pill-muted";
    }
}
