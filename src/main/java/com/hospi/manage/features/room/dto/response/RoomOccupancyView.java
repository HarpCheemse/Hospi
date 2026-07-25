package com.hospi.manage.features.room.dto.response;

import com.hospi.manage.features.reservation.enums.ReservationStatus;
import com.hospi.manage.features.room.entity.Room;
import com.hospi.manage.features.room.enums.ConditionStatus;
import com.hospi.manage.features.room.enums.OccupancyStatus;

import java.time.LocalDate;
import java.util.List;

/** View model for room occupancy details. */
public record RoomOccupancyView(
        Long roomId,
        String roomNumber,
        String roomTypeName,
        OccupancyStatus occupancyStatus,
        ConditionStatus conditionStatus,
        Long reservationId,
        ReservationStatus reservationStatus,
        String guestName,
        LocalDate checkInAt,
        LocalDate checkOutAt,
        List<GuestView> stayingGuests
) {
    /** Create a room occupancy view for a vacant room. */
    public static RoomOccupancyView vacant(Room room) {
        return new RoomOccupancyView(
                room.getId(),
                room.getRoomNumber(),
                room.getRoomType() != null ? room.getRoomType().getName() : null,
                OccupancyStatus.VACANT,
                room.getConditionStatus(),
                null, null, null, null, null, List.of()
        );
    }
}
