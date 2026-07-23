package com.hospi.manage.features.reservation.controller;

import com.hospi.manage.common.constant.Attributes;
import com.hospi.manage.features.payment.service.PaymentService;
import com.hospi.manage.features.reservation.dto.request.DateSearchForm;
import com.hospi.manage.features.reservation.dto.request.OfflineBookingForm;
import com.hospi.manage.features.reservation.dto.request.StayingGuestForm;
import com.hospi.manage.features.reservation.dto.response.CreateDetailsView;
import com.hospi.manage.features.reservation.dto.response.ReservationSummaryView;
import com.hospi.manage.features.reservation.dto.response.RoomTypeAvailabilityView;
import com.hospi.manage.features.reservation.enums.ReservationStatus;
import com.hospi.manage.features.reservation.mapper.ReservationMapper;
import com.hospi.manage.features.reservation.service.ReservationService;
import com.hospi.manage.features.reservation.service.RoomAssignmentService;
import com.hospi.manage.features.reservation.service.RoomAvailabilityService;
import com.hospi.manage.features.reservation.service.StayingGuestService;
import com.hospi.manage.features.reservation.validation.DateSearchValidator;
import com.hospi.manage.features.reservation.validation.OfflineBookingFormValidator;
import com.hospi.manage.features.room.dto.response.RoomSelection;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.hospi.manage.common.constant.Attributes.GUESTS;
import static com.hospi.manage.common.constant.Attributes.NIGHTS;
import static com.hospi.manage.common.constant.Attributes.PAYMENTS;
import static com.hospi.manage.common.constant.Attributes.RESERVATION;

/** Controller for the receptionist active bookings listing and creation flow. */
@Controller
@RequestMapping("/receptionist/bookings")
@RequiredArgsConstructor
@Slf4j
public class ReceptionistBookingController {

    private final ReservationService reservationService;
    private final OfflineBookingFormValidator offlineBookingFormValidator;
    private final DateSearchValidator dateSearchValidator;
    private final RoomAvailabilityService roomAvailabilityService;
    private final PaymentService paymentService;
    private final StayingGuestService stayingGuestService;
    private final RoomAssignmentService roomAssignmentService;

    private static final int PAGE_SIZE = 10;

    @ModelAttribute
    void addCommonAttributes(Model model) {
        model.addAttribute(Attributes.ACTIVE_SIDEBAR, Attributes.ACTIVE_BOOKINGS);
    }

    private static final String PENDING_COUNT = "pendingCount";
    private static final String CONFIRMED_COUNT = "confirmedCount";
    private static final String ARRIVING_TODAY = "arrivingToday";

    private static final String ACTIVE_SOURCE = "activeSource";
    private static final String ACTIVE_SCOPE = "activeScope";

    @GetMapping
    String activeBookings(@RequestParam(required = false) String status,
                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                          @RequestParam(required = false) String search,
                          @RequestParam(required = false) String source,
                          @RequestParam(name = "scope", required = false) String scope,
                          @RequestParam(name = "page", defaultValue = "0") int page, Model model) {
        List<ReservationStatus> statuses = (status != null && !status.isBlank())
                ? List.of(ReservationStatus.valueOf(status))
                : List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED);

        var paged = reservationService.findFiltered(statuses, search, date, PageRequest.of(page, PAGE_SIZE));

        var today = LocalDate.now();

        if (source != null && !source.isBlank()) {
            var filtered = paged.getContent().stream()
                    .filter(r -> r.getSource() != null && r.getSource().name().equals(source))
                    .toList();
            paged = new org.springframework.data.domain.PageImpl<>(
                    filtered, PageRequest.of(page, PAGE_SIZE), filtered.size());
        }

        if ("today".equals(scope)) {
            var filtered = paged.getContent().stream()
                    .filter(r -> r.getCheckInAt() != null && r.getCheckInAt().equals(today))
                    .toList();
            paged = new org.springframework.data.domain.PageImpl<>(
                    filtered, PageRequest.of(page, PAGE_SIZE), filtered.size());
        }

