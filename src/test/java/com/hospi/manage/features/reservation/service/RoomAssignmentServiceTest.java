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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class RoomAssignmentServiceTest {

    @Mock
    private RoomAssignmentRepository roomAssignmentRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private RoomAssignmentService roomAssignmentService;

    // --- assignRoom ---

    @Test
    void assignRoom_shouldAssign() {
        RoomType roomType = new RoomType();
        roomType.setId(99L);
        roomType.setName("Deluxe");

        ReservationDetail detail = new ReservationDetail();
        detail.setRoomType(roomType);
        detail.setRoomCount(3);

        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setDetails(List.of(detail));

        Room room = new Room();
        room.setId(10L);
        room.setRoomType(roomType);
        room.setOccupancyStatus(OccupancyStatus.VACANT);

        RoomAssignment savedAssignment = new RoomAssignment();
        savedAssignment.setId(100L);

        when(roomAssignmentRepository.existsByReservationIdAndRoomId(1L, 10L)).thenReturn(false);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(roomRepository.findById(10L)).thenReturn(Optional.of(room));
        when(roomAssignmentRepository.countByReservationIdAndRoom_RoomTypeId(1L, 99L)).thenReturn(0L);
        when(roomAssignmentRepository.save(any())).thenAnswer(i -> {
            RoomAssignment ra = i.getArgument(0);
            ra.setId(100L);
            return ra;
        });

        RoomAssignment result = roomAssignmentService.assignRoom(1L, 10L);

        assertEquals(100L, result.getId());
        assertEquals(reservation, result.getReservation());
        assertEquals(room, result.getRoom());
        assertEquals(OccupancyStatus.OCCUPIED, room.getOccupancyStatus());
        verify(roomRepository).save(room);
        verify(roomAssignmentRepository).save(any());
    }

    @Test
    void assignRoom_shouldThrow_whenRoomTypeLimitExceeded() {
        RoomType roomType = new RoomType();
        roomType.setId(99L);
        roomType.setName("Deluxe");

        ReservationDetail detail = new ReservationDetail();
        detail.setRoomType(roomType);
        detail.setRoomCount(1);

        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setDetails(List.of(detail));

        Room room = new Room();
        room.setId(10L);
        room.setRoomType(roomType);
        room.setOccupancyStatus(OccupancyStatus.VACANT);

        when(roomAssignmentRepository.existsByReservationIdAndRoomId(1L, 10L)).thenReturn(false);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(roomRepository.findById(10L)).thenReturn(Optional.of(room));
        when(roomAssignmentRepository.countByReservationIdAndRoom_RoomTypeId(1L, 99L)).thenReturn(1L);

        assertThrows(IllegalStateException.class,
                () -> roomAssignmentService.assignRoom(1L, 10L));

        verify(roomRepository, never()).save(any());
        verify(roomAssignmentRepository, never()).save(any());
    }

    @Test
    void assignRoom_shouldThrow_whenAlreadyAssigned() {
        when(roomAssignmentRepository.existsByReservationIdAndRoomId(1L, 10L)).thenReturn(true);

        assertThrows(IllegalStateException.class,
                () -> roomAssignmentService.assignRoom(1L, 10L));

        verify(roomRepository, never()).save(any());
        verify(roomAssignmentRepository, never()).save(any());
    }

    @Test
    void assignRoom_shouldThrow_whenReservationNotFound() {
        when(roomAssignmentRepository.existsByReservationIdAndRoomId(1L, 10L)).thenReturn(false);
        when(reservationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> roomAssignmentService.assignRoom(1L, 10L));
    }

    @Test
    void assignRoom_shouldThrow_whenReservationIsPending() {
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setStatus(ReservationStatus.PENDING);

        when(roomAssignmentRepository.existsByReservationIdAndRoomId(1L, 10L)).thenReturn(false);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        assertThrows(IllegalStateException.class,
                () -> roomAssignmentService.assignRoom(1L, 10L));

        verify(roomRepository, never()).save(any());
        verify(roomAssignmentRepository, never()).save(any());
    }

    @Test
    void assignRoom_shouldThrow_whenRoomNotFound() {
        Reservation reservation = new Reservation();
        reservation.setStatus(ReservationStatus.CONFIRMED);
        when(roomAssignmentRepository.existsByReservationIdAndRoomId(1L, 10L)).thenReturn(false);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(roomRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> roomAssignmentService.assignRoom(1L, 10L));
    }

    @Test
    void assignRoom_shouldThrow_whenRoomNotVacant() {
        Reservation reservation = new Reservation();
        reservation.setId(1L);

        Room room = new Room();
        room.setId(10L);
        room.setOccupancyStatus(OccupancyStatus.OCCUPIED);

        when(roomAssignmentRepository.existsByReservationIdAndRoomId(1L, 10L)).thenReturn(false);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(roomRepository.findById(10L)).thenReturn(Optional.of(room));

        assertThrows(IllegalStateException.class,
                () -> roomAssignmentService.assignRoom(1L, 10L));

        verify(roomRepository, never()).save(any());
        verify(roomAssignmentRepository, never()).save(any());
    }

    // --- removeAssignment ---

    @Test
    void removeAssignment_shouldRemove() {
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setStatus(ReservationStatus.CONFIRMED);

        Room room = new Room();
        room.setId(10L);
        room.setOccupancyStatus(OccupancyStatus.OCCUPIED);

        RoomAssignment assignment = new RoomAssignment();
        assignment.setId(100L);
        assignment.setReservation(reservation);
        assignment.setRoom(room);

        when(roomAssignmentRepository.findById(100L)).thenReturn(Optional.of(assignment));

        roomAssignmentService.removeAssignment(1L, 100L);

        assertEquals(OccupancyStatus.VACANT, room.getOccupancyStatus());
        verify(roomRepository).save(room);
        verify(roomAssignmentRepository).delete(assignment);
    }

    @Test
    void removeAssignment_shouldThrow_whenNotFound() {
        when(roomAssignmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> roomAssignmentService.removeAssignment(1L, 999L));

        verify(roomRepository, never()).save(any());
        verify(roomAssignmentRepository, never()).delete(any());
    }

    @Test
    void removeAssignment_shouldThrow_whenNotOwnedByReservation() {
        Reservation reservation = new Reservation();
        reservation.setId(1L);

        Reservation otherReservation = new Reservation();
        otherReservation.setId(2L);

        Room room = new Room();
        room.setId(10L);

        RoomAssignment assignment = new RoomAssignment();
        assignment.setId(100L);
        assignment.setReservation(otherReservation);
        assignment.setRoom(room);

        when(roomAssignmentRepository.findById(100L)).thenReturn(Optional.of(assignment));

        assertThrows(IllegalArgumentException.class,
                () -> roomAssignmentService.removeAssignment(1L, 100L));
    }

    @Test
    void removeAssignment_shouldThrow_whenReservationIsPending() {
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setStatus(ReservationStatus.PENDING);

        Room room = new Room();
        room.setId(10L);

        RoomAssignment assignment = new RoomAssignment();
        assignment.setId(100L);
        assignment.setReservation(reservation);
        assignment.setRoom(room);

        when(roomAssignmentRepository.findById(100L)).thenReturn(Optional.of(assignment));

        assertThrows(IllegalStateException.class,
                () -> roomAssignmentService.removeAssignment(1L, 100L));

        verify(roomRepository, never()).save(any());
        verify(roomAssignmentRepository, never()).delete(any());
    }

    // --- getAssignedRooms ---

    @Test
    void getAssignedRooms_shouldReturnList() {
        when(roomAssignmentRepository.findByReservationIdOrderByAssignedAtAsc(1L))
                .thenReturn(List.of(new RoomAssignment()));

        List<RoomAssignment> result = roomAssignmentService.getAssignedRooms(1L);

        assertEquals(1, result.size());
        verify(roomAssignmentRepository).findByReservationIdOrderByAssignedAtAsc(1L);
    }

    // --- getAvailableRooms ---

    @Test
    void getAvailableRooms_shouldReturnVacantUnassigned() {
        RoomType rt1 = new RoomType();
        rt1.setId(1L);

        ReservationDetail detail = new ReservationDetail();
        detail.setRoomType(rt1);

        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setDetails(List.of(detail));

        Room roomA = new Room();
        roomA.setId(10L);
        roomA.setOccupancyStatus(OccupancyStatus.VACANT);

        Room roomB = new Room();
        roomB.setId(20L);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(roomAssignmentRepository.findByReservationIdOrderByAssignedAtAsc(1L))
                .thenReturn(List.of(new RoomAssignment() {{
                    setRoom(roomB);
                }}));
        when(roomRepository.findByRoomTypeIdAndOccupancyStatusAndActiveTrue(1L, OccupancyStatus.VACANT))
                .thenReturn(List.of(roomA, roomB));

        List<Room> result = roomAssignmentService.getAvailableRooms(1L);

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getId());
    }
}
