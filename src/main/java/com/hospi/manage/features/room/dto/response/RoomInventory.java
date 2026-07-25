package com.hospi.manage.features.room.dto.response;

/** View model for room type inventory counts. */
public record RoomInventory(Long roomTypeId, String roomTypeName, Integer totalRooms) {
}
