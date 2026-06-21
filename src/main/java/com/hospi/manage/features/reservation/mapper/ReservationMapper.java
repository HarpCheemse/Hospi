package com.hospi.manage.features.reservation.mapper;

import com.hospi.manage.features.reservation.dto.ManageReservationView;
import com.hospi.manage.features.reservation.entity.Reservation;
import com.hospi.manage.features.reservation.entity.RoomAssignment;
import com.hospi.manage.features.reservation.entity.StayingGuest;
import com.hospi.manage.features.room.entity.Room;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReservationMapper {

    private ReservationMapper() {
    }

    public static ManageReservationView toManageView(
            Reservation reservation,
            List<StayingGuest> guests,
            List<RoomAssignment> assignedRooms,
            List<Room> availableRooms) {
        Map<Long, Integer> assignedCounts = assignedRooms.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getRoom().getRoomType().getId(),
                        Collectors.summingInt(a -> 1)));

        return new ManageReservationView(
                reservation, guests, assignedRooms, availableRooms, assignedCounts);
    }
}
