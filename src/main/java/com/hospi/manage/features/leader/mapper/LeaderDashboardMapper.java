package com.hospi.manage.features.leader.mapper;

import com.hospi.manage.features.leader.dto.LeaderDashboardView;
import com.hospi.manage.features.leader.dto.LeaderDashboardView.StatusBreakdown;
import com.hospi.manage.features.room.entity.Room;
import com.hospi.manage.features.room.enums.ConditionStatus;
import com.hospi.manage.features.room.enums.OccupancyStatus;

import java.util.ArrayList;
import java.util.List;

/** Static mapping utilities for building the leader dashboard view model. */
public final class LeaderDashboardMapper {

    private LeaderDashboardMapper() {}

    /** Build dashboard view from all active rooms. */
    public static LeaderDashboardView toDashboardView(List<Room> allRooms) {
        var activeRooms = allRooms.stream()
                .filter(Room::isActive)
                .toList();

        List<Room> nonMaintenance = activeRooms.stream()
                .filter(r -> r.getConditionStatus() != ConditionStatus.MAINTENANCE)
                .toList();

        long totalRooms = activeRooms.size();
        long maintenanceCount = activeRooms.stream()
                .filter(r -> r.getConditionStatus() == ConditionStatus.MAINTENANCE)
                .count();
        long cleanCount = nonMaintenance.stream()
                .filter(r -> r.getConditionStatus() == ConditionStatus.CLEAN)
                .count();
        long dirtyCount = nonMaintenance.stream()
                .filter(r -> r.getConditionStatus() == ConditionStatus.DIRTY)
                .count();
        long occupiedDirty = activeRooms.stream()
                .filter(r -> r.getConditionStatus() == ConditionStatus.DIRTY
                        && r.getOccupancyStatus() == OccupancyStatus.OCCUPIED)
                .count();
        long vacantClean = activeRooms.stream()
                .filter(r -> r.getConditionStatus() == ConditionStatus.CLEAN
                        && r.getOccupancyStatus() == OccupancyStatus.VACANT)
                .count();

        int cleanPercent = totalRooms > 0
                ? (int) Math.round((double) cleanCount / totalRooms * 100)
                : 0;

        var breakdown = new ArrayList<StatusBreakdown>();
        breakdown.add(new StatusBreakdown("CLEAN", (int) cleanCount, "bg-success", "Clean"));
        breakdown.add(new StatusBreakdown("DIRTY", (int) dirtyCount, "bg-danger", "Dirty"));
        breakdown.add(new StatusBreakdown("MAINTENANCE", (int) maintenanceCount, "bg-warning", "Maintenance"));

        var priorityRooms = activeRooms.stream()
                .filter(r -> r.getConditionStatus() == ConditionStatus.DIRTY
                        && r.getOccupancyStatus() == OccupancyStatus.OCCUPIED)
                .map(r -> new LeaderDashboardView.PriorityRoomView(
                        r.getId(),
                        r.getRoomNumber(),
                        r.getRoomType() != null ? r.getRoomType().getName() : null,
                        r.getFloorNumber()))
                .toList();

        return new LeaderDashboardView(
                totalRooms,
                (int) dirtyCount,
                (int) cleanCount,
                (int) maintenanceCount,
                (int) occupiedDirty,
                (int) vacantClean,
                cleanPercent,
                breakdown,
                priorityRooms
        );
    }
}
