package com.hospi.manage.features.reservation.controller;

import com.hospi.manage.common.constant.Attributes;
import com.hospi.manage.core.security.session.AccountPrincipal;
import com.hospi.manage.features.reservation.dto.request.CheckInForm;
import com.hospi.manage.features.reservation.enums.ReservationStatus;
import com.hospi.manage.features.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

/**
 * Controller for guest check-in flow (receptionist-facing).
 */
@Controller
@RequestMapping("/receptionist/reservations")
@RequiredArgsConstructor
public class CheckInController {

    private final ReservationService reservationService;

    /**
     * Set the active sidebar highlight for this feature.
     */
    @ModelAttribute
    void addCommonAttributes(org.springframework.ui.Model model) {
        model.addAttribute(Attributes.ACTIVE_SIDEBAR, "ACTIVE_BOOKINGS");
    }

    /**
     * Redirect to the booking detail page where check-in is handled via modal.
     */
    @GetMapping("/{id}/checkin")
    String checkInForm(@PathVariable Long id) {
        return "redirect:/receptionist/bookings/" + id;
    }

    /**
     * Process the check-in. Validate the booking code, update the reservation
     * status to CHECKED_IN, and redirect to the reservation list.
     */
    @PostMapping("/{id}/checkin")
    String confirmCheckIn(@PathVariable Long id,
                          @ModelAttribute(Attributes.FORM) CheckInForm form,
                          @AuthenticationPrincipal AccountPrincipal principal,
                          RedirectAttributes redirect) {
        try {
            reservationService.checkIn(id,
                    LocalDate.now(),
                    form.bookingCode(),
                    principal.getUsername());
            redirect.addFlashAttribute(Attributes.SUCCESS,
                    "Guest checked in successfully.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            redirect.addFlashAttribute(Attributes.ERROR,
                    e.getMessage());
        }

        return "redirect:/receptionist/bookings";
    }
}
