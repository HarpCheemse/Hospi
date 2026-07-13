package com.hospi.manage.features.guest.controller;

import com.hospi.manage.common.exception.ResourceNotFoundException;
import com.hospi.manage.common.interfaces.EmailService;
import com.hospi.manage.common.interfaces.EmailTemplates;
import com.hospi.manage.features.auth.service.OtpService;
import com.hospi.manage.features.guest.dto.BookingDraft;
import com.hospi.manage.features.guest.dto.GuestDetailForm;
import com.hospi.manage.features.guest.dto.OtpForm;
import com.hospi.manage.features.guest.mapper.BookingSession;
import com.hospi.manage.features.guest.service.BookingFlowService;
import com.hospi.manage.features.guest.validation.BookingDateValidator;
import com.hospi.manage.features.payment.service.PaymentService;
import com.hospi.manage.features.reservation.dto.request.DateSearchForm;
import com.hospi.manage.features.reservation.entity.Reservation;
import com.hospi.manage.features.reservation.enums.ReservationStatus;
import com.hospi.manage.features.reservation.service.ReservationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static com.hospi.manage.common.constant.Attributes.*;

/**
 * 7-step guest booking wizard: dates → rooms → guest details → OTP → payment → confirmation.
 * Session-scoped {@link BookingDraft} carries state; PayPal callbacks use tempKey in URL.
 */
@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/book")
public class BookingFlowController {

    private final ReservationService reservationService;
    private final BookingFlowService bookingFlowService;
    private final OtpService otpService;
    private final BookingDateValidator bookingDateValidator;
    private final PaymentService paymentService;
    private final EmailService emailService;

    /** Step 1 — show date form. Resume check: skip to /book/pay if OTP verified + draft complete.
     *  Pending cleanup: cancel abandoned PENDING reservation. Otherwise: full reset. */
    @GetMapping
    String showDateForm(Model model, HttpSession session,
                        @RequestParam(name = "reset", required = false) String reset) {
        // Resume: skip to pay if already past OTP
        if (reset == null) {
            String otpToken = (String) session.getAttribute(OTP_TOKEN);
            BookingDraft draft = BookingSession.getDraft(session);
            if (otpToken != null && otpService.isValidToken(otpToken)
                    && draft.getGuest() != null
                    && draft.getDates() != null
                    && draft.getRooms() != null) {
                return "redirect:/book/pay";
            }
        }

        // Clean up abandoned PENDING reservation
        String tempKey = (String) session.getAttribute(PENDING_PAYMENT_KEY);
        if (tempKey != null) {
            try {
                Reservation r = reservationService.findByPaymentIdempotencyKey(tempKey);
                if (r.getStatus() == ReservationStatus.PENDING) {
                    reservationService.cancelPendingReservation(r.getId());
                }
            } catch (ResourceNotFoundException e) {
                // key doesn't match any reservation, nothing to cancel
            }
        }

        // Full reset
        session.removeAttribute(BOOKING_DRAFT);
        session.removeAttribute(OTP_TOKEN);
        session.removeAttribute(PENDING_PAYMENT_KEY);
        model.addAttribute(FORM,
                new DateSearchForm(null,
                        null));
        return "guest/booking/book";
    }

    /** Step 1 POST — validate dates, save to draft, redirect to /book/rooms. */
    @PostMapping
    String submitDates(@Valid @ModelAttribute("form") DateSearchForm form,
                       BindingResult binding,
                       HttpSession session) {
        bookingDateValidator.validate(form,
                binding);

        if (binding.hasErrors()) {
            return "guest/booking/book";
        }

        BookingDraft draft = BookingSession.getDraft(session);
        draft.setDates(new BookingDraft.BookingDates(form.checkInAt(),
                form.checkOutAt()));

        return "redirect:/book/rooms";
    }

    /** Step 2 GET — show room availability. Guard: dates must be set. */
    @GetMapping("/rooms")
    String showRooms(HttpSession session, Model model) {
        BookingDraft draft = BookingSession.getDraft(session);
        if (draft.getDates() == null) {
            return "redirect:/book";
        }

        var view = bookingFlowService.buildAvailabilityView(draft);
        model.addAttribute(AVAILABILITY,
                view.availability());
        model.addAttribute(MAX_ROOMS,
                view.maxRooms());
        model.addAttribute(DEPOSIT_PERCENTAGE,
                view.depositPercentage());
        model.addAttribute(NIGHTS,
                view.nights());
        model.addAttribute(DRAFT,
                draft);
        return "guest/booking/rooms";
    }

    /** Step 2 POST — validate room selections, save to draft. */
    @PostMapping("/rooms")
    String submitRooms(@RequestParam(required = false) List<Long> roomTypeIds,
                       @RequestParam(required = false) List<Integer> counts,
                       HttpSession session,
                       RedirectAttributes redirect) {
        BookingDraft draft = BookingSession.getDraft(session);
        if (draft.getDates() == null) {
            return "redirect:/book";
        }

        try {
            bookingFlowService.processRoomSelections(draft,
                    roomTypeIds,
                    counts);
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute(ERROR,
                    e.getMessage());
            return "redirect:/book/rooms";
        }

        return "redirect:/book/verify";
    }

