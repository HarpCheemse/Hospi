package com.hospi.manage.features.reservation.controller;

import com.hospi.manage.common.constant.Attributes;
import com.hospi.manage.core.security.session.AccountPrincipal;
import com.hospi.manage.features.audit.service.AuditService;
import com.hospi.manage.features.reservation.dto.request.CheckInForm;
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
    private final AuditService auditService;

    @ModelAttribute
    void addCommonAttributes(org.springframework.ui.Model model) {
        model.addAttribute(Attributes.ACTIVE_SIDEBAR, "ACTIVE_BOOKINGS");
    }

    @GetMapping("/{id}/checkin")
    String checkInForm(@PathVariable Long id) {
        return "redirect:/receptionist/bookings/" + id;
    }

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
            auditService.log(principal.getId(), principal.getUsername(), "CHECKIN", "RESERVATION", id,
                    "Guest checked in");
            redirect.addFlashAttribute(Attributes.SUCCESS,
                    "Guest checked in successfully.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            redirect.addFlashAttribute(Attributes.ERROR,
                    e.getMessage());
        }

        if (redirect.getFlashAttributes().containsKey(Attributes.ERROR)) {
            return "redirect:/receptionist/bookings/" + id;
        }
        return "redirect:/receptionist/stays/" + id;
    }
}
