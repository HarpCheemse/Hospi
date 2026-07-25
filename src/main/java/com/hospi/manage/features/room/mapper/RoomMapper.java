package com.hospi.manage.features.room.mapper;

import com.hospi.manage.features.room.dto.response.FloorView;
import com.hospi.manage.features.room.dto.response.RoomView;
import com.hospi.manage.features.room.entity.Room;

import java.util.List;

/** Static mapping utilities for room entities to view models. */
public class RoomMapper {

    private RoomMapper() {
    }

    /**
     * Build a {@link FloorView} for a given floor containing its room summaries.
     *
     * @param floorNumber the floor number
     * @param rooms       rooms on this floor
     * @return a floor view model
     */
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