    /** Step 3 GET — show guest detail form. Guard: rooms must be selected. */
    @GetMapping("/verify")
    String showVerify(HttpSession session, Model model) {
        BookingDraft draft = BookingSession.getDraft(session);
        if (draft.getRooms() == null || draft.getRooms().selections() == null || draft.getRooms().selections().isEmpty()) {
            return "redirect:/book/rooms";
        }

        if (!model.containsAttribute(GUEST_DETAIL_FORM)) {
            var guest = draft.getGuest();
            model.addAttribute(GUEST_DETAIL_FORM,
                    new GuestDetailForm(
                            guest != null ? guest.name() : null,
                            guest != null ? guest.email() : null,
                            guest != null ? guest.phone() : null,
                            guest != null ? guest.dateOfBirth() : null,
                            guest != null ? guest.nationality() : null,
                            draft.isAcceptedTos()
                    ));
        }
        model.addAttribute(DRAFT,
                draft);
        return "guest/booking/verify";
    }

    /** Step 3 POST — validate guest details, save, send OTP email. */
    @PostMapping("/verify/send-otp")
    String sendOtp(@Valid @ModelAttribute("guestDetailForm") GuestDetailForm form,
                   BindingResult binding,
                   HttpSession session,
                   RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            return "guest/booking/verify";
        }

        BookingDraft draft = BookingSession.getDraft(session);
        draft.setGuest(new BookingDraft.BookingGuest(
                form.guestName(),
                form.guestEmail(),
                form.guestPhone(),
                form.guestDateOfBirth(),
                form.guestNationality()));
        draft.setAcceptedTos(Boolean.TRUE.equals(form.acceptedTos()));

        String maskedEmail = bookingFlowService.initiateBookingOtp(draft);
        if (maskedEmail == null) {
            redirect.addFlashAttribute(ERROR,
                    "Please wait before requesting a new OTP.");
            return "redirect:/book/verify";
        }

