package com.hospi.manage.features.reservation.dto;

import com.hospi.manage.features.reservation.entity.Reservation;
import com.hospi.manage.features.reservation.entity.RoomAssignment;
import com.hospi.manage.features.reservation.entity.StayingGuest;
import com.hospi.manage.features.room.entity.Room;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

public record ManageReservationView(
        Reservation reservation,
        List<StayingGuest> guests,
        List<RoomAssignment> assignedRooms,
        List<Room> availableRooms,
        Map<Long, Integer> assignedCounts
) {
    public long nights() {
        return ChronoUnit.DAYS.between(reservation.getCheckInAt(), reservation.getCheckOutAt());
    }

    public int assignedCount(Long roomTypeId) {
        return assignedCounts.getOrDefault(roomTypeId, 0);
    }
}
