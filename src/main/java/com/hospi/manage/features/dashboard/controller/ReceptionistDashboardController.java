package com.hospi.manage.features.dashboard.controller;

import com.hospi.manage.common.constant.Attributes;
import com.hospi.manage.features.dashboard.dto.ReceptionistDashboardView;
import com.hospi.manage.features.dashboard.mapper.ReceptionistDashboardMapper;
import com.hospi.manage.features.payment.entity.Payment;
import com.hospi.manage.features.reservation.entity.Reservation;
import com.hospi.manage.features.reservation.entity.RoomAssignment;
import com.hospi.manage.features.reservation.enums.ReservationStatus;
import com.hospi.manage.features.reservation.service.ReservationService;
import com.hospi.manage.features.reservation.service.RoomAssignmentService;
import com.hospi.manage.features.reservation.service.StayingGuestService;
import com.hospi.manage.features.room.entity.Room;
import com.hospi.manage.features.room.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Controller for the receptionist dashboard landing page. */
@Controller
@RequestMapping("/receptionist")
@RequiredArgsConstructor
public class ReceptionistDashboardController {

    private final ReservationService reservationService;
    private final RoomService roomService;
    private final RoomAssignmentService roomAssignmentService;
    private final StayingGuestService stayingGuestService;

    /** Show the receptionist dashboard page. */
    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute(Attributes.ACTIVE_SIDEBAR, "DASHBOARD");

        LocalDate today = LocalDate.now();

        List<Reservation> confirmed = reservationService.findByStatus(ReservationStatus.CONFIRMED);
        List<Reservation> checkedIn = reservationService.findByStatus(ReservationStatus.CHECKED_IN);

        List<Reservation> checkedOutToday = reservationService.findByStatusAndCheckOutAt(
                ReservationStatus.CHECKED_OUT, today
        );

        List<Room> allRooms = roomService.findAll();

        List<Reservation> activeReservations = new ArrayList<>();
        activeReservations.addAll(confirmed);
        activeReservations.addAll(checkedIn);
        activeReservations.addAll(checkedOutToday);

        List<Long> activeResIds = activeReservations.stream()
                .map(Reservation::getId)
                .toList();

        List<RoomAssignment> assignments = roomAssignmentService.findAssignmentsByReservationIds(activeResIds);
        Map<Long, List<RoomAssignment>> assignmentsByReservationId = assignments.stream()
                .collect(Collectors.groupingBy(a -> a.getReservation().getId()));

        List<Payment> payments = reservationService.findPaymentsByReservationIds(activeResIds);
        Map<Long, BigDecimal> paymentsByReservationId = payments.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getReservation().getId(),
                        Collectors.reducing(BigDecimal.ZERO, Payment::getAmount, BigDecimal::add)
                ));

        Map<Long, Integer> guestCountsByReservationId = activeReservations.stream()
                .collect(Collectors.toMap(
                        Reservation::getId,
                        r -> stayingGuestService.getGuestCount(r.getId())
                ));

        ReceptionistDashboardView view = ReceptionistDashboardMapper.toView(
                today,
                confirmed,
                checkedIn,
                checkedOutToday,
                allRooms,
                assignmentsByReservationId,
                paymentsByReservationId,
                guestCountsByReservationId
        );

        model.addAttribute(Attributes.VIEW, view);

        return "receptionist/dashboard";
    }
}
