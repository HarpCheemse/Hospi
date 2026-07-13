package com.hospi.manage.features.reservation.controller;

import com.hospi.manage.common.constant.Attributes;
import com.hospi.manage.features.payment.service.PaymentService;
import com.hospi.manage.features.reservation.dto.response.ActiveBookingsView;
import com.hospi.manage.features.reservation.dto.response.ReservationSummaryView;
import com.hospi.manage.features.reservation.entity.Reservation;
import com.hospi.manage.features.reservation.enums.ReservationStatus;
import com.hospi.manage.features.reservation.mapper.ManagerReservationMapper;
import com.hospi.manage.features.reservation.mapper.ReservationMapper;
import com.hospi.manage.features.reservation.service.ReservationService;
import com.hospi.manage.features.reservation.service.StayingGuestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Controller for the manager reservation overview page (read-only + cancel/refund authority). */
@Controller
@RequestMapping("/manager/reservations")
@RequiredArgsConstructor
@Slf4j
public class ManagerReservationController {

    private final ReservationService reservationService;
    private final StayingGuestService stayingGuestService;
    private final PaymentService paymentService;

    private static final int PAGE_SIZE = 10;

    @ModelAttribute
    void addCommonAttributes(Model model) {
        model.addAttribute(Attributes.ACTIVE_SIDEBAR, "RESERVATIONS");
    }

    @GetMapping
    String list(
            @RequestParam(required = false) String scope,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(name = "page", defaultValue = "0") int page,
            Model model) {

        LocalDate today = LocalDate.now();

        List<ReservationStatus> statuses = (status != null && !status.isBlank())
                ? List.of(ReservationStatus.valueOf(status))
                : Arrays.asList(ReservationStatus.values());

        if ((scope == null || scope.equals("all")) && (status != null && !status.isBlank())) {
            var paged = reservationService.findFiltered(statuses, search, null, PageRequest.of(page, PAGE_SIZE));
            var view = ReservationMapper.toActiveBookingsView(paged, status, null, search);
            var payments = paymentService.getPaymentsByReservationId(
                    paged.getContent().stream().map(Reservation::getId).toList());
            model.addAttribute(Attributes.PAYMENT_STATUSES,
                    ManagerReservationMapper.buildPaymentStatusMap(paged.getContent(), payments));
            addToModel(model, view, scope, status, search);
            return "manager/reservation/list";
        }

        List<Reservation> all = reservationService.findByStatuses(statuses);

        if (scope != null && !scope.equals("all")) {
            all = ManagerReservationMapper.filterByScope(all, scope, today);
        }

        if (search != null && !search.isBlank()) {
            all = ManagerReservationMapper.filterBySearch(all, search);
        }

        all = new ArrayList<>(all);
        all.sort((a, b) -> {
            if (a.getCheckInAt() == null) return 1;
            if (b.getCheckInAt() == null) return -1;
            return b.getCheckInAt().compareTo(a.getCheckInAt());
        });

        var sublist = ManagerReservationMapper.paginate(all, page, PAGE_SIZE);
        var paged = new PageImpl<>(sublist, PageRequest.of(page, PAGE_SIZE), all.size());
        var view = ReservationMapper.toActiveBookingsView(paged, status, null, search);

        var payments = paymentService.getPaymentsByReservationId(
                paged.getContent().stream().map(Reservation::getId).toList());
        model.addAttribute(Attributes.PAYMENT_STATUSES,
                ManagerReservationMapper.buildPaymentStatusMap(paged.getContent(), payments));
        addToModel(model, view, scope, status, search);
        return "manager/reservation/list";
    }

    private void addToModel(Model model, ActiveBookingsView view, String scope, String status, String search) {
        LocalDate today = LocalDate.now();
        model.addAttribute(Attributes.VIEW, view);
        model.addAttribute(Attributes.ALL_STATUSES, ReservationStatus.values());
        model.addAttribute(Attributes.ACTIVE_SCOPE, scope != null ? scope : "all");
        model.addAttribute(Attributes.ACTIVE_STATUS, status);
        model.addAttribute(Attributes.PENDING_COUNT, reservationService.findByStatus(ReservationStatus.PENDING).size());
        model.addAttribute(Attributes.ARRIVING_TODAY,
                ManagerReservationMapper.countByDate(
                        reservationService.findByStatus(ReservationStatus.CONFIRMED), today, "arriving")
                + ManagerReservationMapper.countByDate(
                        reservationService.findByStatus(ReservationStatus.CHECKED_IN), today, "arriving"));
        model.addAttribute(Attributes.DEPARTING_TODAY,
                ManagerReservationMapper.countByDate(
                        reservationService.findByStatus(ReservationStatus.CHECKED_IN), today, "departing"));
        model.addAttribute(Attributes.IN_HOUSE_COUNT,
                reservationService.findByStatus(ReservationStatus.CHECKED_IN).size());
    }

