package com.hospi.manage.features.reservation.controller;

import com.hospi.manage.common.constant.Attributes;
import lombok.extern.slf4j.Slf4j;
import com.hospi.manage.features.payment.entity.Payment;
import com.hospi.manage.features.payment.repository.PaymentRepository;
import com.hospi.manage.features.payment.service.PaymentService;
import com.hospi.manage.features.reservation.dto.response.ActiveBookingsView;
import com.hospi.manage.features.reservation.entity.Reservation;
import com.hospi.manage.features.reservation.enums.ReservationStatus;
import com.hospi.manage.features.reservation.mapper.ReservationMapper;
import com.hospi.manage.features.reservation.service.ReservationService;
import com.hospi.manage.features.reservation.service.StayingGuestService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Controller for the manager reservation overview page (read-only + cancel/refund authority). */
@Controller
@RequestMapping("/manager/reservations")
@RequiredArgsConstructor
@Slf4j
public class ManagerReservationController {

    private final ReservationService reservationService;
    private final StayingGuestService stayingGuestService;
    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;

    private static final int PAGE_SIZE = 10;

    @ModelAttribute
    void addCommonAttributes(Model model) {
        model.addAttribute(Attributes.ACTIVE_SIDEBAR, "RESERVATIONS");
    }

    /** Show all reservations with independent time scope and status filters. */
    @GetMapping
    String list(
            @RequestParam(required = false) String scope,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(name = "page", defaultValue = "0") int page,
            Model model) {

        LocalDate today = LocalDate.now();
        List<Reservation> all;

        // Determine which statuses to include
        List<ReservationStatus> statuses = (status != null && !status.isBlank())
                ? List.of(ReservationStatus.valueOf(status))
                : Arrays.asList(ReservationStatus.values());

        // Use existing filtered query for "all" scope when no scope is active
        if ((scope == null || scope.equals("all")) && (status != null && !status.isBlank())) {
            // Status-only filter — use the efficient paginated query
            var paged = reservationService.findFiltered(statuses, search, null, PageRequest.of(page, PAGE_SIZE));
            var view = ReservationMapper.toActiveBookingsView(paged, status, null, search);
            var paymentStatuses = buildPaymentStatusMap(paged.getContent());
            model.addAttribute("paymentStatuses", paymentStatuses);
            addToModel(model, view, scope, status, search);
            return "manager/reservation/list";
        }

        // Load full result set for combined scope+status filtering
        if (scope == null || scope.equals("all")) {
            all = reservationService.findByStatuses(statuses);
        } else {
            all = reservationService.findByStatuses(statuses);
            all = filterByScope(all, scope, today);
        }

        if (search != null && !search.isBlank()) {
            all = filterBySearch(all, search);
        }

        // Make mutable before sorting
        all = new ArrayList<>(all);

        // Sort by check-in descending
        all.sort((a, b) -> {
            if (a.getCheckInAt() == null) return 1;
            if (b.getCheckInAt() == null) return -1;
            return b.getCheckInAt().compareTo(a.getCheckInAt());
        });

        var paged = paginate(all, page);
        var view = ReservationMapper.toActiveBookingsView(paged, status, null, search);

        // Derive payment statuses for visible page
        var paymentStatuses = buildPaymentStatusMap(paged.getContent());
        model.addAttribute("paymentStatuses", paymentStatuses);

        addToModel(model, view, scope, status, search);
        return "manager/reservation/list";
    }

    private void addToModel(Model model, ActiveBookingsView view, String scope, String status, String search) {
        LocalDate today = LocalDate.now();
        model.addAttribute(Attributes.VIEW, view);
        model.addAttribute("allStatuses", ReservationStatus.values());
        model.addAttribute("activeScope", scope != null ? scope : "all");
        model.addAttribute("activeStatus", status);
        model.addAttribute("pendingCount", reservationService.findByStatus(ReservationStatus.PENDING).size());
        model.addAttribute("arrivingToday",
                countByDate(reservationService.findByStatus(ReservationStatus.CONFIRMED), today, "arriving")
                + countByDate(reservationService.findByStatus(ReservationStatus.CHECKED_IN), today, "arriving"));
        model.addAttribute("departingToday",
                countByDate(reservationService.findByStatus(ReservationStatus.CHECKED_IN), today, "departing"));
        model.addAttribute("inHouseCount", reservationService.findByStatus(ReservationStatus.CHECKED_IN).size());
    }

    /** Show details for a single reservation. */
    @GetMapping("/{id}")
    String detail(@PathVariable Long id, Model model) {
        var reservation = reservationService.findById(id);
        model.addAttribute("reservation", reservation);
        model.addAttribute("guests", stayingGuestService.getGuests(id));
        model.addAttribute("payments", paymentRepository.findAllByReservationId(id));
        model.addAttribute("nights", reservation.getCheckInAt() != null && reservation.getCheckOutAt() != null
                ? ChronoUnit.DAYS.between(reservation.getCheckInAt(), reservation.getCheckOutAt())
                : 0);
        return "manager/reservation/detail";
    }

    /** Cancel a PENDING reservation. */
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

