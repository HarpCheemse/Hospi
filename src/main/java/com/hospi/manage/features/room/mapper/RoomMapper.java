package com.hospi.manage.features.room.mapper;

import com.hospi.manage.features.room.dto.response.FloorView;
import com.hospi.manage.features.room.dto.response.RoomView;
import com.hospi.manage.features.room.entity.Room;

import java.util.List;

public class RoomMapper {

    private RoomMapper() {
    }

    public static FloorView toFloorView(int floorNumber, List<Room> rooms) {
        List<RoomView> roomViews = rooms.stream()
                .map(room -> new RoomView(
                        room.getId(),
                        room.getRoomNumber(),
                        room.getRoomType() != null
                                ? room.getRoomType().getName()
                                : null,
                        room.getOccupancyStatus(),
                        room.getConditionStatus()
                ))
                .toList();

        return new FloorView(
                floorNumber,
                roomViews.size(),
                roomViews
        );
    }
}
