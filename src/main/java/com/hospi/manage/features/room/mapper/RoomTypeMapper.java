package com.hospi.manage.features.room.mapper;

import com.hospi.manage.features.room.dto.response.RoomTypeView;
import com.hospi.manage.features.room.entity.RoomType;

public class RoomTypeMapper {

    public static RoomTypeView toView(RoomType roomType) {
        return RoomTypeView.from(roomType);
    }
}
