package com.hospi.manage.features.reservation.dto.response;

import com.hospi.manage.features.room.entity.Room;

/**
 * View model for an available room that can be assigned.
 * No JPA entities are exposed.
 */
public record AvailableRoomView(
        Long id,
        String roomNumber,
        Long roomTypeId,
        String roomTypeName,
        Short floorNumber
) {
    /** Create from a JPA entity. */
    public static AvailableRoomView from(Room room) {
        return new AvailableRoomView(
                room.getId(),
                room.getRoomNumber(),
                room.getRoomType().getId(),
                room.getRoomType().getName(),
                room.getFloorNumber()
        );
    }
}
