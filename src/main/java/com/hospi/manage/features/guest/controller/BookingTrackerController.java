package com.hospi.manage.features.guest.controller;

import com.hospi.manage.common.exception.ResourceNotFoundException;
import com.hospi.manage.common.interfaces.EmailService;
import com.hospi.manage.features.auth.enums.OtpType;
import com.hospi.manage.features.reservation.dto.response.ReservationSummaryView;
import com.hospi.manage.features.auth.service.OtpService;
import com.hospi.manage.features.guest.dto.BookingTrackForm;
import com.hospi.manage.features.guest.dto.OtpForm;
import com.hospi.manage.features.guest.dto.ReviewForm;
import com.hospi.manage.features.guest.service.BookingTrackerService;
import com.hospi.manage.features.hotel.entity.Hotel;
import com.hospi.manage.features.hotel.service.HotelService;
import com.hospi.manage.features.reservation.entity.Reservation;
import com.hospi.manage.features.reservation.enums.ReservationStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.hospi.manage.common.constant.Attributes.*;

/**
 * Controller for the guest booking tracker flow (lookup by email/code, verify
 * via OTP, review submissions).
 */
@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/my-booking")
public class BookingTrackerController {

    private static final int MAX_TRACKED_CODES = 20;

    private final BookingTrackerService bookingTrackerService;
    private final OtpService otpService;
    private final EmailService emailService;
    private final HotelService hotelService;

    private Set<String> resetTrackedCodes(HttpSession session) {
        var codes = new LinkedHashSet<String>();
        session.setAttribute(TRACKED_BOOKING_CODES, codes);
        return codes;
    }

    private Set<String> getTrackedCodes(HttpSession session) {
        Object attr = session.getAttribute(TRACKED_BOOKING_CODES);
        if (!(attr instanceof Set<?> rawSet)) {
            return resetTrackedCodes(session);
        }
        for (Object item : rawSet) {
            if (!(item instanceof String)) {
                return resetTrackedCodes(session);
            }
        }
        @SuppressWarnings("unchecked")
        var result = (Set<String>) attr;
        return result;
    }

    /**
     * Show the my-booking page with tracked bookings or the lookup form if none
     * are tracked.
     */
    @GetMapping
    String myBooking(HttpSession session,
                     Model model) {
        getHotelOrDefault(model);
        Set<String> codes = getTrackedCodes(session);
        model.addAttribute("activePage", "my-booking");

        if (!codes.isEmpty()) {
            buildMyBookingModel(codes, model, null);
            model.addAttribute(REVIEW_FORM, new ReviewForm(null, null));
        } else {
            model.addAttribute(FORM, new BookingTrackForm(null, null));
        }

        return "guest/my-booking";
    }

    /**
     * Show the OTP verification page for booking tracking.
     */
    @GetMapping("/verify")
    String verifyPage(HttpSession session,
                      Model model) {
        getHotelOrDefault(model);
        String trackedEmail = (String) session.getAttribute(TRACKED_EMAIL);
        String pendingCode = (String) session.getAttribute(PENDING_CODE);

        if (trackedEmail == null || pendingCode == null) {
            return "redirect:/my-booking";
        }

        model.addAttribute(VERIFY_EMAIL, trackedEmail);
        model.addAttribute(OTP_FORM, new OtpForm(null));
        return "guest/my-booking-verify";
    }

    /**
     * Look up a booking by email and confirmation code. On success, send an OTP
     * email and redirect to verification.
     */
    @PostMapping("/lookup")
    String lookup(@Valid @ModelAttribute(FORM) BookingTrackForm form,
                  BindingResult binding,
                  HttpSession session,
                  Model model,
                  RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            getHotelOrDefault(model);
            return "guest/my-booking";
        }

        try {
            bookingTrackerService.lookupByEmailAndCode(form.email(), form.bookingCode());
        } catch (ResourceNotFoundException e) {
            redirect.addFlashAttribute(ERROR, "No booking found with that email and code.");
            return "redirect:/my-booking";
        }

        String rawOtp = otpService.createOtp(form.email(), OtpType.BOOKING_TRACK);

        if (rawOtp != null) {
            emailService.send(form.email(),
                    "Your Booking Tracking OTP",
                    "Your OTP code is: " + rawOtp + "\n\nThis code expires in 10 minutes.");
        }

        session.setAttribute(TRACKED_EMAIL, form.email());
        session.setAttribute(PENDING_CODE, form.bookingCode());

