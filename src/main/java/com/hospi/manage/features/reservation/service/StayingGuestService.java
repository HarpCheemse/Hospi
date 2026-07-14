package com.hospi.manage.features.reservation.service;

import com.hospi.manage.common.exception.ResourceNotFoundException;
import com.hospi.manage.features.reservation.dto.request.StayingGuestForm;
import com.hospi.manage.features.reservation.entity.StayingGuest;
import com.hospi.manage.features.reservation.repository.ReservationRepository;
import com.hospi.manage.features.reservation.repository.StayingGuestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/** Business logic for managing staying guests associated with a reservation. */
@Service
@Transactional
@RequiredArgsConstructor
public class StayingGuestService {

    private final StayingGuestRepository stayingGuestRepository;
    private final ReservationRepository reservationRepository;

    /** Return all staying guests for a reservation. */
    public List<StayingGuest> getGuests(Long reservationId) {
        return stayingGuestRepository.findByReservationIdOrderByCreatedAtAsc(reservationId);
    }

    /** Return the number of registered guests for a reservation. */
    public int getGuestCount(Long reservationId) {
        return (int) stayingGuestRepository.countByReservationId(reservationId);
    }

    /** Add a new staying guest to a reservation. */
    public int getAdultGuestCount(Long reservationId, LocalDate checkInAt) {
        return (int) stayingGuestRepository.findByReservationIdOrderByCreatedAtAsc(reservationId)
                .stream()
                .filter(g -> ChronoUnit.YEARS.between(g.getDateOfBirth(), checkInAt) >= 14)
                .count();
    }

    public StayingGuest addGuest(Long reservationId, StayingGuestForm form) {
        var reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation"));

        StayingGuest guest = new StayingGuest();
        guest.setReservation(reservation);
        guest.setGuestName(form.guestName());
        guest.setDateOfBirth(form.dateOfBirth());
        guest.setNationality(form.nationality());

        return stayingGuestRepository.save(guest);
    }

    /** Update an existing staying guest's details after verifying ownership. */
    public StayingGuest updateGuest(Long reservationId, Long guestId, StayingGuestForm form) {
        StayingGuest guest = stayingGuestRepository.findById(guestId)
                .orElseThrow(() -> new ResourceNotFoundException("Staying guest"));

        if (!guest.getReservation().getId().equals(reservationId)) {
            throw new IllegalArgumentException("Guest does not belong to this reservation");
        }

        guest.setGuestName(form.guestName());
        guest.setDateOfBirth(form.dateOfBirth());
        guest.setNationality(form.nationality());

        return stayingGuestRepository.save(guest);
    }

    /** Find a staying guest by ID and verify it belongs to the reservation. */
    public StayingGuest getGuest(Long reservationId, Long guestId) {
        StayingGuest guest = stayingGuestRepository.findById(guestId)
                .orElseThrow(() -> new ResourceNotFoundException("Staying guest"));

        if (!guest.getReservation().getId().equals(reservationId)) {
            throw new IllegalArgumentException("Guest does not belong to this reservation");
        }

        return guest;
    }

    /** Delete a staying guest after verifying ownership. */
    public void deleteGuest(Long reservationId, Long guestId) {
        StayingGuest guest = stayingGuestRepository.findById(guestId)
                .orElseThrow(() -> new ResourceNotFoundException("Staying guest"));

        if (!guest.getReservation().getId().equals(reservationId)) {
            throw new IllegalArgumentException("Guest does not belong to this reservation");
        }

        stayingGuestRepository.delete(guest);
    }
}
