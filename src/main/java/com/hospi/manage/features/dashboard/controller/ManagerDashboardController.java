package com.hospi.manage.features.dashboard.controller;

import com.hospi.manage.common.constant.Attributes;
import com.hospi.manage.features.dashboard.mapper.ManagerDashboardMapper;
import com.hospi.manage.features.hotel.service.HotelService;
import com.hospi.manage.features.invoice.service.RevenueService;
import com.hospi.manage.features.notification.service.NotificationService;
import com.hospi.manage.features.reservation.enums.ReservationStatus;
import com.hospi.manage.features.reservation.service.ReservationService;
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
import java.util.List;

/** Controller for the manager dashboard with real-time hotel operations data. */
@Controller
@RequestMapping("/manager")
@RequiredArgsConstructor
public class ManagerDashboardController {

    private final ReservationService reservationService;
    private final RoomService roomService;
    private final RevenueService revenueService;
    private final HotelService hotelService;
    private final NotificationService notificationService;

    @GetMapping
    public String dashboard(@AuthenticationPrincipal AccountPrincipal principal, Model model) {
        model.addAttribute(Attributes.ACTIVE_SIDEBAR, "DASHBOARD");

        var today = LocalDate.now();
        var confirmed = reservationService.findByStatus(ReservationStatus.CONFIRMED);
        var checkedIn = reservationService.findByStatus(ReservationStatus.CHECKED_IN);
        var pending = reservationService.findByStatus(ReservationStatus.PENDING);
        var allRecent = reservationService.findByStatuses(List.of(
                ReservationStatus.PENDING, ReservationStatus.CONFIRMED,
                ReservationStatus.CHECKED_IN, ReservationStatus.CHECKED_OUT));
        var allRooms = roomService.findAll();

        var monthStart = today.withDayOfMonth(1);
        long daysSoFar = ChronoUnit.DAYS.between(monthStart, today) + 1;
        var revenueView = revenueService.getRevenueView(
                monthStart, monthStart.plusDays(daysSoFar - 1), "this-month");

        var hotel = hotelService.find();

        String staffName = null;
        String staffRole = null;
        int unreadNotificationCount = 0;
        var recentNotifications = List.<com.hospi.manage.features.notification.dto.NotificationDTO>of();

        if (principal != null) {
            var account = principal.getAccount();
            staffName = account.getFullName();
            staffRole = account.getRole().name();
            unreadNotificationCount = notificationService.getUnreadCount(account.getId());
            recentNotifications = notificationService.getNotifications(
                    account.getId(), PageRequest.of(0, 4)).getContent();
        }

        model.addAttribute(Attributes.VIEW, ManagerDashboardMapper.toView(
                today, confirmed, checkedIn, pending, allRecent, revenueView,
                allRooms, hotel, staffName, staffRole, unreadNotificationCount, recentNotifications));

        return "manager/dashboard";
    }
}
