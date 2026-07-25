package com.hospi.manage.features.room.dto.response;

import com.hospi.manage.features.room.entity.RoomType;

/** View model for room type availability with entity reference and counts. */
public record RoomTypeAvailability(
        RoomType roomType,
        int totalRooms,
        int availableRooms
) {
}
