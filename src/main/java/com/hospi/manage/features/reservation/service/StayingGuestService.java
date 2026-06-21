package com.hospi.manage.features.reservation.service;

import com.hospi.manage.common.exception.ResourceNotFoundException;
import com.hospi.manage.features.reservation.dto.request.StayingGuestForm;
import com.hospi.manage.features.reservation.entity.StayingGuest;
import com.hospi.manage.features.reservation.repository.ReservationRepository;
import com.hospi.manage.features.reservation.repository.StayingGuestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class StayingGuestService {

    private final StayingGuestRepository stayingGuestRepository;
    private final ReservationRepository reservationRepository;

    public List<StayingGuest> getGuests(Long reservationId) {
        return stayingGuestRepository.findByReservationIdOrderByCreatedAtAsc(reservationId);
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

    public StayingGuest getGuest(Long reservationId, Long guestId) {
        StayingGuest guest = stayingGuestRepository.findById(guestId)
                .orElseThrow(() -> new ResourceNotFoundException("Staying guest"));

        if (!guest.getReservation().getId().equals(reservationId)) {
            throw new IllegalArgumentException("Guest does not belong to this reservation");
        }

        return guest;
    }

    public void deleteGuest(Long reservationId, Long guestId) {
        StayingGuest guest = stayingGuestRepository.findById(guestId)
                .orElseThrow(() -> new ResourceNotFoundException("Staying guest"));

        if (!guest.getReservation().getId().equals(reservationId)) {
            throw new IllegalArgumentException("Guest does not belong to this reservation");
        }

        stayingGuestRepository.delete(guest);
    }
}
