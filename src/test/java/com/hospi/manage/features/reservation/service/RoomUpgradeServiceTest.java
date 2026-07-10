package com.hospi.manage.features.reservation.service;

import com.hospi.manage.common.exception.ResourceNotFoundException;
import com.hospi.manage.features.reservation.entity.Reservation;
import com.hospi.manage.features.reservation.entity.ReservationDetail;
import com.hospi.manage.features.reservation.entity.RoomAssignment;
import com.hospi.manage.features.reservation.enums.ReservationStatus;
import com.hospi.manage.features.reservation.repository.ReservationRepository;
import com.hospi.manage.features.reservation.repository.RoomAssignmentRepository;
import com.hospi.manage.features.room.entity.Room;
import com.hospi.manage.features.room.entity.RoomType;
import com.hospi.manage.features.room.enums.OccupancyStatus;
import com.hospi.manage.features.room.repository.RoomRepository;
import com.hospi.manage.features.room.repository.RoomTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomUpgradeServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private RoomTypeRepository roomTypeRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private RoomAssignmentRepository roomAssignmentRepository;

    @InjectMocks
    private RoomUpgradeService service;

    private Reservation reservation;
    private ReservationDetail detail;
    private RoomType basicDouble;
    private RoomType deluxeDouble;

    @BeforeEach
    void setUp() {
        basicDouble = new RoomType();
        basicDouble.setId(1L);
        basicDouble.setName("BASIC DOUBLE");
        basicDouble.setBasePrice(BigDecimal.valueOf(85));

        deluxeDouble = new RoomType();
        deluxeDouble.setId(2L);
        deluxeDouble.setName("DELUXE DOUBLE");
        deluxeDouble.setBasePrice(BigDecimal.valueOf(145));

        detail = new ReservationDetail();
        detail.setRoomType(basicDouble);
        detail.setBasePrice(BigDecimal.valueOf(85));
        detail.setRoomCount(2);
        detail.setTotalPrice(BigDecimal.valueOf(850));

        reservation = new Reservation();
        reservation.setId(1L);
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setCheckInAt(LocalDate.now());
        reservation.setCheckOutAt(LocalDate.now().plusDays(5));
        reservation.setTotalPrice(BigDecimal.valueOf(850));
        reservation.setDetails(new ArrayList<>(List.of(detail)));
    }

    @Test
    void swapOne_shouldSplitDetail_whenRoomCountGreaterThanOne() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(roomTypeRepository.findById(2L)).thenReturn(Optional.of(deluxeDouble));
        when(reservationRepository.save(any())).thenReturn(reservation);

        service.swapOne(1L, 1L, 2L);

        assertEquals(1, detail.getRoomCount());
        assertEquals(2, reservation.getDetails().size());
        var newDetail = reservation.getDetails().get(1);
        assertEquals("DELUXE DOUBLE", newDetail.getRoomType().getName());
        assertEquals(1, newDetail.getRoomCount());
        verify(reservationRepository).save(reservation);
    }

    @Test
    void swapOne_shouldReplaceDetail_whenRoomCountIsOne() {
        detail.setRoomCount(1);
        detail.setTotalPrice(BigDecimal.valueOf(425));

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(roomTypeRepository.findById(2L)).thenReturn(Optional.of(deluxeDouble));
        when(reservationRepository.save(any())).thenReturn(reservation);

        service.swapOne(1L, 1L, 2L);

        assertEquals(1, reservation.getDetails().size());
        var newDetail = reservation.getDetails().get(0);
        assertEquals("DELUXE DOUBLE", newDetail.getRoomType().getName());
        assertEquals(BigDecimal.valueOf(145), newDetail.getBasePrice());
    }

    @Test
    void swapOne_shouldMerge_whenTargetTypeAlreadyExists() {
        detail.setRoomCount(2);
        detail.setTotalPrice(BigDecimal.valueOf(850));

        var existingDeluxe = new ReservationDetail();
        existingDeluxe.setRoomType(deluxeDouble);
        existingDeluxe.setBasePrice(BigDecimal.valueOf(145));
        existingDeluxe.setRoomCount(1);
        existingDeluxe.setTotalPrice(BigDecimal.valueOf(725));
        reservation.getDetails().add(existingDeluxe);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(roomTypeRepository.findById(2L)).thenReturn(Optional.of(deluxeDouble));
        when(reservationRepository.save(any())).thenReturn(reservation);

        service.swapOne(1L, 1L, 2L);

        assertEquals(2, reservation.getDetails().size());
        assertEquals(1, detail.getRoomCount());
        var merged = reservation.getDetails().stream()
                .filter(d -> d.getRoomType().getId().equals(2L))
                .findFirst().orElseThrow();
        assertEquals(2, merged.getRoomCount());
    }

    @Test
    void swapOne_shouldReassignRooms_whenCheckedIn() {
        reservation.setStatus(ReservationStatus.CHECKED_IN);
        detail.setRoomCount(1);

        var oldRoom = new Room();
        oldRoom.setId(1L);
        var oldRoomType = new RoomType();
        oldRoomType.setId(1L);
        oldRoom.setRoomType(oldRoomType);
        oldRoom.setOccupancyStatus(OccupancyStatus.OCCUPIED);

        var assignment = new RoomAssignment();
        assignment.setRoom(oldRoom);

        var newRoom = new Room();
        newRoom.setId(2L);
        newRoom.setOccupancyStatus(OccupancyStatus.VACANT);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(roomTypeRepository.findById(2L)).thenReturn(Optional.of(deluxeDouble));
        when(roomAssignmentRepository.findByReservationIdOrderByAssignedAtAsc(1L))
                .thenReturn(List.of(assignment));
        when(roomRepository.findByRoomTypeIdAndOccupancyStatusAndActiveTrue(2L, OccupancyStatus.VACANT))
                .thenReturn(List.of(newRoom));
        when(reservationRepository.save(any())).thenReturn(reservation);

        service.swapOne(1L, 1L, 2L);

        assertEquals(OccupancyStatus.VACANT, oldRoom.getOccupancyStatus());
        assertEquals(OccupancyStatus.OCCUPIED, newRoom.getOccupancyStatus());
        verify(roomAssignmentRepository).save(any(RoomAssignment.class));
    }

    @Test
    void swapOne_shouldThrow_whenReservationNotFound() {
        when(reservationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.swapOne(99L, 1L, 2L));
    }

    @Test
    void swapOne_shouldThrow_whenStatusNotEligible() {
        reservation.setStatus(ReservationStatus.PENDING);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        assertThrows(IllegalStateException.class,
                () -> service.swapOne(1L, 1L, 2L));
    }

    @Test
    void swapOne_shouldThrow_whenNewRoomTypeNotFound() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(roomTypeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.swapOne(1L, 1L, 99L));
    }

    @Test
    void swapOne_shouldThrow_whenPriceNotHigher() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        var samePrice = new RoomType();
        samePrice.setId(3L);
        samePrice.setBasePrice(BigDecimal.valueOf(85));
        when(roomTypeRepository.findById(3L)).thenReturn(Optional.of(samePrice));

        assertThrows(IllegalStateException.class,
                () -> service.swapOne(1L, 1L, 3L));
    }

    @Test
    void swapOne_shouldThrow_whenCurrentRoomTypeNotFound() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(roomTypeRepository.findById(2L)).thenReturn(Optional.of(deluxeDouble));

        assertThrows(IllegalStateException.class,
                () -> service.swapOne(1L, 99L, 2L));
    }

    @Test
    void swapOne_shouldThrow_whenNoDates() {
        reservation.setCheckInAt(null);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(roomTypeRepository.findById(2L)).thenReturn(Optional.of(deluxeDouble));

        assertThrows(IllegalStateException.class,
                () -> service.swapOne(1L, 1L, 2L));
    }
}