    @GetMapping("/{id}")
    String detail(@PathVariable Long id, Model model) {
        var reservation = reservationService.findById(id);
        model.addAttribute(Attributes.RESERVATION, ReservationSummaryView.from(reservation));
        model.addAttribute(Attributes.GUESTS, stayingGuestService.getGuests(id));
        model.addAttribute(Attributes.PAYMENTS, paymentService.getPaymentsByReservationId(id));
        model.addAttribute(Attributes.NIGHTS, reservation.getCheckInAt() != null && reservation.getCheckOutAt() != null
                ? ChronoUnit.DAYS.between(reservation.getCheckInAt(), reservation.getCheckOutAt())
                : 0);
        return "manager/reservation/detail";
    }

    @PostMapping("/{id}/cancel")
    String cancel(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            reservationService.cancelPendingReservation(id);
            redirect.addFlashAttribute(Attributes.SUCCESS, "Reservation cancelled.");
        } catch (IllegalStateException e) {
            redirect.addFlashAttribute(Attributes.ERROR, e.getMessage());
        }
        return "redirect:/manager/reservations";
    }

    @PostMapping("/{id}/refund")
    String refund(@PathVariable Long id, RedirectAttributes redirect) {
        var reservation = reservationService.findById(id);
        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            redirect.addFlashAttribute(Attributes.ERROR, "Only confirmed reservations can be refunded");
            return "redirect:/manager/reservations";
        }
        var payments = paymentService.getPaymentsByReservationId(id);
        if (payments.isEmpty()) {
            redirect.addFlashAttribute(Attributes.ERROR, "No payments found for this reservation");
            return "redirect:/manager/reservations";
        }

        for (var p : payments) {
            if (p.getRefundedAt() != null) continue;
            if (p.getPaymentMethod() != null && p.getPaymentMethod().name().equals("PAYPAL") && p.getOrderId() != null) {
                try {
                    boolean ok = paymentService.refundOnlineBookingPayment(p.getOrderId());
                    if (!ok) {
                        log.warn("PayPal refund returned false for order {}", p.getOrderId());
                        redirect.addFlashAttribute(Attributes.ERROR, "PayPal refund failed for order " + p.getOrderId());
                        return "redirect:/manager/reservations/" + id;
                    }
                } catch (Exception e) {
                    log.warn("PayPal refund failed for order {}: {}", p.getOrderId(), e.getMessage());
                    redirect.addFlashAttribute(Attributes.ERROR, "PayPal refund error: " + e.getMessage());
                    return "redirect:/manager/reservations/" + id;
                }
            }
        }

        paymentService.markRefunded(reservation, payments);
        redirect.addFlashAttribute(Attributes.SUCCESS, "Refund processed: $" + paymentService.calculateRefund(reservation));
        return "redirect:/manager/reservations/" + id;
    }

    @PostMapping("/{id}/refund/offline")
    String refundOffline(@PathVariable Long id, RedirectAttributes redirect) {
        var reservation = reservationService.findById(id);
        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            redirect.addFlashAttribute(Attributes.ERROR, "Only confirmed reservations can be refunded offline");
            return "redirect:/manager/reservations";
        }
        var payments = paymentService.getPaymentsByReservationId(id);
        if (payments.isEmpty()) {
            redirect.addFlashAttribute(Attributes.ERROR, "No payments found for this reservation");
            return "redirect:/manager/reservations";
        }
        paymentService.markRefunded(reservation, payments);
        redirect.addFlashAttribute(Attributes.SUCCESS, "Offline refund processed: $" + paymentService.calculateRefund(reservation));
        return "redirect:/manager/reservations/" + id;
    }
}
