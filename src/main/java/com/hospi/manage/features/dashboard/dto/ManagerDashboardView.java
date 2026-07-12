package com.hospi.manage.features.dashboard.dto;

import com.hospi.manage.features.dashboard.dto.response.RecentReservationView;
import com.hospi.manage.features.notification.dto.NotificationDTO;
import com.hospi.manage.features.room.dto.response.RoomTypeOccupancyView;

import java.math.BigDecimal;
import java.util.List;

/** View model for the manager dashboard page. */
public record ManagerDashboardView(
        long checkInsToday,
        long checkOutsToday,
        int pendingCount,
        BigDecimal revenueThisMonth,
        BigDecimal revenueChange,
        int totalRooms,
        int occupiedRooms,
        int vacantRooms,
        int dirtyRooms,
        int maintenanceRooms,
        List<RecentReservationView> recentReservations,
        List<RoomTypeOccupancyView> roomTypes,
        BigDecimal hotelRating,
        int hotelReviewCount,
        String staffName,
        String staffRole,
        int unreadNotificationCount,
        List<NotificationDTO> recentNotifications
) {}
