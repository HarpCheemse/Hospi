package com.hospi.manage.features.manager.room.dto;

import java.util.List;

public record FloorView(int number,
                        int roomCount,
                        List<RoomView> rooms) {
}
