package com.hospi.manage.features.room.dto.response;

/** View model for a selected room type and count. */
public record RoomSelection(
        Long roomTypeId,
        Integer roomCount
) {
}