        redirect.addFlashAttribute(SUCCESS,
                "Email sent successfully to " + emailService.maskEmail(form.email()));
        return "redirect:/my-booking/verify";
    }

    /**
     * Verify the OTP for booking tracking. On success, add the booking code to
     * the session's tracked set and redirect to my-booking.
     */
    @PostMapping("/verify")
    String verify(@Valid @ModelAttribute(OTP_FORM) OtpForm form,
                  BindingResult binding,
                  HttpSession session,
                  HttpServletRequest request,
                  Model model,
                  RedirectAttributes redirect) {
        String email = (String) session.getAttribute(TRACKED_EMAIL);
        String pendingCode = (String) session.getAttribute(PENDING_CODE);

        if (email == null || pendingCode == null) {
            return "redirect:/my-booking";
        }

        if (binding.hasErrors()) {
            getHotelOrDefault(model);
            model.addAttribute(VERIFY_EMAIL, email);
            return "guest/my-booking-verify";
        }

        boolean verified = otpService.verifyOtp(email, form.otp(), OtpType.BOOKING_TRACK);

        if (!verified) {
            redirect.addFlashAttribute(ERROR, "Invalid or expired OTP code.");
            return "redirect:/my-booking/verify";
        }

        request.changeSessionId();

        Set<String> codes = getTrackedCodes(session);
        if (codes.size() >= MAX_TRACKED_CODES) {
            redirect.addFlashAttribute(ERROR, "Too many tracked bookings. Please clear your list first.");
            return "redirect:/my-booking";
        }
        codes.add(email + ":" + pendingCode);
        session.setAttribute(TRACKED_BOOKING_CODES, codes);

        session.removeAttribute(PENDING_CODE);
        session.removeAttribute(TRACKED_EMAIL);

        redirect.addFlashAttribute(SUCCESS, "Booking verified successfully!");
        return "redirect:/my-booking";
    }

    /**
     * Clear all tracked booking codes from the session and redirect to
     * my-booking.
     */
    @PostMapping("/clear")
    String clear(HttpSession session) {
        session.removeAttribute(TRACKED_BOOKING_CODES);
        session.removeAttribute(TRACKED_EMAIL);
        session.removeAttribute(PENDING_CODE);
        return "redirect:/my-booking";
    }

    /**
     * Submit a review for a tracked reservation. Re-renders the page with an error
     * if validation or business rules fail.
     */
    @PostMapping("/review")
    String submitReview(@Valid @ModelAttribute(REVIEW_FORM) ReviewForm form,
                        BindingResult binding,
                        HttpSession session,
                        Model model,
                        RedirectAttributes redirect) {
        Set<String> codes = getTrackedCodes(session);

        if (binding.hasErrors()) {
            getHotelOrDefault(model);
            if (!codes.isEmpty()) {
                buildMyBookingModel(codes, model, null);
            } else {
                model.addAttribute(FORM, new BookingTrackForm(null, null));
            }
            return "guest/my-booking";
        }
        if (codes.isEmpty()) {
            redirect.addFlashAttribute(ERROR, "No bookings found. Please look up your booking first.");
            return "redirect:/my-booking";
        }

        try {
            bookingTrackerService.submitReview(form.reservationId(), form.rating(), codes);
        } catch (IllegalStateException e) {
            getHotelOrDefault(model);
            buildMyBookingModel(codes, model, e.getMessage());
            model.addAttribute(REVIEW_FORM, form);
            return "guest/my-booking";
        }

        redirect.addFlashAttribute(SUCCESS, "Review submitted.");
        return "redirect:/my-booking";
    }

    private void buildMyBookingModel(Set<String> codes, Model model, String error) {
        List<Reservation> reservations = bookingTrackerService.resolveByCodes(codes);
        model.addAttribute(RESERVATIONS,
                reservations.stream().map(ReservationSummaryView::from).toList());
        model.addAttribute(REVIEW_MAP, bookingTrackerService.buildReviewMap(reservations));
        model.addAttribute(PILL_CLASSES, bookingTrackerService.buildPillClasses(reservations));
        addCheckedOutMap(model, reservations);
        if (error != null) {
            model.addAttribute(ERROR, error);
        }
    }

    private static void addCheckedOutMap(Model model, List<Reservation> reservations) {
        Map<Long, Boolean> checkedOutMap = reservations.stream()
                .collect(Collectors.toMap(Reservation::getId,
                        r -> r.getStatus() == ReservationStatus.CHECKED_OUT));
        model.addAttribute(CHECKED_OUT, checkedOutMap);
    }

    private void getHotelOrDefault(Model model) {
        try {
            Hotel hotel = hotelService.find();
            model.addAttribute(HOTEL, hotel);
        } catch (DataAccessException | IllegalArgumentException e) {
            log.warn("Failed to load hotel details", e);
            model.addAttribute(HOTEL, null);
        }
    }
}
