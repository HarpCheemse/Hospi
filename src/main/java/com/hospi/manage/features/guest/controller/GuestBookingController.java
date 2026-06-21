package com.hospi.manage.features.guest.controller;

import com.hospi.manage.common.interfaces.EmailService;
import com.hospi.manage.features.admin.config.service.SystemConfigService;
import com.hospi.manage.features.auth.enums.OtpType;
import com.hospi.manage.features.auth.service.OtpService;
import com.hospi.manage.features.guest.dto.BookingDraft;
import com.hospi.manage.features.guest.dto.GuestDetailForm;
import com.hospi.manage.features.guest.validation.BookingDateValidator;
import com.hospi.manage.features.guest.dto.OtpForm;
import com.hospi.manage.features.payment.service.PaymentService;
import com.hospi.manage.features.reservation.dto.request.DateSearchForm;
import com.hospi.manage.features.reservation.dto.response.RoomTypeAvailabilityView;
import com.hospi.manage.features.reservation.entity.Reservation;
import com.hospi.manage.features.reservation.service.ReservationService;
import com.hospi.manage.features.reservation.service.RoomAvailabilityService;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.IntStream;

import static com.hospi.manage.common.constant.Attributes.*;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/book")
public class GuestBookingController {

    private final ReservationService reservationService;
    private final RoomAvailabilityService roomAvailabilityService;
    private final SystemConfigService systemConfigService;
    private final OtpService otpService;
    private final EmailService emailService;
    private final PaymentService paymentService;
    private final BookingDateValidator bookingDateValidator;

    private BookingDraft getDraft(HttpSession session) {
        BookingDraft draft = (BookingDraft) session.getAttribute(BOOKING_DRAFT);
        if (draft == null) {
            draft = new BookingDraft();
            session.setAttribute(BOOKING_DRAFT,
                    draft);
        }
        return draft;
    }

    @GetMapping
    String showDateForm(Model model) {
        model.addAttribute(FORM,
                new DateSearchForm(null, null));
        return "guest/booking/book";
    }

