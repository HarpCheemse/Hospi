package com.hospi.manage.features.receptionist.controller;

import com.hospi.manage.common.constant.Attributes;
import com.hospi.manage.core.security.session.AccountPrincipal;
import com.hospi.manage.features.receptionist.dto.CheckInView;
import com.hospi.manage.features.reservation.entity.Reservation;
import com.hospi.manage.features.reservation.enums.ReservationStatus;
import com.hospi.manage.features.reservation.repository.ReservationRepository;
import com.hospi.manage.features.reservation.service.ReservationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/receptionist/reservations")
public class ReceptionistCheckInController {

    private final ReservationRepository reservationRepository;
    private final ReservationService reservationService;

    public ReceptionistCheckInController(ReservationRepository reservationRepository,
                                         ReservationService reservationService) {
        this.reservationRepository = reservationRepository;
        this.reservationService = reservationService;
    }

    @ModelAttribute
    void addCommonAttributes(Model model) {
        model.addAttribute(Attributes.ACTIVE_SIDEBAR,
                "RESERVATIONS");
    }

    @GetMapping("/{id}/checkin")
    String checkInForm(@PathVariable Long id, Model model) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reservation"));

        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            return "redirect:/receptionist/reservations";
        }

        model.addAttribute(Attributes.VIEW,
                CheckInView.from(reservation));
        return "receptionist/reservation/checkin";
    }

    @PostMapping("/{id}/checkin")
    String confirmCheckIn(@PathVariable Long id,
                          @RequestParam(required = false) String bookingCode,
                          @AuthenticationPrincipal AccountPrincipal principal,
                          RedirectAttributes redirect) {
        try {
            reservationService.checkIn(id,
                    bookingCode,
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
