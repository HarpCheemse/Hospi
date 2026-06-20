package com.hospi.manage.features.reservation.service;

import com.hospi.manage.features.reservation.dto.StayingGuestForm;
import com.hospi.manage.features.reservation.entity.Reservation;
import com.hospi.manage.features.reservation.entity.StayingGuest;
import com.hospi.manage.features.reservation.repository.ReservationRepository;
import com.hospi.manage.features.reservation.repository.StayingGuestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class StayingGuestService {

    private final StayingGuestRepository stayingGuestRepository;
    private final ReservationRepository reservationRepository;

    public StayingGuestService(StayingGuestRepository stayingGuestRepository,
                               ReservationRepository reservationRepository) {
        this.stayingGuestRepository = stayingGuestRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<StayingGuest> getGuests(Long reservationId) {
        return stayingGuestRepository.findByReservationIdOrderByCreatedAtAsc(reservationId);
    }

    public StayingGuest addGuest(Long reservationId, StayingGuestForm form) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));

        StayingGuest guest = new StayingGuest();
        guest.setReservation(reservation);
        guest.setGuestName(form.getGuestName());
        guest.setDateOfBirth(form.getDateOfBirth());
        guest.setNationality(form.getNationality());

        return stayingGuestRepository.save(guest);
    }

    public StayingGuest updateGuest(Long guestId, StayingGuestForm form) {
        StayingGuest guest = stayingGuestRepository.findById(guestId)
                .orElseThrow(() -> new IllegalArgumentException("Staying guest not found"));

        guest.setGuestName(form.getGuestName());
        guest.setDateOfBirth(form.getDateOfBirth());
        guest.setNationality(form.getNationality());

        return stayingGuestRepository.save(guest);
    }

    public StayingGuest getGuest(Long guestId) {
        return stayingGuestRepository.findById(guestId)
                .orElseThrow(() -> new IllegalArgumentException("Staying guest not found"));
    }

    public void deleteGuest(Long guestId) {
        stayingGuestRepository.deleteById(guestId);
    }
}
