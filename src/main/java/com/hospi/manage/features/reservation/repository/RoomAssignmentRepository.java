package com.hospi.manage.features.reservation.repository;

import com.hospi.manage.features.reservation.entity.RoomAssignment;
import com.hospi.manage.features.reservation.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for {@link RoomAssignment} entity — provides room-to-reservation assignment queries.
 */
public interface RoomAssignmentRepository extends JpaRepository<RoomAssignment, Long> {

    List<RoomAssignment> findByReservationIdOrderByAssignedAtAsc(Long reservationId);

    /**
     * Find all assignments for a room that have a reservation in one of the given statuses.
     */
    List<RoomAssignment> findByRoomIdAndReservation_StatusIn(Long roomId, List<ReservationStatus> statuses);

    boolean existsByReservationIdAndRoomId(Long reservationId, Long roomId);

    long countByReservationIdAndRoom_RoomTypeId(Long reservationId, Long roomTypeId);

    void deleteByReservationId(Long reservationId);
}