    /** Process a PayPal refund for a confirmed reservation. Fails without DB changes on error. */
    @Transactional(noRollbackFor = Exception.class)
    @PostMapping("/{id}/refund")
    String refund(@PathVariable Long id, RedirectAttributes redirect) {
        var reservation = reservationService.findById(id);
        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            redirect.addFlashAttribute(Attributes.ERROR, "Only confirmed reservations can be refunded");
            return "redirect:/manager/reservations";
        }
        var payments = paymentRepository.findAllByReservationId(id);
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

        markRefunded(reservation, payments);
        redirect.addFlashAttribute(Attributes.SUCCESS, "Refund processed: $" + paymentService.calculateRefund(reservation));
        return "redirect:/manager/reservations/" + id;
    }

    /** Process an offline refund (skips PayPal API). Always succeeds. */
    @Transactional
    @PostMapping("/{id}/refund/offline")
    String refundOffline(@PathVariable Long id, RedirectAttributes redirect) {
        var reservation = reservationService.findById(id);
        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            redirect.addFlashAttribute(Attributes.ERROR, "Only confirmed reservations can be refunded offline");
            return "redirect:/manager/reservations";
        }
        var payments = paymentRepository.findAllByReservationId(id);
        if (payments.isEmpty()) {
            redirect.addFlashAttribute(Attributes.ERROR, "No payments found for this reservation");
            return "redirect:/manager/reservations";
        }
        markRefunded(reservation, payments);
        redirect.addFlashAttribute(Attributes.SUCCESS, "Offline refund processed: $" + paymentService.calculateRefund(reservation));
        return "redirect:/manager/reservations/" + id;
    }

    private void markRefunded(Reservation reservation, List<Payment> payments) {
        var refundAmount = paymentService.calculateRefund(reservation);
        for (var p : payments) {
            if (p.getRefundedAt() == null) {
                p.setRefundedAmount(refundAmount);
                p.setRefundedAt(LocalDateTime.now());
                paymentRepository.save(p);
            }
        }
        reservation.setStatus(ReservationStatus.CANCELLED);
    }

    // -- Helpers --

    private Map<Long, String> buildPaymentStatusMap(List<Reservation> reservations) {
        if (reservations.isEmpty()) return Map.of();
        var ids = reservations.stream().map(Reservation::getId).toList();
        var payments = paymentRepository.findByReservationIdIn(ids);
        Map<Long, List<Payment>> grouped = payments.stream()
                .collect(Collectors.groupingBy(p -> p.getReservation().getId()));

        Map<Long, String> result = new HashMap<>();
        for (var r : reservations) {
            var pList = grouped.get(r.getId());
            if (pList == null || pList.isEmpty()) {
                result.put(r.getId(), "PENDING");
            } else {
                boolean anyRefunded = pList.stream().anyMatch(p -> p.getRefundedAt() != null);
                boolean anyDeposit = pList.stream().anyMatch(p ->
                        p.getAmount() != null && r.getTotalPrice() != null
                        && p.getAmount().compareTo(r.getTotalPrice()) < 0);
                if (anyRefunded) {
                    result.put(r.getId(), "REFUNDED");
                } else if (anyDeposit) {
                    result.put(r.getId(), "DEPOSIT");
                } else {
                    result.put(r.getId(), "PAID");
                }
            }
        }
        return result;
    }

    private static List<Reservation> filterByScope(List<Reservation> reservations, String scope, LocalDate today) {
        return reservations.stream()
                .filter(r -> {
                    if (r.getCheckInAt() == null || r.getCheckOutAt() == null) return false;
                    return switch (scope) {
                        case "today" -> !r.getCheckOutAt().isBefore(today) && !r.getCheckInAt().isAfter(today);
                        case "upcoming" -> r.getCheckInAt().isAfter(today);
                        case "history" -> r.getCheckOutAt().isBefore(today);
                        default -> true;
                    };
                })
                .toList();
    }

    private static List<Reservation> filterBySearch(List<Reservation> reservations, String search) {
        String q = search.trim().toLowerCase();
        return reservations.stream()
                .filter(r -> (r.getGuestName() != null && r.getGuestName().toLowerCase().contains(q))
                        || (r.getGuestEmail() != null && r.getGuestEmail().toLowerCase().contains(q))
                        || (r.getGuestPhone() != null && r.getGuestPhone().contains(q))
                        || String.valueOf(r.getId()).equals(q))
                .toList();
    }

    private static Page<Reservation> paginate(List<Reservation> list, int page) {
        int start = page * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, list.size());
        List<Reservation> sublist = start >= list.size() ? List.of() : list.subList(start, end);
        return new PageImpl<>(sublist, PageRequest.of(page, PAGE_SIZE), list.size());
    }

    private static long countByDate(List<Reservation> reservations, LocalDate date, String type) {
        return reservations.stream()
                .filter(r -> {
                    if (r.getCheckInAt() == null || r.getCheckOutAt() == null) return false;
                    return "arriving".equals(type) ? r.getCheckInAt().equals(date) : r.getCheckOutAt().equals(date);
                })
                .count();
    }
}
