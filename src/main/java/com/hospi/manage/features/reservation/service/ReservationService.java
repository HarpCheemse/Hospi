package com.hospi.manage.features.reservation.service;

import com.hospi.manage.features.admin.account.enums.Role;
import com.hospi.manage.features.guest.dto.BookingDraft;
import com.hospi.manage.features.notification.service.NotificationService;
import com.hospi.manage.features.payment.entity.Payment;
import com.hospi.manage.features.payment.enums.PaymentMethod;
import com.hospi.manage.features.payment.repository.PaymentRepository;
import com.hospi.manage.features.reservation.dto.OfflineBookingForm;
import com.hospi.manage.features.reservation.entity.Reservation;
import com.hospi.manage.features.reservation.entity.ReservationDetail;
import com.hospi.manage.features.reservation.enums.BookingSource;
import com.hospi.manage.features.reservation.enums.ReservationStatus;
import com.hospi.manage.features.reservation.repository.ReservationRepository;
import com.hospi.manage.features.room.dto.room.RoomSelection;
import com.hospi.manage.features.room.entity.RoomType;
import com.hospi.manage.features.room.repository.RoomTypeRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final PaymentRepository paymentRepository;
    private final RoomAvailabilityService roomAvailabilityService;
    private final AvailabilityEngine availabilityEngine;
    private final NotificationService notificationService;

    public ReservationService(ReservationRepository reservationRepository, RoomTypeRepository roomTypeRepository,
                              PaymentRepository paymentRepository, RoomAvailabilityService roomAvailabilityService,
                              AvailabilityEngine engine,
                              NotificationService notificationService) {
        this.reservationRepository = reservationRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.paymentRepository = paymentRepository;
        this.roomAvailabilityService = roomAvailabilityService;
        this.availabilityEngine = engine;
        this.notificationService = notificationService;
    }

    public List<Reservation> findByStatus(ReservationStatus status) {
        return reservationRepository.findByStatusOrderByCheckInAtDesc(status);
    }

    public List<Reservation> findByStatuses(List<ReservationStatus> statuses) {
        return reservationRepository.findByStatusInOrderByCheckInAtDesc(statuses);
    }

    public Reservation findById(Long id) {
        return reservationRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Reservation not found"));
    }

    /// This function return RoomTypeId and amount of reservations for a given day range
    public Map<Long, Integer> getBookedCounts(LocalDate checkInAt, LocalDate checkOutAt) {
        return availabilityEngine.computeBookedCounts(checkInAt,
                checkOutAt,
                null);
    }

    @Transactional
    public Reservation createReservation(OfflineBookingForm form) {
        Reservation reservation = new Reservation();
        reservation.setGuestName(form.guestName());
        reservation.setGuestEmail(form.guestEmail());
        reservation.setGuestPhone(form.guestPhone());
        reservation.setGuestDateOfBirth(form.guestDateOfBirth());
        reservation.setGuestNationality(form.guestNationality());
        reservation.setCheckInAt(form.checkInAt());
        reservation.setCheckOutAt(form.checkOutAt());
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setSource(BookingSource.OFFLINE);

        BigDecimal totalPrice = BigDecimal.ZERO;
        List<ReservationDetail> details = new ArrayList<>();

        for (RoomSelection room : form.roomSelections()) {
            Long roomTypeId = room.roomTypeId();
            Integer count = room.roomCount();

            if (count == null || count <= 0) continue;

            RoomType roomType = roomTypeRepository.findById(roomTypeId).orElse(null);
            if (roomType == null) continue;

            ReservationDetail detail = new ReservationDetail();
            detail.setReservation(reservation);
            detail.setRoomType(roomType);
            detail.setRoomCount(count);
            detail.setBasePrice(roomType.getBasePrice());

            BigDecimal lineTotal = roomType.getBasePrice().multiply(BigDecimal.valueOf(count));
            detail.setTotalPrice(lineTotal);
            totalPrice = totalPrice.add(lineTotal);

            details.add(detail);
        }

        reservation.setDetails(details);
        reservation.setTotalPrice(totalPrice);

        Reservation saved = reservationRepository.save(reservation);

        // Notify receptionists of new reservation
        notificationService.notifyRole(
                saved.getHotelId(),
                notificationService.roleToId(Role.RECEPTIONIST),
                "New Reservation",
                "Booking for " + saved.getGuestName() + " (" + saved.getCheckInAt() + " to " + saved.getCheckOutAt() + ")",
                "RESERVATION",
                String.valueOf(saved.getId())
        );

        return saved;
    }

    @Transactional
    public Reservation checkIn(Long reservationId, String bookingCode, String principal) {
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(() -> new IllegalArgumentException("Reservation not found"));

        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new IllegalStateException("Only confirmed bookings can be checked in");
        }

        if (reservation.getSource() == BookingSource.ONLINE) {
            if (bookingCode == null || bookingCode.isBlank()) {
                throw new IllegalStateException("Booking code is required for online bookings");
            }
            if (!bookingCode.equals(reservation.getConfirmationCode())) {
                throw new IllegalStateException("Invalid booking code");
            }
        }

        reservation.setStatus(ReservationStatus.CHECKED_IN);
        reservation.setCheckedInAt(LocalDateTime.now());
        reservation.setCheckedInBy(principal);

        return reservationRepository.save(reservation);
    }

    private static final SecureRandom RANDOM = new SecureRandom();

    private String generateConfirmationCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder code = new StringBuilder("HSP-");
        for (int i = 0; i < 6; i++) {
            code.append(chars.charAt(RANDOM.nextInt(chars.length())));
        }
        return code.toString();
    }

    @Transactional
    public Reservation createOnlineBooking(BookingDraft draft) {
        Reservation reservation = new Reservation();
        reservation.setGuestName(draft.getGuestName());
        reservation.setGuestEmail(draft.getGuestEmail());
        reservation.setGuestPhone(draft.getGuestPhone());
        reservation.setGuestDateOfBirth(draft.getGuestDateOfBirth());
        reservation.setGuestNationality(draft.getGuestNationality());
        reservation.setCheckInAt(draft.getCheckInAt());
        reservation.setCheckOutAt(draft.getCheckOutAt());
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setSource(BookingSource.ONLINE);
        reservation.setConfirmationCode(generateConfirmationCode());

        BigDecimal totalPrice = BigDecimal.ZERO;
        List<ReservationDetail> details = new ArrayList<>();

        for (BookingDraft.RoomSelection room : draft.getRoomSelections()) {
            Long roomTypeId = room.roomTypeId();
            Integer count = room.count();

            if (count == null || count <= 0) continue;

            RoomType roomType = roomTypeRepository.findById(roomTypeId).orElse(null);
            if (roomType == null) continue;

            ReservationDetail detail = new ReservationDetail();
            detail.setReservation(reservation);
            detail.setRoomType(roomType);
            detail.setRoomCount(count);
            detail.setBasePrice(roomType.getBasePrice());

            BigDecimal lineTotal = roomType.getBasePrice().multiply(BigDecimal.valueOf(count));
            detail.setTotalPrice(lineTotal);
            totalPrice = totalPrice.add(lineTotal);

            details.add(detail);
        }

        reservation.setDetails(details);
        reservation.setTotalPrice(totalPrice);

        reservation = reservationRepository.save(reservation);

        Payment payment = new Payment();
        payment.setReservation(reservation);
        payment.setAmount(draft.getDepositAmount());
        payment.setPaymentMethod(PaymentMethod.PAYPAL);
        payment.setConfirmedAt(LocalDateTime.now());
        payment.setConfirmedBy(draft.getGuestEmail());

        paymentRepository.save(payment);

        return reservation;
    }

    @Transactional
    public Reservation extendStay(Long reservationId, int extraDays) {
        if (extraDays <= 0) {
            throw new IllegalArgumentException("Extra days must be at least 1");
        }

        Reservation reservation = findById(reservationId);

        if (reservation.getStatus() != ReservationStatus.CHECKED_IN) {
            throw new IllegalStateException("Only checked-in reservations can be extended");
        }

        LocalDate newCheckout = reservation.getCheckOutAt().plusDays(extraDays);

        Map<Long, Integer> required = reservation.getDetails().stream().collect(Collectors.groupingBy(d -> d.getRoomType().getId(),
                Collectors.summingInt(ReservationDetail::getRoomCount)));

        // canFulfil returns true when rooms ARE available — no inversion needed
        if (!availabilityEngine.canFulfil(required,
                reservation.getCheckOutAt(),
                newCheckout,
                reservationId)) {
            throw new IllegalStateException("Cannot extend stay: room not available for extended period");
        }

        reservation.setCheckOutAt(newCheckout);
        reservation.setTotalPrice(reservation.getTotalPrice().add(calculateExtensionCost(reservation,
                extraDays)));

        return reservationRepository.save(reservation);
    }

    private BigDecimal calculateExtensionCost(Reservation reservation, int extraDays) {

        BigDecimal totalPerNight = reservation.getDetails().stream().map(detail -> detail.getBasePrice().multiply(BigDecimal.valueOf(detail.getRoomCount()))).reduce(BigDecimal.ZERO,
                BigDecimal::add);

        return totalPerNight.multiply(BigDecimal.valueOf(extraDays));
    }
}
