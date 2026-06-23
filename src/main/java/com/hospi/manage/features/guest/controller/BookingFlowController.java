package com.hospi.manage.features.guest.controller;

import com.hospi.manage.common.interfaces.EmailService;
import com.hospi.manage.features.admin.config.service.SystemConfigService;
import com.hospi.manage.features.auth.enums.OtpType;
import com.hospi.manage.features.auth.service.OtpService;
import com.hospi.manage.features.guest.dto.BookingDraft;
import com.hospi.manage.features.guest.dto.GuestDetailForm;
import com.hospi.manage.features.guest.mapper.BookingSession;
import com.hospi.manage.features.guest.validation.BookingDateValidator;
import com.hospi.manage.features.guest.dto.OtpForm;
import com.hospi.manage.features.payment.service.PaymentService;
import com.hospi.manage.features.reservation.dto.request.DateSearchForm;
import com.hospi.manage.features.reservation.dto.response.RoomTypeAvailabilityView;
import com.hospi.manage.features.reservation.entity.Reservation;
import com.hospi.manage.features.reservation.service.ReservationService;
import com.hospi.manage.features.reservation.service.RoomAvailabilityService;
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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.hospi.manage.common.constant.Attributes.*;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/book")
public class BookingFlowController {

    private final ReservationService reservationService;
    private final RoomAvailabilityService roomAvailabilityService;
    private final SystemConfigService systemConfigService;
    private final OtpService otpService;
    private final EmailService emailService;
    private final BookingDateValidator bookingDateValidator;
    private final PaymentService paymentService;

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

        BookingDraft draft = BookingSession.getDraft(session);
        draft.setCheckInAt(form.checkInAt());
        draft.setCheckOutAt(form.checkOutAt());

