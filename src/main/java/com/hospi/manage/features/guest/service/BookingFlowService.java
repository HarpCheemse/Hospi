package com.hospi.manage.features.guest.service;

import com.hospi.manage.common.interfaces.EmailService;
import com.hospi.manage.features.admin.config.service.SystemConfigService;
import com.hospi.manage.features.auth.enums.OtpType;
import com.hospi.manage.features.auth.service.OtpService;
import com.hospi.manage.features.guest.dto.BookingDraft;
import com.hospi.manage.features.payment.service.PaymentService;
import com.hospi.manage.features.reservation.dto.response.RoomTypeAvailabilityView;
import com.hospi.manage.features.reservation.entity.Reservation;
import com.hospi.manage.features.reservation.service.ReservationService;
import com.hospi.manage.features.reservation.service.RoomAvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/** Orchestrates availability checks, room selection, OTP, and pending reservation creation for the booking wizard. */
@Service
@RequiredArgsConstructor
public class BookingFlowService {

    private final RoomAvailabilityService roomAvailabilityService;
    private final SystemConfigService systemConfigService;
    private final ReservationService reservationService;
    private final OtpService otpService;
    private final EmailService emailService;
    private final PaymentService paymentService;

    /** View model for the room selection page. */
    public record AvailabilityView(
            List<RoomTypeAvailabilityView> availability,
            Integer maxRooms,
            BigDecimal depositPercentage,
            long nights
    ) {}

    /** Fetch real-time availability and build the room selection view model. */
    public AvailabilityView buildAvailabilityView(BookingDraft draft) {
        List<RoomTypeAvailabilityView> availability = roomAvailabilityService.getAvailability(
                        draft.getDates().checkInAt(),
                        draft.getDates().checkOutAt())
                .stream()
                .map(RoomTypeAvailabilityView::new)
                .toList();
        var config = systemConfigService.getConfig();
        long nights = ChronoUnit.DAYS.between(draft.getDates().checkInAt(), draft.getDates().checkOutAt());
        return new AvailabilityView(availability,
                config.getMaximumRoomPerBook(),
                config.getDefaultDepositPercentage(),
                nights);
    }

    /** Validate room selections, compute pricing, update draft. Throws IllegalArgumentException on validation failure. */
    public void processRoomSelections(BookingDraft draft,
                                       List<Long> roomTypeIds,
                                       List<Integer> counts) {
        List<RoomTypeAvailabilityView> availabilityList = roomAvailabilityService.getAvailability(
                        draft.getDates().checkInAt(),
                        draft.getDates().checkOutAt())
                .stream()
                .map(RoomTypeAvailabilityView::new)
                .toList();

        Map<Long, RoomTypeAvailabilityView> availabilityMap = availabilityList.stream()
                .collect(Collectors.toMap(RoomTypeAvailabilityView::roomTypeId, a -> a));

        if (roomTypeIds == null || counts == null) {
            throw new IllegalArgumentException("Please select at least one room.");
        }

        if (roomTypeIds.size() != counts.size()) {
            throw new IllegalArgumentException("Invalid room selection data");
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
            throw new IllegalArgumentException("Please select at least one room.");
        }

        for (BookingDraft.RoomSelection s : selections) {
            RoomTypeAvailabilityView rt = availabilityMap.get(s.roomTypeId());
            if (rt != null && s.count() > rt.availableRooms()) {
                throw new IllegalArgumentException(
                        "Room type '" + s.roomTypeName() + "' only has " + rt.availableRooms() + " rooms available.");
            }
        }

        var config = systemConfigService.getConfig();
        int maxRooms = config.getMaximumRoomPerBook() != null ? config.getMaximumRoomPerBook() : Integer.MAX_VALUE;
        int totalRooms = selections.stream().mapToInt(BookingDraft.RoomSelection::count).sum();
        if (totalRooms > maxRooms) {
            throw new IllegalArgumentException(
                    "Total rooms selected (" + totalRooms + ") exceeds the maximum of " + maxRooms + " per booking.");
        }

        long nights = draft.getDates().checkOutAt().toEpochDay() - draft.getDates().checkInAt().toEpochDay();
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (BookingDraft.RoomSelection s : selections) {
            totalPrice = totalPrice.add(s.basePrice().multiply(BigDecimal.valueOf(s.count()).multiply(BigDecimal.valueOf(nights))));
        }

        BigDecimal depositPercentage = config.getDefaultDepositPercentage();
        BigDecimal depositAmount = totalPrice.multiply(depositPercentage)
                .divide(BigDecimal.valueOf(100),
                        RoundingMode.HALF_UP);

        draft.setRooms(new BookingDraft.BookingRooms(selections, totalPrice, depositAmount, nights));
    }

    /** Create and send OTP to guest email. Returns masked email or null if cooldown active. */
    public String initiateBookingOtp(BookingDraft draft) {
        String otp = otpService.createOtp(draft.getGuest().email(),
                OtpType.BOOKING_CONFIRM);
        if (otp == null) {
            return null;
        }
        emailService.send(draft.getGuest().email(),
                "Your Booking OTP Code",
                "Your OTP code is: " + otp
                        + "\n\nThis code expires in 10 minutes.\n\nThank you for choosing Hospi!");
        return emailService.maskEmail(draft.getGuest().email());
    }

    /** Verify OTP and return verification token, or null if invalid/expired. */
    public String verifyBookingOtp(String email, String otp) {
        boolean verified = otpService.verifyOtp(email, otp, OtpType.BOOKING_CONFIRM);
        if (!verified) {
            return null;
        }
        return otpService.issueToken(email, OtpType.BOOKING_CONFIRM);
    }

    /** Delegate: create PENDING reservation with idempotency key for PayPal callback correlation. */
    public Reservation createPendingReservation(BookingDraft draft, String paymentIdempotencyKey) {
        return reservationService.createOnlineBookingPending(draft, paymentIdempotencyKey);
    }
}