    @PostMapping
    String submitDates(@Valid @ModelAttribute("form") DateSearchForm form,
                       BindingResult binding,
                       HttpSession session) {
        bookingDateValidator.validate(form, binding);

        if (binding.hasErrors()) {
            return "guest/booking/book";
        }

        BookingDraft draft = getDraft(session);
        draft.setCheckInAt(form.checkInAt());
        draft.setCheckOutAt(form.checkOutAt());

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

        var config = systemConfigService.getConfig();
        model.addAttribute(AVAILABILITY,
                availability);
        model.addAttribute(MAX_ROOMS,
                config.getMaximumRoomPerBook());
        model.addAttribute(DEPOSIT_PERCENTAGE,
                config.getDefaultDepositPercentage());
        model.addAttribute(DRAFT,
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
            redirect.addFlashAttribute(ERROR,
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
            redirect.addFlashAttribute(ERROR,
                    "Please select at least one room.");
            return "redirect:/book/rooms";
        }

        var config = systemConfigService.getConfig();
        int maxRooms = config.getMaximumRoomPerBook() != null ? config.getMaximumRoomPerBook() : Integer.MAX_VALUE;
        int totalRooms = selections.stream().mapToInt(BookingDraft.RoomSelection::count).sum();
        if (totalRooms > maxRooms) {
            redirect.addFlashAttribute(ERROR,
                    "Total rooms selected (" + totalRooms + ") exceeds the maximum of " + maxRooms + " per booking.");
            return "redirect:/book/rooms";
        }

        BigDecimal totalPrice = BigDecimal.ZERO;
        for (BookingDraft.RoomSelection s : selections) {
            totalPrice = totalPrice.add(s.basePrice().multiply(BigDecimal.valueOf(s.count())));
        }

        BigDecimal depositPercentage = config.getDefaultDepositPercentage();
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

        if (!model.containsAttribute(GUEST_DETAIL_FORM)) {
            model.addAttribute(GUEST_DETAIL_FORM, new GuestDetailForm(
                    draft.getGuestName(),
                    draft.getGuestEmail(),
                    draft.getGuestPhone(),
                    draft.getGuestDateOfBirth(),
                    draft.getGuestNationality()
            ));
        }
        if (!model.containsAttribute(OTP_FORM)) {
            model.addAttribute(OTP_FORM,
                    new OtpForm(null));
        }
        model.addAttribute(OTP_SENT,
                otpSent != null && otpSent);
        model.addAttribute(DRAFT,
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
            model.addAttribute(OTP_FORM,
                    new OtpForm(null));
            model.addAttribute(OTP_SENT,
                    false);
            model.addAttribute(DRAFT,
                    getDraft(session));
            return "guest/booking/verify";
        }

        BookingDraft draft = getDraft(session);
        draft.setGuestName(form.guestName());
        draft.setGuestEmail(form.guestEmail());
        draft.setGuestPhone(form.guestPhone());
        draft.setGuestDateOfBirth(form.guestDateOfBirth());
        draft.setGuestNationality(form.guestNationality());

        try {
            String otp = otpService.createOtp(form.guestEmail(),
                    OtpType.BOOKING_CONFIRM);
            emailService.send(form.guestEmail(),
                    "Your Booking OTP Code",
                    "Your OTP code is: " + otp
                            + "\n\nThis code expires in 10 minutes.\n\nThank you for choosing Hospi!");
        } catch (Exception e) {
            redirect.addFlashAttribute(ERROR,
                    "Failed to send OTP. Please try again.");
            return "redirect:/book/verify";
        }

        redirect.addFlashAttribute(SUCCESS,
                "OTP sent to " + form.guestEmail());
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
            GuestDetailForm gForm = new GuestDetailForm(
                    draft.getGuestName(),
                    draft.getGuestEmail(),
                    draft.getGuestPhone(),
                    draft.getGuestDateOfBirth(),
                    draft.getGuestNationality()
            );
            model.addAttribute(GUEST_DETAIL_FORM,
                    gForm);
            model.addAttribute(OTP_SENT,
                    true);
            model.addAttribute(DRAFT,
                    draft);
            return "guest/booking/verify";
        }

        boolean verified = otpService.verifyOtp(draft.getGuestEmail(),
                otpForm.otp(),
                OtpType.BOOKING_CONFIRM);

        if (!verified) {
            redirect.addFlashAttribute(ERROR,
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
            model.addAttribute(ERROR,
                    "Payment was cancelled. Please try again.");
        }

        model.addAttribute(DRAFT,
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
            String returnUrl = UriComponentsBuilder.fromPath("/book/pay/capture")
                    .build().toUriString();

            String cancelUrl = UriComponentsBuilder.fromPath("/book/pay")
                    .queryParam("cancelled", "true")
                    .build().toUriString();

            String approvalUrl =
                    paymentService.createOnlineBookingPayment(
                            draft.getDepositAmount(),
                            returnUrl,
                            cancelUrl);

            return "redirect:" + approvalUrl;

        } catch (Exception e) {
            redirect.addFlashAttribute(
                    ERROR,
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
                redirect.addFlashAttribute(ERROR,
                        "Payment was not completed. Please try again.");
                return "redirect:/book/pay";
            }
        } catch (Exception e) {
            redirect.addFlashAttribute(ERROR,
                    "Payment processing failed. Please contact support.");
            return "redirect:/book/pay";
        }

        Reservation reservation;
        try {
            reservation = reservationService.createOnlineBooking(draft);
        } catch (Exception e) {
            redirect.addFlashAttribute(ERROR,
                    "Failed to create booking. Please contact support.");
            return "redirect:/book/pay";
        }

        session.removeAttribute(BOOKING_DRAFT);

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
        } catch (Exception e) {
            log.warn("Failed to send confirmation email for booking {}", reservation.getConfirmationCode(), e);
        }

        return "redirect:" + UriComponentsBuilder.fromPath("/book/confirmation")
                .queryParam("code", reservation.getConfirmationCode())
                .build().toUriString();
    }


    @GetMapping("/confirmation")
    String showConfirmation(@RequestParam String code, Model model) {
        model.addAttribute(BOOKING_CODE,
                code);
        return "guest/booking/confirmation";
    }
}
