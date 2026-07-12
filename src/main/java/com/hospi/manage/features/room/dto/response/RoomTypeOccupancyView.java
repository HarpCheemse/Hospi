package com.hospi.manage.features.room.dto.response;

/** View model for room type occupancy on the manager dashboard. */
public record RoomTypeOccupancyView(
        String roomTypeName,
        int totalRooms,
        int occupiedRooms
) {
    public int availableRooms() {
        return totalRooms - occupiedRooms;
    }

    public int occupancyPercent() {
        return totalRooms > 0 ? occupiedRooms * 100 / totalRooms : 0;
    }
}
