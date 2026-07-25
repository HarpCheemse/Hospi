package com.hospi.manage.features.room.mapper;

import com.hospi.manage.features.room.dto.response.RoomTypeView;
import com.hospi.manage.features.room.entity.RoomType;

/** Static mapping utility for room type entities to view models. */
public class RoomTypeMapper {

    private RoomTypeMapper() {}

    /**
     * Convert a {@link RoomType} entity to its view representation.
     *
     * @param roomType the room type to transform
     * @return a {@link RoomTypeView}
     */
    public static RoomTypeView toView(RoomType roomType) {
        return RoomTypeView.from(roomType);
    }
}
