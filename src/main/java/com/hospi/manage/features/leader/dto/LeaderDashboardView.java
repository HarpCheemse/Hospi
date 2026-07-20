package com.hospi.manage.features.leader.dto;

import java.util.List;

/** View model for the leader dashboard page. */
public record LeaderDashboardView(
        long totalRooms,
        int dirtyRooms,
        int cleanRooms,
        int maintenanceRooms,
        int occupiedDirtyRooms,
        int vacantCleanRooms,
        int cleanPercent,
        List<StatusBreakdown> statusBreakdown,
        List<PriorityRoomView> priorityRooms
) {
    public record StatusBreakdown(String status, int count, String colorClass, String label) {}

    public record PriorityRoomView(Long id, String roomNumber, String roomTypeName, Short floorNumber) {}
}
