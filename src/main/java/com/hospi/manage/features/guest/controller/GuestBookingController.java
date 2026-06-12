package com.hospi.manage.features.guest.controller;

import com.hospi.manage.common.interfaces.EmailService;
import com.hospi.manage.features.admin.config.service.SystemConfigService;
import com.hospi.manage.features.auth.enums.OtpType;
import com.hospi.manage.features.auth.service.OtpService;
import com.hospi.manage.features.guest.dto.BookingDraft;
import com.hospi.manage.features.guest.dto.GuestDetailForm;
import com.hospi.manage.features.guest.dto.OtpForm;
import com.hospi.manage.features.payment.service.PaymentService;
import com.hospi.manage.features.reservation.dto.DateSearchForm;
import com.hospi.manage.features.reservation.dto.RoomTypeAvailabilityView;
import com.hospi.manage.features.reservation.entity.Reservation;
import com.hospi.manage.features.reservation.service.ReservationService;
import com.hospi.manage.features.reservation.service.RoomAvailabilityService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/book")
public class GuestBookingController {

    private final ReservationService reservationService;
    private final RoomAvailabilityService roomAvailabilityService;
    private final SystemConfigService systemConfigService;
    private final OtpService otpService;
    private final EmailService emailService;
    private final PaymentService paymentService;

    public GuestBookingController(
            ReservationService reservationService,
            RoomAvailabilityService roomAvailabilityService,
            SystemConfigService systemConfigService,
            OtpService otpService,
            EmailService emailService,
            PaymentService paymentService) {

        this.reservationService = reservationService;
        this.roomAvailabilityService = roomAvailabilityService;
        this.systemConfigService = systemConfigService;
        this.otpService = otpService;
        this.emailService = emailService;
        this.paymentService = paymentService;
    }

    private BookingDraft getDraft(HttpSession session) {
        BookingDraft draft = (BookingDraft) session.getAttribute("bookingDraft");
        if (draft == null) {
            draft = new BookingDraft();
            session.setAttribute("bookingDraft",
                    draft);
        }
        return draft;
    }

    @GetMapping
    String showDateForm(Model model) {
        model.addAttribute("form",
                new DateSearchForm());
        return "guest/booking/book";
    }

    @PostMapping
    String submitDates(@Valid @ModelAttribute("form") DateSearchForm form,
                       BindingResult binding,
                       HttpSession session) {
        if (form.getCheckOutAt() != null && form.getCheckInAt() != null
                && !form.getCheckOutAt().isAfter(form.getCheckInAt())) {
            binding.rejectValue("checkOutAt",
                    "error",
                    "Check-out must be after check-in");
        }

        if (binding.hasErrors()) {
            return "guest/booking/book";
        }

        BookingDraft draft = getDraft(session);
        draft.setCheckInAt(form.getCheckInAt());
        draft.setCheckOutAt(form.getCheckOutAt());

        return "redirect:/book/rooms";
    }

    @GetMapping("/rooms")
    String showRooms(HttpSession session, Model model) {
        BookingDraft draft = getDraft(session);
        if (draft.getCheckInAt() == null || draft.getCheckOutAt() == null) {
            return "redirect:/book";
        }

        List<RoomTypeAvailabilityView> availability = roomAvailabilityService.getAvailability(
                        draft.getCheckInAt(),
                        draft.getCheckOutAt())
                .stream()
                .map(RoomTypeAvailabilityView::new)
                .toList();

        model.addAttribute("availability",
                availability);
        model.addAttribute("draft",
                draft);
        return "guest/booking/rooms";
    }

