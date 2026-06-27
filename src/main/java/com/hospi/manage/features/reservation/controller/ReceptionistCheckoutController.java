package com.hospi.manage.features.receptionist.controller;

import com.hospi.manage.common.constant.Attributes;
import com.hospi.manage.features.config.service.SystemConfigService;
import com.hospi.manage.features.payment.entity.Payment;
import com.hospi.manage.features.payment.enums.PaymentMethod;
import com.hospi.manage.features.payment.repository.PaymentRepository;
import com.hospi.manage.features.receptionist.dto.CheckoutCalculation;
import com.hospi.manage.features.receptionist.dto.CheckoutForm;
import com.hospi.manage.features.reservation.entity.Reservation;
import com.hospi.manage.features.reservation.enums.ReservationStatus;
import com.hospi.manage.features.reservation.service.CheckoutService;
import com.hospi.manage.features.reservation.service.ReservationService;
import com.hospi.manage.features.reservation.service.StayingGuestService;
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
public class ReceptionistCheckoutController {

    private final ReservationService reservationService;
    private final CheckoutService checkoutService;
    private final StayingGuestService stayingGuestService;
    private final SystemConfigService systemConfigService;
    private final PaymentRepository paymentRepository;

    public ReceptionistCheckoutController(ReservationService reservationService,
                                          CheckoutService checkoutService,
                                          StayingGuestService stayingGuestService,
                                          SystemConfigService systemConfigService,
                                          PaymentRepository paymentRepository) {
        this.reservationService = reservationService;
        this.checkoutService = checkoutService;
        this.stayingGuestService = stayingGuestService;
        this.systemConfigService = systemConfigService;
        this.paymentRepository = paymentRepository;
    }

    @ModelAttribute
    void addCommonAttributes(Model model) {
        model.addAttribute(Attributes.ACTIVE_SIDEBAR,
                "RESERVATIONS");
    }

    @GetMapping("/{id}/checkout")
    String checkout(@PathVariable Long id, Model model) {
        Reservation reservation = reservationService.findById(id);
        if (reservation.getStatus() != ReservationStatus.CHECKED_IN
                && reservation.getStatus() != ReservationStatus.CONFIRMED) {
            return "redirect:/receptionist/reservations";
        }

        int registeredGuests = stayingGuestService.getGuests(id).size();
        CheckoutCalculation calc = checkoutService.calculate(reservation,
                null,
                registeredGuests);

        model.addAttribute("reservation",
                reservation);
        model.addAttribute("calc",
                calc);
        model.addAttribute("config",
                systemConfigService.getConfig());
        model.addAttribute("registeredGuests",
                registeredGuests);
        model.addAttribute("form",
                new CheckoutForm());

        return "receptionist/reservation/checkout";
    }

    @PostMapping("/{id}/checkout")
    String completeCheckout(@PathVariable Long id,
                            @ModelAttribute CheckoutForm form,
                            Principal principal,
                            RedirectAttributes redirect) {
        try {
            Reservation reservation = reservationService.findById(id);
            int registeredGuests = stayingGuestService.getGuests(id).size();

            LocalDateTime actualTime = form.isApplyLateFee() ? form.getActualCheckoutTime() : null;
            CheckoutCalculation calc = checkoutService.calculate(reservation,
                    actualTime,
                    registeredGuests);

            PaymentMethod method = PaymentMethod.valueOf(form.getPaymentMethod());

            checkoutService.complete(id,
                    calc,
                    form.getAmountReceived(),
                    method,
                    principal.getName(),
                    form.getActualCheckoutTime(),
                    form.isApplyLateFee(),
                    form.getExtraGuestFeeOverride());

            redirect.addFlashAttribute("success",
                    "Checkout completed successfully.");
            return "redirect:/receptionist/reservations/" + id + "/receipt";

        } catch (Exception e) {
            redirect.addFlashAttribute("error",
                    e.getMessage());
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
        BigDecimal depositPaid = payments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO,
                        BigDecimal::add);

        BigDecimal totalPaid = depositPaid;
        if (reservation.getLateCheckoutFeeApplied() != null) {
            totalPaid = totalPaid.add(reservation.getLateCheckoutFeeApplied());
        }
        if (reservation.getExtraGuestFeeApplied() != null) {
            totalPaid = totalPaid.add(reservation.getExtraGuestFeeApplied());
        }

        model.addAttribute("reservation",
                reservation);
        model.addAttribute("depositPaid",
                depositPaid);
        model.addAttribute("totalPaid",
                totalPaid);
        return "receptionist/reservation/receipt";
    }
}