        return "redirect:/book/rooms";
    }

    @GetMapping("/rooms")
    String showRooms(HttpSession session, Model model) {
        BookingDraft draft = BookingSession.getDraft(session);
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
        BookingDraft draft = BookingSession.getDraft(session);
        if (draft.getCheckInAt() == null || draft.getCheckOutAt() == null) {
            return "redirect:/book";
        }

        List<RoomTypeAvailabilityView> availabilityList = roomAvailabilityService.getAvailability(
                        draft.getCheckInAt(),
                        draft.getCheckOutAt())
                .stream()
                .map(RoomTypeAvailabilityView::new)
                .toList();

        Map<Long, RoomTypeAvailabilityView> availabilityMap = availabilityList.stream()
                .collect(Collectors.toMap(RoomTypeAvailabilityView::roomTypeId, a -> a));

        if (roomTypeIds == null || counts == null) {
            redirect.addFlashAttribute(ERROR,
                    "Please select at least one room.");
            return "redirect:/book/rooms";
        }

        if (roomTypeIds.size() != counts.size()) {
            redirect.addFlashAttribute(ERROR,
                    "Invalid room selection data");
            return "redirect:/book/rooms";
        }

        List<BookingDraft.RoomSelection> selections = IntStream.range(0,
                        roomTypeIds.size())
                .filter(i -> counts.get(i) != null && counts.get(i) > 0)
                .mapToObj(i -> {
                    RoomTypeAvailabilityView rt = availabilityMap.get(roomTypeIds.get(i));
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

        for (BookingDraft.RoomSelection s : selections) {
            RoomTypeAvailabilityView rt = availabilityMap.get(s.roomTypeId());
            if (rt != null && s.count() > rt.availableRooms()) {
                redirect.addFlashAttribute(ERROR,
                        "Room type '" + s.roomTypeName() + "' only has " + rt.availableRooms() + " rooms available.");
                return "redirect:/book/rooms";
            }
        }

        var config = systemConfigService.getConfig();
        int maxRooms = config.getMaximumRoomPerBook() != null ? config.getMaximumRoomPerBook() : Integer.MAX_VALUE;
        int totalRooms = selections.stream().mapToInt(BookingDraft.RoomSelection::count).sum();
        if (totalRooms > maxRooms) {
            redirect.addFlashAttribute(ERROR,
                    "Total rooms selected (" + totalRooms + ") exceeds the maximum of " + maxRooms + " per booking.");
            return "redirect:/book/rooms";
        }

        long nights = draft.getCheckOutAt().toEpochDay() - draft.getCheckInAt().toEpochDay();
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (BookingDraft.RoomSelection s : selections) {
            totalPrice = totalPrice.add(s.basePrice().multiply(BigDecimal.valueOf(s.count()).multiply(BigDecimal.valueOf(nights))));
        }

        BigDecimal depositPercentage = config.getDefaultDepositPercentage();
        BigDecimal depositAmount = totalPrice.multiply(depositPercentage)
                .divide(BigDecimal.valueOf(100),
                        RoundingMode.HALF_UP);

        draft.setRoomSelections(selections);
        draft.setTotalPrice(totalPrice);
        draft.setDepositAmount(depositAmount);
        draft.setNumberOfNights(nights);

        return "redirect:/book/verify";
    }

    @GetMapping("/verify")
    String showVerify(HttpSession session, Model model) {
        BookingDraft draft = BookingSession.getDraft(session);
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
        model.addAttribute(DRAFT,
                draft);
        return "guest/booking/verify";
    }

    @PostMapping("/verify/send-otp")
    String sendOtp(@Valid @ModelAttribute("guestDetailForm") GuestDetailForm form,
                   BindingResult binding,
                   HttpSession session,
                   RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            return "guest/booking/verify";
        }

        BookingDraft draft = BookingSession.getDraft(session);
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

        return "redirect:/book/verify-otp";
    }

    @GetMapping("/verify-otp")
    String showVerifyOtp(HttpSession session, Model model) {
        BookingDraft draft = BookingSession.getDraft(session);
        if (draft.getGuestEmail() == null) {
            return "redirect:/book/verify";
        }

        if (!model.containsAttribute(OTP_FORM)) {
            model.addAttribute(OTP_FORM,
                    new OtpForm(null));
        }
        model.addAttribute("email",
                draft.getGuestEmail());
        return "guest/booking/verify-otp";
    }

    @PostMapping("/verify-otp")
    String verifyOtp(@Valid @ModelAttribute("otpForm") OtpForm otpForm,
                     BindingResult binding,
                     HttpSession session,
                     Model model,
                     RedirectAttributes redirect) {
        BookingDraft draft = BookingSession.getDraft(session);
        if (draft.getGuestEmail() == null) {
            return "redirect:/book/verify";
        }

        if (binding.hasErrors()) {
            model.addAttribute(OTP_FORM,
                    otpForm);
            model.addAttribute("email",
                    draft.getGuestEmail());
            return "guest/booking/verify-otp";
        }

        boolean verified = otpService.verifyOtp(draft.getGuestEmail(),
                otpForm.otp(),
                OtpType.BOOKING_CONFIRM);

        if (!verified) {
            redirect.addFlashAttribute(ERROR,
                    "Invalid or expired OTP");
            return "redirect:/book/verify-otp";
        }

        String token = otpService.issueToken(draft.getGuestEmail(),
                OtpType.BOOKING_CONFIRM);
        session.setAttribute("otpToken", token);

        return "redirect:/book/pay";
    }

    @GetMapping("/pay")
    String showPay(HttpSession session, Model model) {
        BookingDraft draft = BookingSession.getDraft(session);
        if (draft.getGuestEmail() == null) {
            return "redirect:/book/verify";
        }
        model.addAttribute(DRAFT, draft);
        String otpToken = (String) session.getAttribute("otpToken");
        model.addAttribute("token", otpToken);
        return "guest/booking/pay";
    }

    @PostMapping("/pay")
    String createPayPalOrder(HttpSession session,
                             HttpServletRequest request,
                             RedirectAttributes redirect) throws IOException {
        BookingDraft draft = BookingSession.getDraft(session);
        if (draft.getGuestEmail() == null) {
            return "redirect:/book/verify";
        }

        String returnUrl = ServletUriComponentsBuilder.fromRequest(request)
                .replacePath("/book/pay/success")
                .build().toUriString();
        String cancelUrl = ServletUriComponentsBuilder.fromRequest(request)
                .replacePath("/book/pay/cancel")
                .build().toUriString();

        String approvalUrl = paymentService.createOnlineBookingPayment(
                draft.getDepositAmount(), returnUrl, cancelUrl);

        return "redirect:" + approvalUrl;
    }

    @GetMapping("/pay/success")
    String payPalSuccess(@RequestParam("token") String orderId,
                         HttpSession session,
                         RedirectAttributes redirect) throws IOException {
        boolean captured = paymentService.captureOnlineBookingPayment(orderId);
        if (!captured) {
            redirect.addFlashAttribute(ERROR, "Payment could not be completed. Please try again.");
            return "redirect:/book/pay";
        }

        BookingDraft draft = BookingSession.getDraft(session);
        if (draft.getGuestEmail() == null) {
            return "redirect:/book";
        }

        Reservation reservation = reservationService.createOnlineBookingPending(draft, orderId);
        reservationService.confirmAndAddPayment(reservation.getId(), draft.getDepositAmount(), "ONLINE_BOOKING");

        String confirmationCode = reservation.getConfirmationCode();

        session.removeAttribute(BOOKING_DRAFT);
        session.removeAttribute("otpToken");

        return "redirect:/book/confirmation?code=" + confirmationCode;
    }

    @GetMapping("/pay/cancel")
    String payPalCancel(HttpSession session) {
        return "redirect:/book/pay";
    }

    @GetMapping("/confirmation")
    String showConfirmation(@RequestParam String code, Model model) {
        model.addAttribute(BOOKING_CODE,
                code);
        return "guest/booking/confirmation";
    }
}
