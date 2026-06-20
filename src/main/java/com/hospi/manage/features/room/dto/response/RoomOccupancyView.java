package com.hospi.manage.features.room.dto.response;

import com.hospi.manage.features.room.entity.Room;
import com.hospi.manage.features.room.enums.ConditionStatus;
import com.hospi.manage.features.room.enums.OccupancyStatus;

import java.time.LocalDate;
import java.util.List;

public record RoomOccupancyView(
        Long roomId,
        String roomNumber,
        String roomTypeName,
        OccupancyStatus occupancyStatus,
        ConditionStatus conditionStatus,
        String guestName,
        LocalDate checkInAt,
        LocalDate checkOutAt,
        List<GuestView> stayingGuests
) {
    public static RoomOccupancyView vacant(Room room) {
        return new RoomOccupancyView(
                room.getId(),
                room.getRoomNumber(),
                room.getRoomType() != null ? room.getRoomType().getName() : null,
                OccupancyStatus.VACANT,
                room.getConditionStatus(),
                null, null, null, List.of()
        );
    }
}
