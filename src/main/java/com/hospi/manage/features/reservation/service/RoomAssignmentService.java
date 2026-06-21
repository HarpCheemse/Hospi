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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class RoomAssignmentService {

    private final RoomAssignmentRepository roomAssignmentRepository;
    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;

    public RoomAssignmentService(RoomAssignmentRepository roomAssignmentRepository,
                                 ReservationRepository reservationRepository,
                                 RoomRepository roomRepository) {
        this.roomAssignmentRepository = roomAssignmentRepository;
        this.reservationRepository = reservationRepository;
        this.roomRepository = roomRepository;
    }

    public List<RoomAssignment> getAssignedRooms(Long reservationId) {
        return roomAssignmentRepository.findByReservationIdOrderByAssignedAtAsc(reservationId);
    }

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

        room.setOccupancyStatus(OccupancyStatus.OCCUPIED);
        roomRepository.save(room);

        RoomAssignment assignment = new RoomAssignment();
        assignment.setReservation(reservation);
        assignment.setRoom(room);
        return roomAssignmentRepository.save(assignment);
    }

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
}