        redirect.addFlashAttribute(SUCCESS,
                "Email sent successfully to " + maskedEmail);
        return "redirect:/book/verify-otp";
    }

    /** Step 4 GET — show OTP entry form. Guard: guest must be set. */
    @GetMapping("/verify-otp")
    String showVerifyOtp(HttpSession session, Model model) {
        BookingDraft draft = BookingSession.getDraft(session);
        if (draft.getGuest() == null) {
            return "redirect:/book/verify";
        }

        if (!model.containsAttribute(OTP_FORM)) {
            model.addAttribute(OTP_FORM,
                    new OtpForm(null));
        }
        model.addAttribute(EMAIL,
                draft.getGuest().email());
        return "guest/booking/verify-otp";
    }

    /** Step 4 POST — verify OTP, issue token on success, redirect to /book/pay. */
    @PostMapping("/verify-otp")
    String verifyOtp(@Valid @ModelAttribute("otpForm") OtpForm otpForm,
                     BindingResult binding,
                     HttpSession session,
                     Model model,
                     RedirectAttributes redirect) {
        BookingDraft draft = BookingSession.getDraft(session);
        if (draft.getGuest() == null) {
            return "redirect:/book/verify";
        }

        if (binding.hasErrors()) {
            model.addAttribute(OTP_FORM,
                    otpForm);
            model.addAttribute(EMAIL,
                    draft.getGuest().email());
            return "guest/booking/verify-otp";
        }

        String token = bookingFlowService.verifyBookingOtp(draft.getGuest().email(),
                otpForm.otp());

        if (token == null) {
            redirect.addFlashAttribute(ERROR,
                    "Invalid or expired OTP");
            return "redirect:/book/verify-otp";
        }

        session.setAttribute(OTP_TOKEN,
                token);

        return "redirect:/book/pay";
    }

    /** Step 5 GET — show payment summary. Guard: guest + valid OTP token. */
    @GetMapping("/pay")
    String showPay(HttpSession session, Model model) {
        BookingDraft draft = BookingSession.getDraft(session);
        if (draft.getGuest() == null) {
            return "redirect:/book/verify";
        }
        String otpToken = (String) session.getAttribute(OTP_TOKEN);
        if (otpToken == null || !otpService.isValidToken(otpToken)) {
            return "redirect:/book/verify-otp";
        }
        model.addAttribute(DRAFT,
                draft);
        model.addAttribute(TOKEN,
                otpToken);
        return "guest/booking/pay";
    }

    /** Step 5 POST — create PayPal order. Guards OTP, creates PENDING reservation, redirects to PayPal. */
    @PostMapping("/pay")
    String createPayPalOrder(HttpSession session,
                             HttpServletRequest request,
                             RedirectAttributes redirect) throws IOException {
        BookingDraft draft = BookingSession.getDraft(session);
        if (draft.getGuest() == null) {
            return "redirect:/book/verify";
        }

        String otpToken = (String) session.getAttribute(OTP_TOKEN);
        if (otpToken == null || !otpService.isValidToken(otpToken)) {
            redirect.addFlashAttribute(ERROR,
                    "Your session has expired. Please verify your email again.");
            return "redirect:/book/verify-otp";
        }

        String tempKey = "TEMP-" + UUID.randomUUID();
        Reservation reservation;
        try {
            reservation = bookingFlowService.createPendingReservation(draft, tempKey);
        } catch (IllegalStateException e) {
            redirect.addFlashAttribute(ERROR, e.getMessage());
            return "redirect:/book/rooms";
        }
        session.setAttribute(PENDING_PAYMENT_KEY, tempKey);

        String returnUrl = ServletUriComponentsBuilder.fromRequest(request)
                .replacePath("/book/pay/success")
                .queryParam("key", tempKey)
                .build().toUriString();
        String cancelUrl = ServletUriComponentsBuilder.fromRequest(request)
                .replacePath("/book/pay/cancel")
                .queryParam("key", tempKey)
                .build().toUriString();

        String approvalUrl = paymentService.createOnlineBookingPayment(
                draft.getRooms().depositAmount(),
                returnUrl,
                cancelUrl);

        return "redirect:" + approvalUrl;
    }

    /** Step 6a — Handle PayPal success callback. Capture payment, confirm reservation, and redirect to confirmation page. */
    @GetMapping("/pay/success")
    String payPalSuccess(@RequestParam("token") String orderId,
                         @RequestParam("key") String tempKey,
                         HttpSession session,
                         RedirectAttributes redirect) throws IOException {
        boolean captured = paymentService.captureOnlineBookingPayment(orderId);
        if (!captured) {
            redirect.addFlashAttribute(ERROR, "Payment could not be completed. Please try again.");
            return "redirect:/book/pay";
        }

        Reservation reservation;
        try {
            reservation = reservationService.findByPaymentIdempotencyKey(tempKey);
        } catch (ResourceNotFoundException e) {
            redirect.addFlashAttribute(ERROR, "Session expired. Please start your booking again.");
            return "redirect:/book";
        }

        // Reservation was cancelled by cleanup scheduler while user was on PayPal
        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            boolean refunded = paymentService.refundOnlineBookingPayment(orderId);
            session.removeAttribute(BOOKING_DRAFT);
            session.removeAttribute(OTP_TOKEN);
            session.removeAttribute(PENDING_PAYMENT_KEY);
            if (refunded) {
                redirect.addFlashAttribute(ERROR,
                        "Your reservation has expired. The payment has been refunded. Please start a new booking.");
            } else {
                redirect.addFlashAttribute(ERROR,
                        "Your reservation has expired and the refund could not be processed. Please contact support.");
            }
            return "redirect:/book";
        }

        if (reservation.getStatus() != ReservationStatus.PENDING) {
            redirect.addFlashAttribute(ERROR,
                    "This booking is no longer available. The rooms may have been released.");
            return "redirect:/book";
        }

        BookingDraft draft = BookingSession.getDraft(session);
        reservationService.confirmAndAddPayment(reservation.getId(),
                draft.getRooms().depositAmount(), ONLINE_BOOKING, orderId);

        String confirmationCode = reservation.getConfirmationCode();

        try {
            var template = EmailTemplates.bookingConfirmation(
                    reservation.getGuestName(),
                    confirmationCode,
                    reservation.getCheckInAt().toString(),
                    reservation.getCheckOutAt().toString());
            emailService.send(reservation.getGuestEmail(), template.subject(), template.content());
        } catch (Exception e) {
            log.warn("Failed to send confirmation email to {}: {}", reservation.getGuestEmail(), e.getMessage());
        }

        session.removeAttribute(BOOKING_DRAFT);
        session.removeAttribute(OTP_TOKEN);
        session.removeAttribute(PENDING_PAYMENT_KEY);

        return "redirect:/book/confirmation?code=" + confirmationCode;
    }

    /** Step 6b — Handle PayPal cancel callback. Cancel PENDING reservation if found. */
    @GetMapping("/pay/cancel")
    String payPalCancel(@RequestParam(name = "key", required = false) String tempKey) {
        if (tempKey != null) {
            try {
                Reservation r = reservationService.findByPaymentIdempotencyKey(tempKey);
                if (r.getStatus() == ReservationStatus.PENDING) {
                    reservationService.cancelPendingReservation(r.getId());
                }
            } catch (ResourceNotFoundException e) {
                // key doesn't match any reservation, nothing to cancel
            }
        }
        return "redirect:/book/pay";
    }

    /** Step 7 — show booking confirmation page. */
    @GetMapping("/confirmation")
    String showConfirmation(@RequestParam String code, Model model) {
        model.addAttribute(BOOKING_CODE,
                code);
        return "guest/booking/confirmation";
    }
}