    @PostMapping("/rooms")
    String submitRooms(@RequestParam(required = false) List<Long> roomTypeIds,
                       @RequestParam(required = false) List<Integer> counts,
                       HttpSession session,
                       RedirectAttributes redirect) {
        BookingDraft draft = getDraft(session);
        if (draft.getCheckInAt() == null || draft.getCheckOutAt() == null) {
            return "redirect:/book";
        }

        List<RoomTypeAvailabilityView> availability = roomAvailabilityService.getAvailability(
                        draft.getCheckInAt(),
                        draft.getCheckOutAt())
                .stream()
                .map(RoomTypeAvailabilityView::new)
                .toList();

        if (roomTypeIds == null || counts == null) {
            redirect.addFlashAttribute("error",
                    "Please select at least one room.");
            return "redirect:/book/rooms";
        }

        List<BookingDraft.RoomSelection> selections = IntStream.range(0,
                        roomTypeIds.size())
                .filter(i -> counts.get(i) != null && counts.get(i) > 0)
                .mapToObj(i -> {
                    RoomTypeAvailabilityView rt = availability.stream()
                            .filter(a -> a.roomTypeId().equals(roomTypeIds.get(i)))
                            .findFirst().orElse(null);
                    if (rt == null) return null;
                    return new BookingDraft.RoomSelection(
                            rt.roomTypeId(),
                            rt.name(),
                            counts.get(i),
                            rt.basePrice()
                    );
                })
                .filter(s -> s != null)
                .toList();

        if (selections.isEmpty()) {
            redirect.addFlashAttribute("error",
                    "Please select at least one room.");
            return "redirect:/book/rooms";
        }

        BigDecimal totalPrice = BigDecimal.ZERO;
        for (BookingDraft.RoomSelection s : selections) {
            totalPrice = totalPrice.add(s.basePrice().multiply(BigDecimal.valueOf(s.count())));
        }

        BigDecimal depositPercentage = systemConfigService.getConfig().getDefaultDepositPercentage();
        BigDecimal depositAmount = totalPrice.multiply(depositPercentage)
                .divide(BigDecimal.valueOf(100),
                        RoundingMode.HALF_UP);

        draft.setRoomSelections(selections);
        draft.setTotalPrice(totalPrice);
        draft.setDepositAmount(depositAmount);

        return "redirect:/book/verify";
    }

    @GetMapping("/verify")
    String showVerify(HttpSession session, Model model,
                      @RequestParam(required = false) Boolean otpSent) {
        BookingDraft draft = getDraft(session);
        if (draft.getRoomSelections() == null || draft.getRoomSelections().isEmpty()) {
            return "redirect:/book/rooms";
        }

        if (!model.containsAttribute("guestDetailForm")) {
            GuestDetailForm form = new GuestDetailForm();
            if (draft.getGuestName() != null) form.setGuestName(draft.getGuestName());
            if (draft.getGuestEmail() != null) form.setGuestEmail(draft.getGuestEmail());
            if (draft.getGuestPhone() != null) form.setGuestPhone(draft.getGuestPhone());
            if (draft.getGuestDateOfBirth() != null) form.setGuestDateOfBirth(draft.getGuestDateOfBirth());
            if (draft.getGuestNationality() != null) form.setGuestNationality(draft.getGuestNationality());
            model.addAttribute("guestDetailForm",
                    form);
        }
        if (!model.containsAttribute("otpForm")) {
            model.addAttribute("otpForm",
                    new OtpForm());
        }
        model.addAttribute("otpSent",
                otpSent != null && otpSent);
        model.addAttribute("draft",
                draft);
        return "guest/booking/verify";
    }

    @PostMapping("/verify/send-otp")
    String sendOtp(@Valid @ModelAttribute("guestDetailForm") GuestDetailForm form,
                   BindingResult binding,
                   HttpSession session,
                   Model model,
                   RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            model.addAttribute("otpForm",
                    new OtpForm());
            model.addAttribute("otpSent",
                    false);
            model.addAttribute("draft",
                    getDraft(session));
            return "guest/booking/verify";
        }

        BookingDraft draft = getDraft(session);
        draft.setGuestName(form.getGuestName());
        draft.setGuestEmail(form.getGuestEmail());
        draft.setGuestPhone(form.getGuestPhone());
        draft.setGuestDateOfBirth(form.getGuestDateOfBirth());
        draft.setGuestNationality(form.getGuestNationality());

        try {
            String otp = otpService.createOtp(form.getGuestEmail(),
                    OtpType.BOOKING_CONFIRM);
            emailService.send(form.getGuestEmail(),
                    "Your Booking OTP Code",
                    "Your OTP code is: " + otp
                            + "\n\nThis code expires in 10 minutes.\n\nThank you for choosing Hospi!");
        } catch (Exception e) {
            redirect.addFlashAttribute("error",
                    "Failed to send OTP. Please try again.");
            return "redirect:/book/verify";
        }

