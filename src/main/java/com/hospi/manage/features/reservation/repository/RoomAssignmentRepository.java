package com.hospi.manage.features.reservation.repository;

import com.hospi.manage.features.reservation.entity.RoomAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomAssignmentRepository extends JpaRepository<RoomAssignment, Long> {

    List<RoomAssignment> findByReservationIdOrderByAssignedAtAsc(Long reservationId);

    boolean existsByReservationIdAndRoomId(Long reservationId, Long roomId);

    void deleteByReservationId(Long reservationId);
}
