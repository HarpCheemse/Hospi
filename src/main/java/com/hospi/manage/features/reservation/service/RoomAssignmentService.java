package com.hospi.manage.features.reservation.service;

import com.hospi.manage.common.exception.ResourceNotFoundException;
import com.hospi.manage.features.reservation.entity.Reservation;
import com.hospi.manage.features.reservation.entity.ReservationDetail;
import com.hospi.manage.features.reservation.entity.RoomAssignment;
import com.hospi.manage.features.reservation.repository.ReservationRepository;
import com.hospi.manage.features.reservation.repository.RoomAssignmentRepository;
import com.hospi.manage.features.room.entity.Room;
import com.hospi.manage.features.room.enums.OccupancyStatus;
import com.hospi.manage.features.room.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** Business logic for assigning and removing physical rooms to and from reservations. */
@Service
@Transactional
@RequiredArgsConstructor
public class RoomAssignmentService {

    private final RoomAssignmentRepository roomAssignmentRepository;
    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;

    /** Return all room assignments for a reservation. */
    public List<RoomAssignment> getAssignedRooms(Long reservationId) {
        return roomAssignmentRepository.findByReservationIdOrderByAssignedAtAsc(reservationId);
    }

    /** Return the total number of assigned rooms for a reservation. */
    public int getAssignedRoomCount(Long reservationId) {
        return (int) roomAssignmentRepository.countByReservationId(reservationId);
    }

    /** Return all vacant rooms matching the reservation's room types. */
    public List<Room> getAvailableRooms(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation"));

        Set<Long> typeIds = reservation.getDetails().stream()
                .map(d -> d.getRoomType().getId())
                .collect(Collectors.toSet());

        Set<Long> alreadyAssigned = roomAssignmentRepository
                .findByReservationIdOrderByAssignedAtAsc(reservationId)
                .stream()
                .map(a -> a.getRoom().getId())
                .collect(Collectors.toSet());

        List<Room> available = new ArrayList<>();
        for (Long typeId : typeIds) {
            List<Room> rooms = roomRepository.findByRoomTypeIdAndOccupancyStatusAndActiveTrue(
                    typeId, OccupancyStatus.VACANT);
            for (Room room : rooms) {
                if (!alreadyAssigned.contains(room.getId())) {
                    available.add(room);
                }
            }
        }

        return available;
    }

    /** Assign a physical room to a reservation and mark it occupied. */
    public RoomAssignment assignRoom(Long reservationId, Long roomId) {
        if (roomAssignmentRepository.existsByReservationIdAndRoomId(reservationId, roomId)) {
            throw new IllegalStateException("Room is already assigned to this reservation");
        }

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation"));

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room"));

        if (room.getOccupancyStatus() != OccupancyStatus.VACANT) {
            throw new IllegalStateException("Room is not vacant");
        }

        Long roomTypeId = room.getRoomType().getId();
        ReservationDetail detail = reservation.getDetails().stream()
                .filter(d -> d.getRoomType().getId().equals(roomTypeId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Room type not requested in this reservation"));

        long assignedCount = roomAssignmentRepository.countByReservationIdAndRoom_RoomTypeId(reservationId, roomTypeId);
        if (assignedCount >= detail.getRoomCount()) {
            throw new IllegalStateException("Cannot assign more rooms of type " + room.getRoomType().getName());
        }

        room.setOccupancyStatus(OccupancyStatus.OCCUPIED);
        roomRepository.save(room);

        RoomAssignment assignment = new RoomAssignment();
        assignment.setReservation(reservation);
        assignment.setRoom(room);
        return roomAssignmentRepository.save(assignment);
    }

    /** Remove a room assignment and mark the room vacant. */
    public void removeAssignment(Long reservationId, Long assignmentId) {
        RoomAssignment assignment = roomAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment"));

        if (!assignment.getReservation().getId().equals(reservationId)) {
            throw new IllegalArgumentException("Assignment does not belong to this reservation");
        }

        Room room = assignment.getRoom();
        room.setOccupancyStatus(OccupancyStatus.VACANT);
        roomRepository.save(room);

        roomAssignmentRepository.delete(assignment);
    }

    /** Find all assignments for the given reservation IDs. */
    public List<RoomAssignment> findAssignmentsByReservationIds(List<Long> reservationIds) {
        if (reservationIds == null || reservationIds.isEmpty()) {
            return List.of();
        }
        return roomAssignmentRepository.findByReservationIdIn(reservationIds);
    }

    /** Set all rooms assigned to this reservation to VACANT during checkout. */
    public void vacateAllReservationRooms(Long reservationId) {
        List<RoomAssignment> assignments = roomAssignmentRepository
                .findByReservationIdOrderByAssignedAtAsc(reservationId);
        for (RoomAssignment a : assignments) {
            Room room = a.getRoom();
            room.setOccupancyStatus(OccupancyStatus.VACANT);
            roomRepository.save(room);
        }
    }
}
