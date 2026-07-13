package com.hospi.manage.features.reservation.controller;

import com.hospi.manage.common.constant.Attributes;
import com.hospi.manage.core.security.session.AccountPrincipal;
import com.hospi.manage.features.audit.service.AuditService;
import com.hospi.manage.features.payment.enums.PaymentMethod;
import com.hospi.manage.features.payment.service.PaymentService;
import com.hospi.manage.features.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for confirming payments on PENDING reservations (receptionist-facing).
 */
@Controller
@RequestMapping("/receptionist/reservations")
@RequiredArgsConstructor
public class PaymentController {

    private final ReservationService reservationService;
    private final PaymentService paymentService;
    private final AuditService auditService;

    @ModelAttribute
    void addCommonAttributes(org.springframework.ui.Model model) {
        model.addAttribute(Attributes.ACTIVE_SIDEBAR, "ACTIVE_BOOKINGS");
    }

    @GetMapping("/{id}/payment")
    String paymentForm(@PathVariable Long id) {
        return "redirect:/receptionist/bookings/" + id;
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
            auditService.log(principal.getId(), principal.getUsername(), "PAYMENT", "RESERVATION", id,
                    "Payment method: " + paymentMethod);
            redirect.addFlashAttribute(Attributes.SUCCESS,
                    "Payment confirmed successfully. Booking is now confirmed.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            redirect.addFlashAttribute(Attributes.ERROR,
                    e.getMessage());
        }

        return "redirect:/receptionist/bookings";
    }
}
