package com.hospi.manage.features.reservation.controller;

import com.hospi.manage.common.constant.Attributes;
import com.hospi.manage.core.security.session.AccountPrincipal;
import com.hospi.manage.features.payment.enums.PaymentMethod;
import com.hospi.manage.features.payment.service.PaymentService;
import com.hospi.manage.features.reservation.dto.PaymentConfirmationView;
import com.hospi.manage.features.reservation.enums.ReservationStatus;
import com.hospi.manage.features.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/receptionist/reservations")
@RequiredArgsConstructor
public class PaymentController {

    private final ReservationService reservationService;
    private final PaymentService paymentService;

    @ModelAttribute
    void addCommonAttributes(Model model) {
        model.addAttribute(Attributes.ACTIVE_SIDEBAR,
                "RESERVATIONS");
    }

    @GetMapping("/{id}/payment")
    String paymentForm(@PathVariable Long id, Model model) {
        var reservation = reservationService.findById(id);

        if (reservation.getStatus() != ReservationStatus.PENDING) {
            return "redirect:/receptionist/reservations";
        }

        model.addAttribute(Attributes.VIEW,
                PaymentConfirmationView.from(reservation));
        return "receptionist/reservation/confirm-payment";
    }

    @PostMapping("/{id}/payment")
    String confirmPayment(@PathVariable Long id,
                          @RequestParam PaymentMethod paymentMethod,
                          @AuthenticationPrincipal AccountPrincipal principal,
                          RedirectAttributes redirect) {
        try {
            paymentService.confirmPayment(id,
                    paymentMethod,
                    principal.getUsername());
            redirect.addFlashAttribute(Attributes.SUCCESS,
                    "Payment confirmed successfully. Booking is now confirmed.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            redirect.addFlashAttribute(Attributes.ERROR,
                    e.getMessage());
        }

        return "redirect:/receptionist/reservations";
    }
}
