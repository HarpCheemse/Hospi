package com.hospi.manage.features.room.dto.room_type;

import com.hospi.manage.features.room.entity.RoomType;

public record RoomTypeAvailability(
        RoomType roomType,
        int totalRooms,
        int availableRooms
) {
}