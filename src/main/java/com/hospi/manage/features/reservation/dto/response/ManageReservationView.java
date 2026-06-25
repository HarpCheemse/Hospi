package com.hospi.manage.features.reservation.dto.response;

import com.hospi.manage.features.reservation.entity.Reservation;
import com.hospi.manage.features.reservation.entity.RoomAssignment;
import com.hospi.manage.features.reservation.entity.StayingGuest;
import com.hospi.manage.features.room.entity.Room;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

/** View model for the reservation management page with guests, assignments, and available rooms. */
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
