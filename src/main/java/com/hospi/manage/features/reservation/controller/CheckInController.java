package com.hospi.manage.features.reservation.controller;

import com.hospi.manage.common.constant.Attributes;
import com.hospi.manage.core.security.session.AccountPrincipal;
import com.hospi.manage.features.reservation.dto.request.CheckInForm;
import com.hospi.manage.features.reservation.dto.response.CheckInView;
import com.hospi.manage.features.reservation.enums.ReservationStatus;
import com.hospi.manage.features.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/receptionist/reservations")
@RequiredArgsConstructor
public class CheckInController {

    private final ReservationService reservationService;

    @ModelAttribute
    void addCommonAttributes(Model model) {
        model.addAttribute(Attributes.ACTIVE_SIDEBAR,
                "RESERVATIONS");
    }

    @GetMapping("/{id}/checkin")
    String checkInForm(@PathVariable Long id, Model model) {
        var reservation = reservationService.findById(id);

        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            return "redirect:/receptionist/reservations";
        }

        model.addAttribute(Attributes.VIEW,
                CheckInView.from(reservation));
        model.addAttribute(Attributes.FORM,
                new CheckInForm(null));
        return "receptionist/reservation/checkin";
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
            redirect.addFlashAttribute(Attributes.SUCCESS,
                    "Guest checked in successfully.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            redirect.addFlashAttribute(Attributes.ERROR,
                    e.getMessage());
        }

        return "redirect:/receptionist/reservations";
    }
}
