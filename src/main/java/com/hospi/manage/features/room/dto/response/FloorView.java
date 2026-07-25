package com.hospi.manage.features.room.dto.response;

import java.util.List;

/** View model for a floor with its rooms. */
public record FloorView(int number,
                        int roomCount,
                        List<RoomView> rooms) {
}
