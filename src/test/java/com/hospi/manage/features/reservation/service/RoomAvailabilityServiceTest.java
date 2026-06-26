package com.hospi.manage.features.reservation.service;

import com.hospi.manage.features.reservation.entity.Reservation;
import com.hospi.manage.features.reservation.entity.ReservationDetail;
import com.hospi.manage.features.reservation.repository.ReservationRepository;
import com.hospi.manage.features.room.dto.response.RoomInventory;
import com.hospi.manage.features.room.dto.response.RoomTypeAvailability;
import com.hospi.manage.features.room.entity.RoomType;
import com.hospi.manage.features.room.repository.RoomRepository;
import com.hospi.manage.features.room.repository.RoomTypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomAvailabilityServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private RoomTypeRepository roomTypeRepository;

    @InjectMocks
    private RoomAvailabilityService roomAvailabilityService;

    private final LocalDate checkIn = LocalDate.of(2026, 7, 1);
    private final LocalDate checkOut = LocalDate.of(2026, 7, 5);

    // -- getAvailability(LocalDate, LocalDate) --

    @Test
    void shouldDelegateToThreeArgOverload_whenCallingTwoArgGetAvailability() {
        List<RoomTypeAvailability> result = roomAvailabilityService.getAvailability(checkIn, checkOut);

        assertNotNull(result);
    }

    // -- getAvailability(LocalDate, LocalDate, Long) --

    @Test
    void shouldReturnAvailability_whenRoomTypesExist() {
        RoomType rt1 = new RoomType();
        rt1.setId(1L);
        RoomType rt2 = new RoomType();
        rt2.setId(2L);

        when(roomRepository.getRoomInventory()).thenReturn(List.of(
                new RoomInventory(1L, "Deluxe", 5),
                new RoomInventory(2L, "Suite", 3)
        ));
        when(roomTypeRepository.findAll()).thenReturn(List.of(rt1, rt2));
        when(reservationRepository.findOverlapping(checkIn, checkOut)).thenReturn(List.of());

        List<RoomTypeAvailability> result = roomAvailabilityService.getAvailability(checkIn, checkOut, null);

        assertEquals(2, result.size());
        assertEquals(rt1, result.get(0).roomType());
        assertEquals(5, result.get(0).totalRooms());
        assertEquals(5, result.get(0).availableRooms());
        assertEquals(rt2, result.get(1).roomType());
        assertEquals(3, result.get(1).totalRooms());
        assertEquals(3, result.get(1).availableRooms());
    }

    @Test
    void shouldSubtractBookedCounts_whenOverlappingReservationsExist() {
        RoomType rt1 = new RoomType();
        rt1.setId(1L);

        ReservationDetail detail = new ReservationDetail();
        detail.setRoomType(rt1);
        detail.setRoomCount(2);

        Reservation reservation = new Reservation();
        reservation.getDetails().add(detail);

        when(roomRepository.getRoomInventory()).thenReturn(List.of(
                new RoomInventory(1L, "Deluxe", 5)
        ));
        when(roomTypeRepository.findAll()).thenReturn(List.of(rt1));
        when(reservationRepository.findOverlapping(checkIn, checkOut)).thenReturn(List.of(reservation));

        List<RoomTypeAvailability> result = roomAvailabilityService.getAvailability(checkIn, checkOut, null);

        assertEquals(1, result.size());
        assertEquals(5, result.get(0).totalRooms());
        assertEquals(3, result.get(0).availableRooms());
    }

    @Test
    void shouldReturnAvailableEqualsTotal_whenNoBookings() {
        RoomType rt1 = new RoomType();
        rt1.setId(1L);

        when(roomRepository.getRoomInventory()).thenReturn(List.of(
                new RoomInventory(1L, "Deluxe", 4)
        ));
        when(roomTypeRepository.findAll()).thenReturn(List.of(rt1));
        when(reservationRepository.findOverlapping(checkIn, checkOut)).thenReturn(List.of());

        List<RoomTypeAvailability> result = roomAvailabilityService.getAvailability(checkIn, checkOut, null);

        assertEquals(4, result.get(0).availableRooms());
        assertEquals(4, result.get(0).totalRooms());
    }

    @Test
    void shouldPassExcludedReservationId_whenProvided() {
        RoomType rt1 = new RoomType();
        rt1.setId(1L);

        when(roomRepository.getRoomInventory()).thenReturn(List.of(
                new RoomInventory(1L, "Deluxe", 3)
        ));
        when(roomTypeRepository.findAll()).thenReturn(List.of(rt1));
        when(reservationRepository.findOverlappingExcluding(99L, checkIn, checkOut)).thenReturn(List.of());

        List<RoomTypeAvailability> result = roomAvailabilityService.getAvailability(checkIn, checkOut, 99L);

        assertEquals(1, result.size());
        assertEquals(3, result.get(0).availableRooms());
        verify(reservationRepository).findOverlappingExcluding(99L, checkIn, checkOut);
        verify(reservationRepository, never()).findOverlapping(any(), any());
    }

    @Test
    void shouldComputeAvailabilityAcrossMultipleRoomTypes() {
        RoomType rt1 = new RoomType();
        rt1.setId(1L);
        RoomType rt2 = new RoomType();
        rt2.setId(2L);

        ReservationDetail d1 = new ReservationDetail();
        d1.setRoomType(rt1);
        d1.setRoomCount(3);

        ReservationDetail d2 = new ReservationDetail();
        d2.setRoomType(rt2);
        d2.setRoomCount(1);

        Reservation r = new Reservation();
        r.getDetails().add(d1);
        r.getDetails().add(d2);

        when(roomRepository.getRoomInventory()).thenReturn(List.of(
                new RoomInventory(1L, "Deluxe", 5),
                new RoomInventory(2L, "Suite", 2)
        ));
        when(roomTypeRepository.findAll()).thenReturn(List.of(rt1, rt2));
        when(reservationRepository.findOverlapping(checkIn, checkOut)).thenReturn(List.of(r));

        List<RoomTypeAvailability> result = roomAvailabilityService.getAvailability(checkIn, checkOut, null);

        assertEquals(2, result.size());
        assertEquals(2, result.get(0).availableRooms());
        assertEquals(1, result.get(1).availableRooms());
    }

    // -- canFulfil --

    @Test
    void shouldReturnTrue_whenAllRequiredRoomsAvailable() {
        RoomType rt1 = new RoomType();
        rt1.setId(1L);
        RoomType rt2 = new RoomType();
        rt2.setId(2L);

        when(roomRepository.getRoomInventory()).thenReturn(List.of(
                new RoomInventory(1L, "Deluxe", 5),
                new RoomInventory(2L, "Suite", 3)
        ));
        when(roomTypeRepository.findAll()).thenReturn(List.of(rt1, rt2));
        when(reservationRepository.findOverlapping(checkIn, checkOut)).thenReturn(List.of());

        Map<Long, Integer> required = Map.of(1L, 3, 2L, 2);
        boolean result = roomAvailabilityService.canFulfil(required, checkIn, checkOut, null);

        assertTrue(result);
    }

    @Test
    void shouldReturnFalse_whenRequiredRoomTypeNotAvailable() {
        RoomType rt1 = new RoomType();
        rt1.setId(1L);

        when(roomRepository.getRoomInventory()).thenReturn(List.of(
                new RoomInventory(1L, "Deluxe", 2)
        ));
        when(roomTypeRepository.findAll()).thenReturn(List.of(rt1));
        when(reservationRepository.findOverlapping(checkIn, checkOut)).thenReturn(List.of());

        Map<Long, Integer> required = Map.of(1L, 5);
        boolean result = roomAvailabilityService.canFulfil(required, checkIn, checkOut, null);

        assertFalse(result);
    }

    @Test
    void shouldReturnTrue_whenRequiredMapIsEmpty() {
        when(roomRepository.getRoomInventory()).thenReturn(List.of(
                new RoomInventory(1L, "Deluxe", 2)
        ));
        when(roomTypeRepository.findAll()).thenReturn(List.of(new RoomType() {{
            setId(1L);
        }}));
        when(reservationRepository.findOverlapping(checkIn, checkOut)).thenReturn(List.of());

        Map<Long, Integer> required = Map.of();
        boolean result = roomAvailabilityService.canFulfil(required, checkIn, checkOut, null);

        assertTrue(result);
    }

    @Test
    void shouldReturnFalse_whenOnlySomeRoomTypesAvailable() {
        RoomType rt1 = new RoomType();
        rt1.setId(1L);
        RoomType rt2 = new RoomType();
        rt2.setId(2L);

        when(roomRepository.getRoomInventory()).thenReturn(List.of(
                new RoomInventory(1L, "Deluxe", 5),
                new RoomInventory(2L, "Suite", 1)
        ));
        when(roomTypeRepository.findAll()).thenReturn(List.of(rt1, rt2));
        when(reservationRepository.findOverlapping(checkIn, checkOut)).thenReturn(List.of());

        Map<Long, Integer> required = Map.of(1L, 2, 2L, 2);
        boolean result = roomAvailabilityService.canFulfil(required, checkIn, checkOut, null);

        assertFalse(result);
    }

    // -- computeBookedCounts --

    @Test
    void shouldCallFindOverlapping_whenExcludedIdIsNull() {
        when(reservationRepository.findOverlapping(checkIn, checkOut)).thenReturn(List.of());

        Map<Long, Integer> result = roomAvailabilityService.computeBookedCounts(checkIn, checkOut, null);

        assertTrue(result.isEmpty());
        verify(reservationRepository).findOverlapping(checkIn, checkOut);
        verify(reservationRepository, never()).findOverlappingExcluding(any(), any(), any());
    }

    @Test
    void shouldCallFindOverlappingExcluding_whenExcludedIdProvided() {
        when(reservationRepository.findOverlappingExcluding(99L, checkIn, checkOut)).thenReturn(List.of());

        Map<Long, Integer> result = roomAvailabilityService.computeBookedCounts(checkIn, checkOut, 99L);

        assertTrue(result.isEmpty());
        verify(reservationRepository).findOverlappingExcluding(99L, checkIn, checkOut);
        verify(reservationRepository, never()).findOverlapping(any(), any());
    }

    @Test
    void shouldAggregateBookedCountsAcrossReservations() {
        RoomType rt1 = new RoomType();
        rt1.setId(1L);
        RoomType rt2 = new RoomType();
        rt2.setId(2L);

        ReservationDetail d1 = new ReservationDetail();
        d1.setRoomType(rt1);
        d1.setRoomCount(2);

        ReservationDetail d2 = new ReservationDetail();
        d2.setRoomType(rt1);
        d2.setRoomCount(1);

        ReservationDetail d3 = new ReservationDetail();
        d3.setRoomType(rt2);
        d3.setRoomCount(3);

        Reservation r1 = new Reservation();
        r1.getDetails().add(d1);
        r1.getDetails().add(d3);

        Reservation r2 = new Reservation();
        r2.getDetails().add(d2);

        when(reservationRepository.findOverlapping(checkIn, checkOut)).thenReturn(List.of(r1, r2));

        Map<Long, Integer> result = roomAvailabilityService.computeBookedCounts(checkIn, checkOut, null);

        assertEquals(2, result.size());
        assertEquals(3, result.get(1L));
        assertEquals(3, result.get(2L));
    }

    @Test
    void shouldReturnEmptyMap_whenNoOverlappingReservations() {
        when(reservationRepository.findOverlapping(checkIn, checkOut)).thenReturn(List.of());

        Map<Long, Integer> result = roomAvailabilityService.computeBookedCounts(checkIn, checkOut, null);

        assertTrue(result.isEmpty());
    }
}
