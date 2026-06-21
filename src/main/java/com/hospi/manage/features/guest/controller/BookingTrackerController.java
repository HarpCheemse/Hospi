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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
@RequestMapping("/my-booking")
public class BookingTrackerController {

    private final BookingTrackerService bookingTrackerService;
    private final OtpService otpService;
    private final EmailService emailService;

    public BookingTrackerController(BookingTrackerService bookingTrackerService,
                                    OtpService otpService,
                                    EmailService emailService) {
        this.bookingTrackerService = bookingTrackerService;
        this.otpService = otpService;
        this.emailService = emailService;
    }

    @SuppressWarnings("unchecked")
    private Set<String> getTrackedCodes(HttpSession session) {
        Set<String> codes = (Set<String>) session.getAttribute("trackedBookingCodes");
        if (codes == null) {
            codes = new LinkedHashSet<>();
            session.setAttribute("trackedBookingCodes", codes);
        }
        return codes;
    }

    @GetMapping
    public String myBooking(HttpSession session,
                            Model model) {
        Set<String> codes = getTrackedCodes(session);

        if (!codes.isEmpty()) {
            List<Reservation> reservations = bookingTrackerService.resolveByCodes(codes);
            model.addAttribute("reservations", reservations);
            model.addAttribute("reviewMap", bookingTrackerService.buildReviewMap(reservations));
            model.addAttribute("reviewForm", new ReviewForm());
            model.addAttribute("pillClasses", bookingTrackerService.buildPillClasses(reservations));
        } else {
            model.addAttribute("form", new BookingTrackForm());
        }

        return "guest/my-booking";
    }

    @GetMapping("/verify")
    String verifyPage(@RequestParam(required = false) String email,
                      HttpSession session,
                      Model model) {
        String trackedEmail = (String) session.getAttribute("trackedEmail");
        String pendingCode = (String) session.getAttribute("pendingCode");

        if (trackedEmail == null || pendingCode == null) {
            return "redirect:/my-booking";
        }

        model.addAttribute("verifyEmail", trackedEmail);
        model.addAttribute("otpForm", new OtpForm());
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

        Optional<Reservation> reservation = bookingTrackerService
                .lookupByEmailAndCode(form.getEmail(), form.getBookingCode());

        if (reservation.isEmpty()) {
            redirect.addFlashAttribute("error", "No booking found with that email and code.");
            return "redirect:/my-booking";
        }

        String rawOtp = otpService.createOtp(form.getEmail(), OtpType.BOOKING_TRACK);
        emailService.send(form.getEmail(),
                "Your Booking Tracking OTP",
                "Your OTP code is: " + rawOtp + "\n\nThis code expires in 10 minutes.");

        session.setAttribute("trackedEmail", form.getEmail());
        session.setAttribute("pendingCode", form.getBookingCode());

        return "redirect:/my-booking/verify?email=" + form.getEmail();
    }

    @PostMapping("/verify")
    String verify(@Valid @ModelAttribute("otpForm") OtpForm form,
                  BindingResult binding,
                  HttpSession session,
                  Model model,
                  RedirectAttributes redirect) {
        String email = (String) session.getAttribute("trackedEmail");
        String pendingCode = (String) session.getAttribute("pendingCode");

        if (email == null || pendingCode == null) {
            return "redirect:/my-booking";
        }

        if (binding.hasErrors()) {
            model.addAttribute("verifyEmail", email);
            return "guest/my-booking-verify";
        }

        boolean verified = otpService.verifyOtp(email, form.getOtp(), OtpType.BOOKING_TRACK);

        if (!verified) {
            redirect.addFlashAttribute("error", "Invalid or expired OTP code.");
            return "redirect:/my-booking/verify?email=" + email;
        }

        Set<String> codes = getTrackedCodes(session);
        codes.add(pendingCode);

        session.removeAttribute("pendingCode");
        session.removeAttribute("trackedEmail");

        redirect.addFlashAttribute("success", "Booking verified successfully!");
        return "redirect:/my-booking";
    }

    @PostMapping("/clear")
    String clear(HttpSession session) {
        session.removeAttribute("trackedBookingCodes");
        session.removeAttribute("trackedEmail");
        session.removeAttribute("pendingCode");
        return "redirect:/my-booking";
    }

    @PostMapping("/review")
    String submitReview(@Valid @ModelAttribute ReviewForm form,
                        BindingResult binding,
                        RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            return "redirect:/my-booking";
        }

        try {
            bookingTrackerService.submitReview(form.getReservationId(), form.getRating());
            redirect.addFlashAttribute("success", "Thank you for your review!");
        } catch (IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/my-booking";
    }
}
