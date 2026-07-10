package com.hospi.manage.features.dashboard.controller;

import com.hospi.manage.common.constant.Attributes;
import com.hospi.manage.features.invoice.service.RevenueService;
import com.hospi.manage.features.notification.service.NotificationService;
import com.hospi.manage.features.reservation.enums.ReservationStatus;
import com.hospi.manage.features.reservation.service.ReservationService;
import com.hospi.manage.features.room.enums.ConditionStatus;
import com.hospi.manage.features.room.enums.OccupancyStatus;
import com.hospi.manage.features.room.service.RoomService;
import com.hospi.manage.core.security.session.AccountPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Controller for the manager dashboard with real-time hotel operations data. */
@Controller
@RequestMapping("/manager")
@RequiredArgsConstructor
public class ManagerDashboardController {

    private final ReservationService reservationService;
    private final RoomService roomService;
    private final RevenueService revenueService;
    private final NotificationService notificationService;

    @GetMapping
    public String dashboard(@AuthenticationPrincipal AccountPrincipal principal, Model model) {
        model.addAttribute(Attributes.ACTIVE_SIDEBAR, "DASHBOARD");

        var today = LocalDate.now();
        var allRooms = roomService.findAll();

        // KPIs
        model.addAttribute("checkInsToday",
                reservationService.findByStatus(ReservationStatus.CONFIRMED).stream()
                        .filter(r -> r.getCheckInAt() != null && r.getCheckInAt().equals(today))
                        .count());
        model.addAttribute("checkOutsToday",
                reservationService.findByStatus(ReservationStatus.CHECKED_IN).stream()
                        .filter(r -> r.getCheckOutAt() != null && r.getCheckOutAt().equals(today))
                        .count());
        model.addAttribute("pendingCount",
                reservationService.findByStatus(ReservationStatus.PENDING).size());

        // Revenue this month
        var monthStart = today.withDayOfMonth(1);
        long daysSoFar = ChronoUnit.DAYS.between(monthStart, today) + 1;
        var monthEnd = monthStart.plusDays(daysSoFar - 1);
        var revenueView = revenueService.getRevenueView(monthStart, monthEnd, "this-month");
        model.addAttribute("revenueThisMonth", revenueView.totalRevenue());
        model.addAttribute("revenueChange", revenueView.changePercent());

        // Room status
        long occupied = allRooms.stream().filter(r -> r.getOccupancyStatus() == OccupancyStatus.OCCUPIED).count();
        long vacant = allRooms.stream().filter(r -> r.getOccupancyStatus() == OccupancyStatus.VACANT).count();
        long dirty = allRooms.stream().filter(r -> r.getConditionStatus() == ConditionStatus.DIRTY).count();
        long maintenance = allRooms.stream().filter(r -> r.getConditionStatus() == ConditionStatus.MAINTENANCE || !r.isActive()).count();
        model.addAttribute("occupiedRooms", occupied);
        model.addAttribute("vacantRooms", vacant);
        model.addAttribute("dirtyRooms", dirty);
        model.addAttribute("maintenanceRooms", maintenance);

        // Recent reservations (last 5)
        var recentReservations = new java.util.ArrayList<>(reservationService.findByStatuses(
                java.util.List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED,
                        ReservationStatus.CHECKED_IN, ReservationStatus.CHECKED_OUT)));
        recentReservations.sort((a, b) -> b.getCreatedAt() != null && a.getCreatedAt() != null
                ? b.getCreatedAt().compareTo(a.getCreatedAt()) : 0);
        model.addAttribute("recentReservations",
                recentReservations.stream().limit(5).toList());

        // User info & notifications
        if (principal != null) {
            model.addAttribute("staffName", principal.getAccount().getFullName());
            model.addAttribute("staffRole", principal.getAccount().getRole().name());
            model.addAttribute("unreadNotificationCount",
                    notificationService.getUnreadCount(principal.getAccount().getId()));
        }

        return "manager/dashboard";
    }
}
