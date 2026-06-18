package com.hospi.manage.features.room.dto;

import com.hospi.manage.features.room.dto.room.RoomView;

import java.util.List;

public record FloorView(int number,
                        int roomCount,
                        List<RoomView> rooms) {
}