        redirect.addFlashAttribute("success",
                "OTP sent to " + form.getGuestEmail());
        return "redirect:/book/verify?otpSent=true";
    }

    @PostMapping("/verify")
    String verifyOtp(@Valid @ModelAttribute("otpForm") OtpForm otpForm,
                     BindingResult binding,
                     HttpSession session,
                     Model model,
                     RedirectAttributes redirect) {
        BookingDraft draft = getDraft(session);
        if (draft.getGuestEmail() == null) {
            return "redirect:/book/verify";
        }

        if (binding.hasErrors()) {
            GuestDetailForm gForm = new GuestDetailForm();
            gForm.setGuestName(draft.getGuestName());
            gForm.setGuestEmail(draft.getGuestEmail());
            gForm.setGuestPhone(draft.getGuestPhone());
            gForm.setGuestDateOfBirth(draft.getGuestDateOfBirth());
            gForm.setGuestNationality(draft.getGuestNationality());
            model.addAttribute("guestDetailForm",
                    gForm);
            model.addAttribute("otpSent",
                    true);
            model.addAttribute("draft",
                    draft);
            return "guest/booking/verify";
        }

        boolean verified = otpService.verifyOtp(draft.getGuestEmail(),
                otpForm.getOtp(),
                OtpType.BOOKING_CONFIRM);

        if (!verified) {
            redirect.addFlashAttribute("error",
                    "Invalid or expired OTP");
            return "redirect:/book/verify?otpSent=true";
        }

        draft.setEmailVerified(true);
        return "redirect:/book/pay";
    }

    @GetMapping("/pay")
    String showPay(HttpSession session, Model model,
                   @RequestParam(required = false) Boolean cancelled) {
        BookingDraft draft = getDraft(session);
        if (!draft.isEmailVerified()) {
            return "redirect:/book/verify";
        }

        if (cancelled != null && cancelled) {
            model.addAttribute("error",
                    "Payment was cancelled. Please try again.");
        }

        model.addAttribute("draft",
                draft);
        return "guest/booking/pay";
    }

    @PostMapping("/pay")
    String processPay(HttpSession session,
                      RedirectAttributes redirect) {

        BookingDraft draft = getDraft(session);

        if (!draft.isEmailVerified()) {
            return "redirect:/book/verify";
        }

        try {
            String returnUrl =
                    "http://localhost:8080/book/pay/capture";

            String cancelUrl =
                    "http://localhost:8080/book/pay?cancelled=true";

            String approvalUrl =
                    paymentService.createOnlineBookingPayment(
                            draft.getDepositAmount(),
                            returnUrl,
                            cancelUrl);

            return "redirect:" + approvalUrl;

        } catch (Exception e) {
            redirect.addFlashAttribute(
                    "error",
                    "Failed to initiate payment. Please try again."
            );

            return "redirect:/book/pay";
        }
    }

    @GetMapping("/pay/capture")
    String capturePay(@RequestParam("token") String token,
                      HttpSession session,
                      RedirectAttributes redirect) {
        BookingDraft draft = getDraft(session);
        if (!draft.isEmailVerified()) {
            return "redirect:/book/verify";
        }

        try {
            boolean completed =
                    paymentService.captureOnlineBookingPayment(token);
            if (!completed) {
                redirect.addFlashAttribute("error",
                        "Payment was not completed. Please try again.");
                return "redirect:/book/pay";
            }
        } catch (Exception e) {
            redirect.addFlashAttribute("error",
                    "Payment processing failed. Please contact support.");
            return "redirect:/book/pay";
        }

        Reservation reservation;
        try {
            reservation = reservationService.createOnlineBooking(draft);
        } catch (Exception e) {
            redirect.addFlashAttribute("error",
                    "Failed to create booking. Please contact support.");
            return "redirect:/book/pay";
        }

        session.removeAttribute("bookingDraft");

        try {
            emailService.send(reservation.getGuestEmail(),
                    "Booking Confirmed - Your Hospi Booking Code",
                    "Dear " + reservation.getGuestName() + ",\n\n"
                            + "Your booking is confirmed!\n\n"
                            + "Booking Code: " + reservation.getConfirmationCode() + "\n"
                            + "Check-In: " + reservation.getCheckInAt() + "\n"
                            + "Check-Out: " + reservation.getCheckOutAt() + "\n"
                            + "Total: $" + reservation.getTotalPrice() + "\n\n"
                            + "Please present your booking code at check-in.\n\n"
                            + "Thank you for choosing Hospi!");
        } catch (Exception ignored) {
        }

        return "redirect:/book/confirmation?code=" + reservation.getConfirmationCode();
    }


    @GetMapping("/confirmation")
    String showConfirmation(@RequestParam String code, Model model) {
        model.addAttribute("bookingCode",
                code);
        return "guest/booking/confirmation";
    }
}
