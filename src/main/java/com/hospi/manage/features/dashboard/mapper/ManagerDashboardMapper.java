package com.hospi.manage.features.dashboard.mapper;

import com.hospi.manage.features.dashboard.dto.ManagerDashboardView;
import com.hospi.manage.features.dashboard.dto.response.RecentReservationView;
import com.hospi.manage.features.hotel.entity.Hotel;
import com.hospi.manage.features.invoice.dto.response.RevenueView;
import com.hospi.manage.features.notification.dto.NotificationDTO;
import com.hospi.manage.features.reservation.entity.Reservation;
import com.hospi.manage.features.reservation.enums.ReservationStatus;
import com.hospi.manage.features.room.dto.response.RoomTypeOccupancyView;
import com.hospi.manage.features.room.entity.Room;
import com.hospi.manage.features.room.enums.ConditionStatus;
import com.hospi.manage.features.room.enums.OccupancyStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

/** Static mapping utilities for building the manager dashboard view model. */
public class ManagerDashboardMapper {

    private ManagerDashboardMapper() {}

    /** Build the full dashboard view from raw service data. */
    public static ManagerDashboardView toView(
            LocalDate today,
            List<Reservation> confirmed,
            List<Reservation> checkedIn,
            List<Reservation> pendingReservations,
            List<Reservation> allRecent,
            RevenueView revenueView,
            List<Room> allRooms,
            Hotel hotel,
            String staffName,
            String staffRole,
            int unreadNotificationCount,
            List<NotificationDTO> recentNotifications
    ) {
        long checkInsToday = confirmed.stream()
                .filter(r -> r.getCheckInAt() != null && r.getCheckInAt().equals(today))
                .count();

        long checkOutsToday = checkedIn.stream()
                .filter(r -> r.getCheckOutAt() != null && r.getCheckOutAt().equals(today))
                .count();

        int pendingCount = pendingReservations.size();

        int totalRooms = allRooms.size();
        int occupiedRooms = (int) allRooms.stream()
                .filter(r -> r.getOccupancyStatus() == OccupancyStatus.OCCUPIED).count();
        int vacantRooms = (int) allRooms.stream()
                .filter(r -> r.getOccupancyStatus() == OccupancyStatus.VACANT).count();
        int dirtyRooms = (int) allRooms.stream()
                .filter(r -> r.getConditionStatus() == ConditionStatus.DIRTY).count();
        int maintenanceRooms = (int) allRooms.stream()
                .filter(r -> r.getConditionStatus() == ConditionStatus.MAINTENANCE || !r.isActive()).count();

        var allSorted = new ArrayList<>(allRecent);
        allSorted.sort((a, b) -> b.getCreatedAt() != null && a.getCreatedAt() != null
                ? b.getCreatedAt().compareTo(a.getCreatedAt()) : 0);

        var recentReservations = allSorted.stream().limit(5)
                .map(r -> new RecentReservationView(
                        r.getGuestName(), r.getCheckInAt(), r.getCheckOutAt(),
                        r.getStatus().name(), r.getTotalPrice()))
                .toList();

        var roomTypes = buildRoomTypeOccupancy(allRooms);

        return new ManagerDashboardView(
                checkInsToday, checkOutsToday, pendingCount,
                revenueView.totalRevenue(), revenueView.changePercent(),
                totalRooms, occupiedRooms, vacantRooms, dirtyRooms, maintenanceRooms,
                recentReservations, roomTypes,
                hotel.getAverageRating(), hotel.getReviewCount(),
                staffName, staffRole, unreadNotificationCount, recentNotifications
        );
    }

    private static List<RoomTypeOccupancyView> buildRoomTypeOccupancy(List<Room> rooms) {
        var byType = new LinkedHashMap<String, int[]>();
        for (var room : rooms) {
            var name = room.getRoomType() != null ? room.getRoomType().getName() : "Unknown";
            byType.computeIfAbsent(name, k -> new int[2]);
            byType.get(name)[0]++;
            if (room.getOccupancyStatus() == OccupancyStatus.OCCUPIED) {
                byType.get(name)[1]++;
            }
        }
        return byType.entrySet().stream()
                .map(e -> new RoomTypeOccupancyView(e.getKey(), e.getValue()[0], e.getValue()[1]))
                .sorted((a, b) -> b.totalRooms() - a.totalRooms())
                .toList();
    }
}
