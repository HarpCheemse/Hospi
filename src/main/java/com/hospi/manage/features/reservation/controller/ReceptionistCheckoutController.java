package com.hospi.manage.features.receptionist.controller;

import com.hospi.manage.common.constant.Attributes;
import com.hospi.manage.features.admin.config.service.SystemConfigService;
import com.hospi.manage.features.payment.entity.Payment;
import com.hospi.manage.features.payment.enums.PaymentMethod;
import com.hospi.manage.features.payment.repository.PaymentRepository;
import com.hospi.manage.features.receptionist.dto.CheckoutCalculation;
import com.hospi.manage.features.receptionist.dto.CheckoutForm;
import com.hospi.manage.features.receptionist.dto.CheckoutView;
import com.hospi.manage.features.receptionist.dto.ReceiptView;
import com.hospi.manage.features.receptionist.service.CheckoutService;
import com.hospi.manage.features.reservation.entity.Reservation;
import com.hospi.manage.features.reservation.enums.ReservationStatus;
import com.hospi.manage.features.reservation.service.ReservationService;
import com.hospi.manage.features.reservation.service.StayingGuestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/receptionist/reservations")
@RequiredArgsConstructor
public class ReceptionistCheckoutController {

    private final ReservationService reservationService;
    private final CheckoutService checkoutService;
    private final StayingGuestService stayingGuestService;
    private final SystemConfigService systemConfigService;
    private final PaymentRepository paymentRepository;

    @ModelAttribute
    void addCommonAttributes(Model model) {
        model.addAttribute(Attributes.ACTIVE_SIDEBAR, "RESERVATIONS");
    }

    @GetMapping("/{id}/checkout")
    String checkout(@PathVariable Long id, Model model) {
        Reservation reservation = reservationService.findById(id);
        if (reservation.getStatus() != ReservationStatus.CHECKED_IN
                && reservation.getStatus() != ReservationStatus.CONFIRMED) {
            return "redirect:/receptionist/reservations";
        }

        int adultGuests = stayingGuestService.getAdultGuestCount(id, reservation.getCheckInAt());
        CheckoutCalculation calc = checkoutService.calculate(reservation, null, adultGuests);

        model.addAttribute(Attributes.VIEW, new CheckoutView(reservation, calc, adultGuests));
        model.addAttribute("config", systemConfigService.getConfig());
        model.addAttribute(Attributes.FORM, new CheckoutForm(null, null, null));

        return "receptionist/reservation/checkout";
    }

    @PostMapping("/{id}/checkout")
    String completeCheckout(@PathVariable Long id,
                            @ModelAttribute CheckoutForm form,
                            Principal principal,
                            RedirectAttributes redirect) {
        try {
            Reservation reservation = reservationService.findById(id);
            int adultGuests = stayingGuestService.getAdultGuestCount(id, reservation.getCheckInAt());

            boolean applyLateFee = Boolean.TRUE.equals(form.applyLateFee());
            LocalDateTime actualTime = applyLateFee ? form.actualCheckoutTime() : null;
            CheckoutCalculation calc = checkoutService.calculate(reservation, actualTime, adultGuests);

            PaymentMethod method = PaymentMethod.valueOf(form.paymentMethod());

            checkoutService.complete(id, calc, method,
                    principal.getName(), form.actualCheckoutTime(),
                    applyLateFee);

            redirect.addFlashAttribute(Attributes.SUCCESS, "Checkout completed successfully.");
            return "redirect:/receptionist/reservations/" + id + "/receipt";

        } catch (IllegalStateException | IllegalArgumentException e) {
            redirect.addFlashAttribute(Attributes.ERROR, e.getMessage());
            return "redirect:/receptionist/reservations/" + id + "/checkout";
        }
    }

    @GetMapping("/{id}/receipt")
    String receipt(@PathVariable Long id, Model model) {
        Reservation reservation = reservationService.findById(id);
        if (reservation.getStatus() != ReservationStatus.CHECKED_OUT) {
            return "redirect:/receptionist/reservations";
        }

        List<Payment> payments = paymentRepository.findAllByReservationId(id);
        model.addAttribute(Attributes.VIEW, ReceiptView.from(reservation, payments));
        return "receptionist/reservation/receipt";
    }
}
