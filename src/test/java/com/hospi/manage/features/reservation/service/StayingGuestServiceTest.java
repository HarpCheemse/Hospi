package com.hospi.manage.features.reservation.service;

import com.hospi.manage.common.exception.ResourceNotFoundException;
import com.hospi.manage.features.reservation.dto.StayingGuestForm;
import com.hospi.manage.features.reservation.entity.Reservation;
import com.hospi.manage.features.reservation.entity.StayingGuest;
import com.hospi.manage.features.reservation.repository.ReservationRepository;
import com.hospi.manage.features.reservation.repository.StayingGuestRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class StayingGuestServiceTest {

    @Mock
    private StayingGuestRepository stayingGuestRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private StayingGuestService stayingGuestService;

    private final StayingGuestForm form = new StayingGuestForm("Jane Guest", LocalDate.of(1992, 6, 15), "UK");

    @Test
    void addGuest_shouldCreate() {
        Reservation reservation = new Reservation();
        reservation.setId(1L);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(stayingGuestRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        StayingGuest result = stayingGuestService.addGuest(1L, form);

        assertEquals("Jane Guest", result.getGuestName());
        assertEquals(LocalDate.of(1992, 6, 15), result.getDateOfBirth());
        assertEquals("UK", result.getNationality());
        assertEquals(reservation, result.getReservation());
        verify(stayingGuestRepository).save(result);
    }

    @Test
    void addGuest_shouldThrow_whenReservationNotFound() {
        when(reservationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> stayingGuestService.addGuest(999L, form));

        verify(stayingGuestRepository, never()).save(any());
    }

    @Test
    void updateGuest_shouldUpdate() {
        Reservation reservation = new Reservation();
        reservation.setId(1L);

        StayingGuest guest = new StayingGuest();
        guest.setId(1L);
        guest.setReservation(reservation);
        guest.setGuestName("Old Name");
        guest.setDateOfBirth(LocalDate.of(1980, 1, 1));
        guest.setNationality("US");

        when(stayingGuestRepository.findById(1L)).thenReturn(Optional.of(guest));
        when(stayingGuestRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        StayingGuest result = stayingGuestService.updateGuest(1L, 1L, form);

        assertEquals("Jane Guest", result.getGuestName());
        assertEquals(LocalDate.of(1992, 6, 15), result.getDateOfBirth());
        assertEquals("UK", result.getNationality());
        verify(stayingGuestRepository).save(guest);
    }

    @Test
    void updateGuest_shouldThrow_whenNotFound() {
        when(stayingGuestRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> stayingGuestService.updateGuest(1L, 999L, form));
    }

    @Test
    void updateGuest_shouldThrow_whenNotOwnedByReservation() {
        Reservation otherReservation = new Reservation();
        otherReservation.setId(2L);

        StayingGuest guest = new StayingGuest();
        guest.setId(1L);
        guest.setReservation(otherReservation);

        when(stayingGuestRepository.findById(1L)).thenReturn(Optional.of(guest));

        assertThrows(IllegalArgumentException.class,
                () -> stayingGuestService.updateGuest(1L, 1L, form));
    }

    @Test
    void deleteGuest_shouldDelete() {
        Reservation reservation = new Reservation();
        reservation.setId(1L);

        StayingGuest guest = new StayingGuest();
        guest.setId(1L);
        guest.setReservation(reservation);

        when(stayingGuestRepository.findById(1L)).thenReturn(Optional.of(guest));

        stayingGuestService.deleteGuest(1L, 1L);

        verify(stayingGuestRepository).delete(guest);
    }

    @Test
    void deleteGuest_shouldThrow_whenNotOwnedByReservation() {
        Reservation otherReservation = new Reservation();
        otherReservation.setId(2L);

        StayingGuest guest = new StayingGuest();
        guest.setId(1L);
        guest.setReservation(otherReservation);

        when(stayingGuestRepository.findById(1L)).thenReturn(Optional.of(guest));

        assertThrows(IllegalArgumentException.class,
                () -> stayingGuestService.deleteGuest(1L, 1L));
    }

    @Test
    void getGuests_shouldReturnList() {
        when(stayingGuestRepository.findByReservationIdOrderByCreatedAtAsc(1L))
                .thenReturn(List.of(new StayingGuest()));

        List<StayingGuest> result = stayingGuestService.getGuests(1L);

        assertEquals(1, result.size());
        verify(stayingGuestRepository).findByReservationIdOrderByCreatedAtAsc(1L);
    }

    @Test
    void getGuest_shouldReturn_whenFound() {
        Reservation reservation = new Reservation();
        reservation.setId(1L);

        StayingGuest guest = new StayingGuest();
        guest.setId(1L);
        guest.setReservation(reservation);

        when(stayingGuestRepository.findById(1L)).thenReturn(Optional.of(guest));

        StayingGuest result = stayingGuestService.getGuest(1L, 1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void getGuest_shouldThrow_whenNotFound() {
        when(stayingGuestRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> stayingGuestService.getGuest(1L, 999L));
    }

    @Test
    void getGuest_shouldThrow_whenNotOwnedByReservation() {
        Reservation otherReservation = new Reservation();
        otherReservation.setId(2L);

        StayingGuest guest = new StayingGuest();
        guest.setId(1L);
        guest.setReservation(otherReservation);

        when(stayingGuestRepository.findById(1L)).thenReturn(Optional.of(guest));

        assertThrows(IllegalArgumentException.class,
                () -> stayingGuestService.getGuest(1L, 1L));
    }
}
