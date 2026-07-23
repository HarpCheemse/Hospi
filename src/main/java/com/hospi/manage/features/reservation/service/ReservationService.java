package com.hospi.manage.features.reservation.service;

import com.hospi.manage.common.exception.ResourceNotFoundException;
import com.hospi.manage.features.account.enums.Role;
import com.hospi.manage.features.guest.dto.BookingDraft;
import com.hospi.manage.features.notification.service.NotificationService;
import com.hospi.manage.features.payment.entity.Payment;
import com.hospi.manage.features.payment.enums.PaymentMethod;
import com.hospi.manage.features.payment.repository.PaymentRepository;
import com.hospi.manage.features.reservation.dto.request.OfflineBookingForm;
import com.hospi.manage.features.reservation.entity.Reservation;
import com.hospi.manage.features.reservation.entity.ReservationDetail;
import com.hospi.manage.features.reservation.enums.BookingSource;
import com.hospi.manage.features.reservation.enums.ReservationStatus;
import com.hospi.manage.features.reservation.repository.ReservationRepository;
import com.hospi.manage.features.room.dto.response.RoomSelection;
import com.hospi.manage.features.room.entity.RoomType;
import com.hospi.manage.features.room.repository.RoomTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Business logic for reservation CRUD, check-in, stay extension, and online booking creation. */
@Service
@RequiredArgsConstructor
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final PaymentRepository paymentRepository;
    private final RoomAvailabilityService roomAvailabilityService;
    private final NotificationService notificationService;
    private final StayingGuestService stayingGuestService;
    private final RoomAssignmentService roomAssignmentService;

    /** Find all reservations with the given status, ordered by check-in date descending. */
    public List<Reservation> findByStatus(ReservationStatus status) {
        return reservationRepository.findByStatusOrderByCheckInAtDesc(status);
    }

    /** Find all reservations matching any of the given statuses. */
    public List<Reservation> findByStatuses(List<ReservationStatus> statuses) {
        return reservationRepository.findByStatusInOrderByCheckInAtDesc(statuses);
    }

    /** Find reservations filtered by statuses, guest name, and optional date. */
    public List<Reservation> findFiltered(List<ReservationStatus> statuses,
                                          String guestName,
                                          LocalDate date) {
        String searchPattern = "%";
        if (guestName != null && !guestName.isBlank()) {
            searchPattern = "%" + guestName.trim().toLowerCase() + "%";
        }
        if (date != null) {
            return reservationRepository.findFilteredWithDate(statuses,
                    searchPattern,
                    date);
        }
        return reservationRepository.findFiltered(statuses,
                searchPattern);
    }

    /** Find reservations filtered by statuses, guest name, and optional date with pagination. */
    public Page<Reservation> findFiltered(List<ReservationStatus> statuses,
                                          String guestName,
                                          LocalDate date,
                                          Pageable pageable) {
        String searchPattern = "%";
        if (guestName != null && !guestName.isBlank()) {
            searchPattern = "%" + guestName.trim().toLowerCase() + "%";
        }
        if (date != null) {
            return reservationRepository.findFilteredWithDate(statuses,
                    searchPattern,
                    date,
                    pageable);
        }
        return reservationRepository.findFiltered(statuses,
                searchPattern,
                pageable);
    }

    /** Find checked-in reservations optionally filtered by search term. */
    public List<Reservation> findCheckedInFiltered(String search) {
        if (search == null || search.isBlank()) {
            return findByStatus(ReservationStatus.CHECKED_IN);
        }
        return reservationRepository.findCheckedInFiltered("%" + search.trim().toLowerCase() + "%");
    }

    /** Find checked-in reservations with pagination and optional search filter. */
    public Page<Reservation> findCheckedInFiltered(String search, Pageable pageable) {
        if (search == null || search.isBlank()) {
            return reservationRepository.findByStatusOrderByCheckInAtDesc(ReservationStatus.CHECKED_IN,
                    pageable);
        }
        return reservationRepository.findCheckedInFiltered("%" + search.trim().toLowerCase() + "%",
                pageable);
    }

    /** Find a reservation by ID. */
    public Reservation findById(Long id) {
        return reservationRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Reservation"));
    }

    /** Find a reservation by its payment idempotency key. */
    public Reservation findByPaymentIdempotencyKey(String paymentIdempotencyKey) {
        return reservationRepository.findByPaymentIdempotencyKey(paymentIdempotencyKey)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation"));
    }

    /** Compute the number of booked rooms per room type for a given date range. */
    public Map<Long, Integer> computeBookedCounts(LocalDate checkInAt, LocalDate checkOutAt) {
        return roomAvailabilityService.computeBookedCounts(checkInAt,
                checkOutAt,
                null);
    }

    /** Create a new offline (receptionist) reservation with room selections. */
    @Transactional
    public Reservation createReservation(OfflineBookingForm form) {
        Map<Long, Integer> required = form.roomSelections().stream()
                .filter(r -> r.roomCount() != null && r.roomCount() > 0)
                .collect(Collectors.toMap(RoomSelection::roomTypeId, RoomSelection::roomCount));
        if (!required.isEmpty() && !roomAvailabilityService.canFulfil(required, form.checkInAt(), form.checkOutAt(), null)) {
            throw new IllegalStateException("Selected rooms are no longer available");
        }

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

            RoomType roomType = roomTypeRepository.findById(roomTypeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Room type " + roomTypeId));

            totalPrice = totalPrice.add(roomType.getBasePrice().multiply(BigDecimal.valueOf(count)));
            details.add(buildDetail(reservation, roomType, count, 1));
        }

        reservation.setDetails(details);
        reservation.setTotalPrice(totalPrice);

        Reservation saved = reservationRepository.save(reservation);

        // Notify receptionists of new reservation
        notificationService.notifyRole(
                Role.RECEPTIONIST,
                "New Reservation",
                "Booking for " + saved.getGuestName() + " (" + saved.getCheckInAt() + " to " + saved.getCheckOutAt() + ")"
        );

        notificationService.notifyRole(
                Role.MANAGER,
                "New Walk-In Booking",
                "Walk-in booking created for " + saved.getGuestName() + " (" + saved.getCheckInAt() + " to " + saved.getCheckOutAt() + ")"
        );

        return saved;
    }

    /** Check in a confirmed reservation. */
    @Transactional
    public Reservation checkIn(Long reservationId, LocalDate today, String bookingCode, String principal) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation"));

        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new IllegalStateException("Only confirmed bookings can be checked in");
        }

        if (reservation.getCheckInAt().isAfter(today)) {
            throw new IllegalStateException("Cannot check in before the booking start date");
        }
        if (reservation.getCheckOutAt().isBefore(today)) {
            throw new IllegalStateException("Cannot check in after the booking has ended");
        }

        if (reservation.getSource() == BookingSource.ONLINE) {
            if (bookingCode == null || bookingCode.isBlank()) {
                throw new IllegalStateException("Booking code is required for online bookings");
            }
            if (!bookingCode.equals(reservation.getConfirmationCode())) {
                throw new IllegalStateException("Invalid booking code");
            }
        }

        int guestCount = stayingGuestService.getGuestCount(reservationId);
        if (guestCount < 1) {
            throw new IllegalStateException("At least one staying guest must be registered before check-in");
        }

        int assignedRooms = roomAssignmentService.getAssignedRoomCount(reservationId);
        int requiredRooms = reservation.getDetails().stream()
                .mapToInt(ReservationDetail::getRoomCount)
                .sum();
        if (assignedRooms < requiredRooms) {
            throw new IllegalStateException(
                    "All rooms must be assigned before check-in (" + assignedRooms + " of " + requiredRooms + " assigned)");
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

    /** Create a pending online reservation from the booking wizard draft. */
    @Transactional
    public Reservation createOnlineBookingPending(BookingDraft draft, String paymentIdempotencyKey) {
        Map<Long, Integer> required = draft.getRooms().selections().stream()
                .collect(Collectors.toMap(
                        BookingDraft.RoomSelection::roomTypeId,
                        BookingDraft.RoomSelection::count
                ));
        if (!roomAvailabilityService.canFulfil(required,
                draft.getDates().checkInAt(),
                draft.getDates().checkOutAt(),
                null)) {
            throw new IllegalStateException("Sorry, some of the rooms you selected are no longer available. Please revise your selection.");
        }

        Reservation reservation = new Reservation();
        reservation.setGuestName(draft.getGuest().name());
        reservation.setGuestEmail(draft.getGuest().email());
        reservation.setGuestPhone(draft.getGuest().phone());
        reservation.setGuestDateOfBirth(draft.getGuest().dateOfBirth());
        reservation.setGuestNationality(draft.getGuest().nationality());
        reservation.setCheckInAt(draft.getDates().checkInAt());
        reservation.setCheckOutAt(draft.getDates().checkOutAt());
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setSource(BookingSource.ONLINE);
        reservation.setConfirmationCode(generateConfirmationCode());
        reservation.setPaymentIdempotencyKey(paymentIdempotencyKey);

        long nights = nightsBetween(draft.getDates().checkInAt(),
                draft.getDates().checkOutAt());

        List<Long> roomTypeIds = draft.getRooms().selections().stream()
                .map(BookingDraft.RoomSelection::roomTypeId)
                .toList();
        Map<Long, RoomType> roomTypeMap = roomTypeRepository.findAllById(roomTypeIds).stream()
                .collect(Collectors.toMap(RoomType::getId,
                        rt -> rt));

        BigDecimal totalPrice = BigDecimal.ZERO;
        List<ReservationDetail> details = new ArrayList<>();

        for (BookingDraft.RoomSelection room : draft.getRooms().selections()) {
            Long roomTypeId = room.roomTypeId();
            Integer count = room.count();

            if (count == null || count <= 0) continue;

            RoomType roomType = roomTypeMap.get(roomTypeId);
            if (roomType == null) {
                throw new ResourceNotFoundException("Room type " + roomTypeId);
            }

            totalPrice = totalPrice.add(roomType.getBasePrice().multiply(BigDecimal.valueOf(count)).multiply(BigDecimal.valueOf(nights)));
            details.add(buildDetail(reservation, roomType, count, nights));
        }

        reservation.setDetails(details);
        reservation.setTotalPrice(totalPrice);

        return reservationRepository.save(reservation);
    }

    /** Confirm a pending reservation and record the PayPal payment. */
    @Transactional
    public void confirmAndAddPayment(Long reservationId, BigDecimal amount, String confirmedBy,
                                     String paymentIdempotencyKey) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation"));

        if (reservation.getStatus() == ReservationStatus.CONFIRMED) {
            return;
        }
        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("Reservation cannot be confirmed in current status");
        }

        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setPaymentIdempotencyKey(paymentIdempotencyKey);

        Payment payment = new Payment();
        payment.setReservation(reservation);
        payment.setAmount(amount);
        payment.setPaymentMethod(PaymentMethod.PAYPAL);
        payment.setConfirmedAt(LocalDateTime.now());
        payment.setConfirmedBy(confirmedBy);
        payment.setOrderId(paymentIdempotencyKey);

        reservationRepository.save(reservation);
        paymentRepository.save(payment);

        notificationService.notifyRole(
                Role.RECEPTIONIST,
                "New Online Booking",
                "Online booking confirmed for " + reservation.getGuestName() + " (" + reservation.getCheckInAt() + " to " + reservation.getCheckOutAt() + ")"
        );

        notificationService.notifyRole(
                Role.MANAGER,
                "New Online Booking",
                "Online booking confirmed for " + reservation.getGuestName() + " (" + reservation.getCheckInAt() + " to " + reservation.getCheckOutAt() + ")"
        );
    }

    /** Confirm an expired reservation (cancelled by cleanup) and record the PayPal payment. */
    @Transactional
    public void confirmExpiredReservation(Long reservationId, BigDecimal amount, String confirmedBy,
                                          String paymentIdempotencyKey) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation"));

        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setPaymentIdempotencyKey(paymentIdempotencyKey);

        Payment payment = new Payment();
        payment.setReservation(reservation);
        payment.setAmount(amount);
        payment.setPaymentMethod(PaymentMethod.PAYPAL);
        payment.setConfirmedAt(LocalDateTime.now());
        payment.setConfirmedBy(confirmedBy);
        payment.setOrderId(paymentIdempotencyKey);

        reservationRepository.save(reservation);
        paymentRepository.save(payment);

        notificationService.notifyRole(
                Role.RECEPTIONIST,
                "New Online Booking (Recovered)",
                "Expired online booking recovered for " + reservation.getGuestName()
                        + " (" + reservation.getCheckInAt() + " to " + reservation.getCheckOutAt() + ")"
        );

        notificationService.notifyRole(
                Role.MANAGER,
                "New Online Booking (Recovered)",
                "Expired online booking recovered for " + reservation.getGuestName()
                        + " (" + reservation.getCheckInAt() + " to " + reservation.getCheckOutAt() + ")"
        );
    }

    /** Cancel a PENDING or CONFIRMED reservation. */
    @Transactional
    public void cancelReservation(Long id) {
        Reservation reservation = findById(id);
        if (reservation.getStatus() != ReservationStatus.PENDING
                && reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new IllegalStateException("Only PENDING or CONFIRMED reservations can be cancelled");
        }
        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
    }

    /** Extend a checked-in reservation by the given number of extra days. */
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
        if (!roomAvailabilityService.canFulfil(required,
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

    private long nightsBetween(LocalDate checkIn, LocalDate checkOut) {
        if (!checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("Check-out date must be after check-in date");
        }
        return ChronoUnit.DAYS.between(checkIn,
                checkOut);
    }

    private ReservationDetail buildDetail(Reservation reservation, RoomType roomType, int count, long nights) {
        ReservationDetail detail = new ReservationDetail();
        detail.setReservation(reservation);
        detail.setRoomType(roomType);
        detail.setRoomCount(count);
        detail.setBasePrice(roomType.getBasePrice());
        BigDecimal lineTotal = roomType.getBasePrice()
                .multiply(BigDecimal.valueOf(count))
                .multiply(BigDecimal.valueOf(nights));
        detail.setTotalPrice(lineTotal);
        return detail;
    }

    /** Find all payments for the given reservation IDs. */
    public List<Payment> findPaymentsByReservationIds(List<Long> reservationIds) {
        if (reservationIds == null || reservationIds.isEmpty()) {
            return List.of();
        }
        return paymentRepository.findByReservationIdIn(reservationIds);
    }

    /** Find reservations by status and check-in date. */
    public List<Reservation> findByStatusAndCheckInAt(ReservationStatus status, LocalDate date) {
        return reservationRepository.findByStatusAndCheckInAt(status, date);
    }

    /** Find reservations by status and check-out date. */
    public List<Reservation> findByStatusAndCheckOutAt(ReservationStatus status, LocalDate date) {
        return reservationRepository.findByStatusAndCheckOutAt(status, date);
    }
}