        model.addAttribute(Attributes.VIEW,
                ReservationMapper.toActiveBookingsView(paged, status, date, search));
        model.addAttribute(ACTIVE_SOURCE, source);
        model.addAttribute(ACTIVE_SCOPE, scope != null ? scope : "all");

        model.addAttribute(PENDING_COUNT, reservationService.findByStatus(ReservationStatus.PENDING).size());
        model.addAttribute(CONFIRMED_COUNT, reservationService.findByStatus(ReservationStatus.CONFIRMED).size());
        model.addAttribute(ARRIVING_TODAY,
                reservationService.findByStatus(ReservationStatus.CONFIRMED).stream()
                        .filter(r -> r.getCheckInAt() != null && r.getCheckInAt().equals(today))
                        .count());

        return "receptionist/reservation/active";
    }

    @GetMapping("/{id}")
    String detail(@PathVariable Long id, Model model) {
        var reservation = reservationService.findById(id);
        model.addAttribute(RESERVATION, ReservationSummaryView.from(reservation));
        model.addAttribute(GUESTS, stayingGuestService.getGuests(id));
        model.addAttribute(PAYMENTS, paymentService.getPaymentsByReservationId(id));
        model.addAttribute(NIGHTS, reservation.getCheckInAt() != null && reservation.getCheckOutAt() != null
                ? ChronoUnit.DAYS.between(reservation.getCheckInAt(), reservation.getCheckOutAt())
                : 0);
        model.addAttribute("guestForm", new StayingGuestForm(null, null, null));
        model.addAttribute("assignedRooms", roomAssignmentService.getAssignedRooms(id));
        model.addAttribute("availableRooms", roomAssignmentService.getAvailableRooms(id));
        return "receptionist/reservation/detail";
    }

    @GetMapping("/create")
    String create(Model model) {
        model.addAttribute(Attributes.FORM, new DateSearchForm(null, null));
        return "receptionist/reservation/create";
    }

    @PostMapping("/create")
    String searchDates(@Valid @ModelAttribute(Attributes.FORM) DateSearchForm form, BindingResult binding) {
        dateSearchValidator.validate(form, binding);
        if (binding.hasErrors()) {
            return "receptionist/reservation/create";
        }
        return "redirect:" + UriComponentsBuilder.fromPath("/receptionist/bookings/create/details")
                .queryParam("checkInAt", form.checkInAt())
                .queryParam("checkOutAt", form.checkOutAt()).build().toUriString();
    }

    @GetMapping("/create/details")
    String createDetails(@RequestParam(required = false) LocalDate checkInAt,
                         @RequestParam(required = false) LocalDate checkOutAt, Model model) {
        if (checkInAt == null || checkOutAt == null) {
            return "redirect:/receptionist/bookings/create";
        }

        List<RoomTypeAvailabilityView> availabilityView = getAvailabilityView(checkInAt, checkOutAt);
        long nights = ChronoUnit.DAYS.between(checkInAt, checkOutAt);

        List<RoomSelection> roomSelections = availabilityView.stream()
                .map(rt -> new RoomSelection(rt.roomTypeId(), 0)).toList();

        model.addAttribute(Attributes.VIEW, new CreateDetailsView(availabilityView, nights));
        model.addAttribute(Attributes.FORM, new OfflineBookingForm(
                checkInAt, checkOutAt, null, null, null, null, null, roomSelections));
        return "receptionist/reservation/details";
    }

    @PostMapping("/create/details")
    String createBooking(@Valid @ModelAttribute(Attributes.FORM) OfflineBookingForm form,
                         BindingResult bindingResult, RedirectAttributes redirect, Model model) {
        offlineBookingFormValidator.validate(form, bindingResult);

        if (bindingResult.hasErrors()) {
            List<RoomTypeAvailabilityView> availabilityView = getAvailabilityView(form.checkInAt(), form.checkOutAt());
            long nights = ChronoUnit.DAYS.between(form.checkInAt(), form.checkOutAt());
            model.addAttribute(Attributes.VIEW, new CreateDetailsView(availabilityView, nights));
            return "receptionist/reservation/details";
        }

        var reservation = reservationService.createReservation(form);
        redirect.addFlashAttribute(Attributes.SUCCESS, "Offline booking created successfully.");
        return "redirect:/receptionist/bookings/" + reservation.getId();
    }

    @PostMapping("/{id}/cancel")
    String cancel(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            reservationService.cancelReservation(id);
            redirect.addFlashAttribute(Attributes.SUCCESS, "Reservation cancelled.");
        } catch (IllegalStateException e) {
            log.warn("Cancel failed for reservation {}: {}", id, e.getMessage());
            redirect.addFlashAttribute(Attributes.ERROR, e.getMessage());
        }
        return "redirect:/receptionist/bookings/" + id;
    }

    @PostMapping("/{id}/refund")
    String refund(@PathVariable Long id, RedirectAttributes redirect) {
        var reservation = reservationService.findById(id);
        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            redirect.addFlashAttribute(Attributes.ERROR, "Only confirmed reservations can be refunded");
            return "redirect:/receptionist/bookings/" + id;
        }
        var payments = paymentService.getPaymentsByReservationId(id);
        if (payments.isEmpty()) {
            redirect.addFlashAttribute(Attributes.ERROR, "No payments found for this reservation");
            return "redirect:/receptionist/bookings/" + id;
        }

        for (var p : payments) {
            if (p.getRefundedAt() != null) continue;
            if (p.getPaymentMethod() != null && p.getPaymentMethod().name().equals("PAYPAL") && p.getOrderId() != null) {
                try {
                    boolean ok = paymentService.refundOnlineBookingPayment(p.getOrderId());
                    if (!ok) {
                        log.warn("PayPal refund returned false for order {}", p.getOrderId());
                        redirect.addFlashAttribute(Attributes.ERROR, "PayPal refund failed for order " + p.getOrderId());
                        return "redirect:/receptionist/bookings/" + id;
                    }
                } catch (Exception e) {
                    log.warn("PayPal refund failed for order {}: {}", p.getOrderId(), e.getMessage());
                    redirect.addFlashAttribute(Attributes.ERROR, "PayPal refund error: " + e.getMessage());
                    return "redirect:/receptionist/bookings/" + id;
                }
            }
        }

        paymentService.markRefunded(reservation, payments);
        redirect.addFlashAttribute(Attributes.SUCCESS, "Refund processed: $" + paymentService.calculateRefund(reservation));
        return "redirect:/receptionist/bookings/" + id;
    }

    @PostMapping("/{id}/refund/offline")
    String refundOffline(@PathVariable Long id, RedirectAttributes redirect) {
        var reservation = reservationService.findById(id);
        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            redirect.addFlashAttribute(Attributes.ERROR, "Only confirmed reservations can be refunded offline");
            return "redirect:/receptionist/bookings/" + id;
        }
        var payments = paymentService.getPaymentsByReservationId(id);
        if (payments.isEmpty()) {
            redirect.addFlashAttribute(Attributes.ERROR, "No payments found for this reservation");
            return "redirect:/receptionist/bookings/" + id;
        }
        paymentService.markRefunded(reservation, payments);
        redirect.addFlashAttribute(Attributes.SUCCESS, "Offline refund processed: $" + paymentService.calculateRefund(reservation));
        return "redirect:/receptionist/bookings/" + id;
    }

    private List<RoomTypeAvailabilityView> getAvailabilityView(LocalDate checkInAt, LocalDate checkOutAt) {
        return roomAvailabilityService.getAvailability(checkInAt, checkOutAt).stream()
                .map(RoomTypeAvailabilityView::new).toList();
    }
}
