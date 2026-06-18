package com.hospi.manage.features.room.dto.room;

import com.hospi.manage.features.room.entity.RoomType;

public record RoomInventory(RoomType roomType, Integer totalRooms) {
}