package com.hospi.manage.features.guest.controller;

import com.hospi.manage.common.interfaces.EmailService;
import com.hospi.manage.features.auth.enums.OtpType;
import com.hospi.manage.features.auth.service.OtpService;
import com.hospi.manage.features.guest.dto.BookingTrackForm;
import com.hospi.manage.features.guest.dto.ReviewForm;
import com.hospi.manage.features.reservation.entity.Reservation;
import com.hospi.manage.features.reservation.entity.Review;
import com.hospi.manage.features.reservation.enums.ReservationStatus;
import com.hospi.manage.features.reservation.repository.ReservationRepository;
import com.hospi.manage.features.reservation.repository.ReviewRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;

import static com.hospi.manage.features.reservation.enums.ReservationStatus.*;

@Controller
@RequestMapping("/my-booking")
public class BookingTrackerController {

    private final ReservationRepository reservationRepository;
    private final OtpService otpService;
    private final EmailService emailService;
    private final ReviewRepository reviewRepository;

    public BookingTrackerController(ReservationRepository reservationRepository,
                                    OtpService otpService,
                                    EmailService emailService,
                                    ReviewRepository reviewRepository) {
        this.reservationRepository = reservationRepository;
        this.otpService = otpService;
        this.emailService = emailService;
        this.reviewRepository = reviewRepository;
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
    public String myBooking(@RequestParam(required = false) String verify,
                            HttpSession session,
                            Model model) {
        Set<String> codes = getTrackedCodes(session);

        if (!codes.isEmpty()) {
            List<Reservation> reservations = codes.stream()
                    .map(code -> reservationRepository.findByConfirmationCode(code).orElse(null))
                    .filter(Objects::nonNull)
                    .toList();
            model.addAttribute("reservations", reservations);

            Map<Long, Review> reviewMap = reservations.stream()
                    .map(r -> reviewRepository.findByReservationId(r.getId()).orElse(null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(r -> r.getReservation().getId(), r -> r));
            model.addAttribute("reviewMap", reviewMap);
            model.addAttribute("reviewForm", new ReviewForm());

            Map<Long, String> pillClasses = new HashMap<>();
            for (Reservation r : reservations) {
                pillClasses.put(r.getId(), pillClass(r.getStatus()));
            }
            model.addAttribute("pillClasses", pillClasses);
        } else {
            model.addAttribute("form", new BookingTrackForm());
            if (verify != null) {
                model.addAttribute("verifyEmail", verify);
            }
        }

        return "guest/my-booking";
    }

    @PostMapping("/lookup")
    String lookup(@Valid @ModelAttribute("form") BookingTrackForm form,
                  BindingResult binding,
                  HttpSession session,
                  RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            return "guest/my-booking";
        }

        Optional<Reservation> reservation = reservationRepository
                .findByGuestEmailAndConfirmationCode(form.getEmail(), form.getBookingCode());

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

        return "redirect:/my-booking?verify=" + form.getEmail();
    }

    @PostMapping("/verify")
    String verify(@RequestParam String otp,
                  HttpSession session,
                  RedirectAttributes redirect) {
        String email = (String) session.getAttribute("trackedEmail");
        String pendingCode = (String) session.getAttribute("pendingCode");

        if (email == null || pendingCode == null) {
            return "redirect:/my-booking";
        }

        boolean verified = otpService.verifyOtp(email, otp, OtpType.BOOKING_TRACK);

        if (!verified) {
            redirect.addFlashAttribute("error", "Invalid or expired OTP code.");
            return "redirect:/my-booking?verify=" + email;
        }

        Set<String> codes = getTrackedCodes(session);
        codes.add(pendingCode);

        session.removeAttribute("pendingCode");
        session.removeAttribute("verifyEmail");

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
                        HttpSession session,
                        RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            return "redirect:/my-booking";
        }

        if (reviewRepository.existsByReservationId(form.getReservationId())) {
            redirect.addFlashAttribute("error", "You have already reviewed this booking.");
            return "redirect:/my-booking";
        }

        Reservation reservation = reservationRepository.findById(form.getReservationId())
                .orElse(null);

        if (reservation == null || reservation.getStatus() != ReservationStatus.CHECKED_OUT) {
            redirect.addFlashAttribute("error", "Reviews are only available for completed stays.");
            return "redirect:/my-booking";
        }

        Review review = new Review();
        review.setReservation(reservation);
        review.setRating(form.getRating());
        reviewRepository.save(review);

        redirect.addFlashAttribute("success", "Thank you for your review!");
        return "redirect:/my-booking";
    }

    private static String pillClass(ReservationStatus status) {
        if (status == CHECKED_OUT || status == CHECKED_IN) return "pill-success";
        if (status == CONFIRMED) return "pill-warning";
        if (status == CANCELLED) return "pill-danger";
        return "pill-muted";
    }
}
