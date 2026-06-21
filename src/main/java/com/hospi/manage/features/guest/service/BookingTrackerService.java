package com.hospi.manage.features.guest.service;

import com.hospi.manage.features.reservation.entity.Reservation;
import com.hospi.manage.features.reservation.entity.Review;
import com.hospi.manage.features.reservation.enums.ReservationStatus;
import com.hospi.manage.features.reservation.repository.ReservationRepository;
import com.hospi.manage.features.reservation.repository.ReviewRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.hospi.manage.features.reservation.enums.ReservationStatus.*;

@Service
public class BookingTrackerService {

    private final ReservationRepository reservationRepository;
    private final ReviewRepository reviewRepository;

    public BookingTrackerService(ReservationRepository reservationRepository,
                                 ReviewRepository reviewRepository) {
        this.reservationRepository = reservationRepository;
        this.reviewRepository = reviewRepository;
    }

    public Optional<Reservation> lookupByEmailAndCode(String email, String code) {
        return reservationRepository.findByGuestEmailAndConfirmationCode(email, code);
    }

    public List<Reservation> resolveByCodes(Set<String> codes) {
        return codes.stream()
                .map(code -> reservationRepository.findByConfirmationCode(code).orElse(null))
                .filter(Objects::nonNull)
                .toList();
    }

    public Map<Long, Review> buildReviewMap(List<Reservation> reservations) {
        return reservations.stream()
                .map(r -> reviewRepository.findByReservationId(r.getId()).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(r -> r.getReservation().getId(), r -> r));
    }

    public Map<Long, String> buildPillClasses(List<Reservation> reservations) {
        Map<Long, String> map = new HashMap<>();
        for (Reservation r : reservations) {
            map.put(r.getId(), pillClass(r.getStatus()));
        }
        return map;
    }

    public Review submitReview(Long reservationId, Integer rating) {
        if (reviewRepository.existsByReservationId(reservationId)) {
            throw new IllegalStateException("You have already reviewed this booking.");
        }

        Reservation reservation = reservationRepository.findById(reservationId).orElse(null);

        if (reservation == null || reservation.getStatus() != ReservationStatus.CHECKED_OUT) {
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
