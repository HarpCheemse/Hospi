package com.hospi.manage.features.dashboard.controller;

import com.hospi.manage.common.constant.Attributes;
import com.hospi.manage.features.hotel.service.HotelService;
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
import java.util.ArrayList;
import java.util.Comparator;
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
        var allRooms = roomService.findAll();

        addKpis(model, today, allRooms);
        addRoomStats(model, allRooms);
        addRecentReservations(model);
        addHotelRating(model);
        addUserInfo(model, principal);

        return "manager/dashboard";
    }

    private void addKpis(Model model, LocalDate today, List<?> allRooms) {
        model.addAttribute(Attributes.CHECK_INS_TODAY,
                reservationService.findByStatus(ReservationStatus.CONFIRMED).stream()
                        .filter(r -> r.getCheckInAt() != null && r.getCheckInAt().equals(today))
                        .count());
        model.addAttribute(Attributes.CHECK_OUTS_TODAY,
                reservationService.findByStatus(ReservationStatus.CHECKED_IN).stream()
                        .filter(r -> r.getCheckOutAt() != null && r.getCheckOutAt().equals(today))
                        .count());
        model.addAttribute(Attributes.PENDING_COUNT,
                reservationService.findByStatus(ReservationStatus.PENDING).size());

        var monthStart = today.withDayOfMonth(1);
        long daysSoFar = ChronoUnit.DAYS.between(monthStart, today) + 1;
        var revenueView = revenueService.getRevenueView(
                monthStart, monthStart.plusDays(daysSoFar - 1), "this-month");
        model.addAttribute(Attributes.REVENUE_THIS_MONTH, revenueView.totalRevenue());
        model.addAttribute(Attributes.REVENUE_CHANGE, revenueView.changePercent());
    }

    private void addRoomStats(Model model, List<?> allRooms) {
        model.addAttribute(Attributes.TOTAL_ROOMS, allRooms.size());
        model.addAttribute(Attributes.OCCUPIED_ROOMS,
                allRooms.stream().filter(r -> ((com.hospi.manage.features.room.entity.Room) r)
                        .getOccupancyStatus() == OccupancyStatus.OCCUPIED).count());
        model.addAttribute(Attributes.VACANT_ROOMS,
                allRooms.stream().filter(r -> ((com.hospi.manage.features.room.entity.Room) r)
                        .getOccupancyStatus() == OccupancyStatus.VACANT).count());
        model.addAttribute(Attributes.DIRTY_ROOMS,
                allRooms.stream().filter(r -> ((com.hospi.manage.features.room.entity.Room) r)
                        .getConditionStatus() == ConditionStatus.DIRTY).count());
        model.addAttribute(Attributes.MAINTENANCE_ROOMS,
                allRooms.stream().filter(r -> ((com.hospi.manage.features.room.entity.Room) r)
                        .getConditionStatus() == ConditionStatus.MAINTENANCE
                        || !((com.hospi.manage.features.room.entity.Room) r).isActive()).count());
    }

    private void addRecentReservations(Model model) {
        var all = new ArrayList<>(reservationService.findByStatuses(
                List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED,
                        ReservationStatus.CHECKED_IN, ReservationStatus.CHECKED_OUT)));
        all.sort((a, b) -> b.getCreatedAt() != null && a.getCreatedAt() != null
                ? b.getCreatedAt().compareTo(a.getCreatedAt()) : 0);
        model.addAttribute(Attributes.RECENT_RESERVATIONS, all.stream().limit(5).toList());
    }

    private void addHotelRating(Model model) {
        var hotel = hotelService.find();
        model.addAttribute("hotelRating", hotel.getAverageRating());
        model.addAttribute("hotelReviewCount", hotel.getReviewCount());
    }

    private void addUserInfo(Model model, AccountPrincipal principal) {
        if (principal == null) return;
        var account = principal.getAccount();
        model.addAttribute(Attributes.STAFF_NAME, account.getFullName());
        model.addAttribute(Attributes.STAFF_ROLE, account.getRole().name());
        model.addAttribute(Attributes.UNREAD_NOTIFICATION_COUNT,
                notificationService.getUnreadCount(account.getId()));
        model.addAttribute(Attributes.RECENT_NOTIFICATIONS,
                notificationService.getNotifications(account.getId(), PageRequest.of(0, 4))
                        .getContent());
    }
}
