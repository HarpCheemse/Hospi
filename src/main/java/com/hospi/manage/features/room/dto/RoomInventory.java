package com.hospi.manage.features.room.dto;

import com.hospi.manage.features.room.entity.RoomType;

public record RoomInventory(RoomType roomType, Integer totalRooms) {
}