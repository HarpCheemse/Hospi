package com.hospi.manage.features.guest.controller;

import com.hospi.manage.common.interfaces.EmailService;
import com.hospi.manage.features.auth.enums.OtpType;
import com.hospi.manage.features.auth.service.OtpService;
import com.hospi.manage.features.guest.dto.BookingTrackForm;
import com.hospi.manage.features.guest.dto.OtpForm;
import com.hospi.manage.features.guest.dto.ReviewForm;
import com.hospi.manage.features.guest.service.BookingTrackerService;
import com.hospi.manage.features.reservation.entity.Reservation;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

import static com.hospi.manage.common.constant.Attributes.*;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/my-booking")
public class BookingTrackerController {

    private final BookingTrackerService bookingTrackerService;
    private final OtpService otpService;
    private final EmailService emailService;

    @SuppressWarnings("unchecked")
    private Set<String> getTrackedCodes(HttpSession session) {
        Set<String> codes = (Set<String>) session.getAttribute(TRACKED_BOOKING_CODES);
        if (codes == null) {
            codes = new LinkedHashSet<>();
            session.setAttribute(TRACKED_BOOKING_CODES, codes);
        }
        return codes;
    }

    @GetMapping
    public String myBooking(HttpSession session,
                            Model model) {
        Set<String> codes = getTrackedCodes(session);

        if (!codes.isEmpty()) {
                List<Reservation> reservations = bookingTrackerService.resolveByCodes(codes);
                model.addAttribute(RESERVATIONS, reservations);
                model.addAttribute(REVIEW_MAP, bookingTrackerService.buildReviewMap(reservations));
            model.addAttribute(REVIEW_FORM, new ReviewForm(null, null));
                model.addAttribute(PILL_CLASSES, bookingTrackerService.buildPillClasses(reservations));
            } else {
            model.addAttribute(FORM, new BookingTrackForm(null, null));
        }

        return "guest/my-booking";
    }

    @GetMapping("/verify")
    String verifyPage(@RequestParam(required = false) String email,
                      HttpSession session,
                      Model model) {
        String trackedEmail = (String) session.getAttribute(TRACKED_EMAIL);
        String pendingCode = (String) session.getAttribute(PENDING_CODE);

        if (trackedEmail == null || pendingCode == null) {
            return "redirect:/my-booking";
        }

        model.addAttribute(VERIFY_EMAIL, trackedEmail);
        model.addAttribute(OTP_FORM, new OtpForm(null));
        return "guest/my-booking-verify";
    }

    @PostMapping("/lookup")
    String lookup(@Valid @ModelAttribute("form") BookingTrackForm form,
                  BindingResult binding,
                  HttpSession session,
                  RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            return "guest/my-booking";
        }

        try {
            bookingTrackerService.lookupByEmailAndCode(form.email(), form.bookingCode());
        } catch (Exception e) {
            redirect.addFlashAttribute(ERROR, "No booking found with that email and code.");
            return "redirect:/my-booking";
        }

        String rawOtp = otpService.createOtp(form.email(), OtpType.BOOKING_TRACK);

        try {
            emailService.send(form.email(),
                    "Your Booking Tracking OTP",
                    "Your OTP code is: " + rawOtp + "\n\nThis code expires in 10 minutes.");
        } catch (Exception e) {
            log.warn("Failed to send booking tracking OTP to {}", form.email(), e);
        }

        session.setAttribute(TRACKED_EMAIL, form.email());
        session.setAttribute(PENDING_CODE, form.bookingCode());

        return "redirect:" + UriComponentsBuilder.fromPath("/my-booking/verify")
                .queryParam("email", form.email())
                .build().toUriString();
    }

    @PostMapping("/verify")
    String verify(@Valid @ModelAttribute("otpForm") OtpForm form,
                  BindingResult binding,
                  HttpSession session,
                  Model model,
                  RedirectAttributes redirect) {
        String email = (String) session.getAttribute(TRACKED_EMAIL);
        String pendingCode = (String) session.getAttribute(PENDING_CODE);

        if (email == null || pendingCode == null) {
            return "redirect:/my-booking";
        }

        if (binding.hasErrors()) {
            model.addAttribute(VERIFY_EMAIL, email);
            return "guest/my-booking-verify";
        }

        boolean verified = otpService.verifyOtp(email, form.otp(), OtpType.BOOKING_TRACK);

        if (!verified) {
            redirect.addFlashAttribute(ERROR, "Invalid or expired OTP code.");
            return "redirect:" + UriComponentsBuilder.fromPath("/my-booking/verify")
                    .queryParam("email", email)
                    .build().toUriString();
        }

        Set<String> codes = getTrackedCodes(session);
        codes.add(pendingCode);

        session.removeAttribute(PENDING_CODE);
        session.removeAttribute(TRACKED_EMAIL);

        redirect.addFlashAttribute(SUCCESS, "Booking verified successfully!");
        return "redirect:/my-booking";
    }

    @PostMapping("/clear")
    String clear(HttpSession session) {
        session.removeAttribute(TRACKED_BOOKING_CODES);
        session.removeAttribute(TRACKED_EMAIL);
        session.removeAttribute(PENDING_CODE);
        return "redirect:/my-booking";
    }

    @PostMapping("/review")
    String submitReview(@Valid @ModelAttribute ReviewForm form,
                        BindingResult binding,
                        HttpSession session,
                        Model model) {
        Set<String> codes = getTrackedCodes(session);

        if (binding.hasErrors() || codes.isEmpty()) {
            if (!codes.isEmpty()) {
                List<Reservation> reservations = bookingTrackerService.resolveByCodes(codes);
            model.addAttribute(RESERVATIONS, reservations);
            model.addAttribute(REVIEW_MAP, bookingTrackerService.buildReviewMap(reservations));
            model.addAttribute(REVIEW_FORM, new ReviewForm(null, null));
            model.addAttribute(PILL_CLASSES, bookingTrackerService.buildPillClasses(reservations));
            } else {
            model.addAttribute(FORM, new BookingTrackForm(null, null));
            }
            return "guest/my-booking";
        }

        try {
            bookingTrackerService.submitReview(form.reservationId(), form.rating());
        } catch (IllegalStateException e) {
            List<Reservation> reservations = bookingTrackerService.resolveByCodes(codes);
            model.addAttribute(RESERVATIONS, reservations);
            model.addAttribute(REVIEW_MAP, bookingTrackerService.buildReviewMap(reservations));
            model.addAttribute(REVIEW_FORM, new ReviewForm(null, null));
            model.addAttribute(PILL_CLASSES, bookingTrackerService.buildPillClasses(reservations));
            model.addAttribute(ERROR, e.getMessage());
            return "guest/my-booking";
        }

        return "redirect:/my-booking";
    }
}
