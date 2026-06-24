package com.hospi.manage.features.receptionist.service;

import com.hospi.manage.features.admin.config.service.SystemConfigService;
import com.hospi.manage.features.payment.entity.Payment;
import com.hospi.manage.features.payment.enums.PaymentMethod;
import com.hospi.manage.features.payment.repository.PaymentRepository;
import com.hospi.manage.features.receptionist.dto.CheckoutCalculation;
import com.hospi.manage.features.reservation.entity.Reservation;
import com.hospi.manage.features.reservation.enums.ReservationStatus;
import com.hospi.manage.features.reservation.repository.ReservationRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class CheckoutService {

    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final SystemConfigService systemConfigService;

    public CheckoutService(ReservationRepository reservationRepository,
                           PaymentRepository paymentRepository,
                           SystemConfigService systemConfigService) {
        this.reservationRepository = reservationRepository;
        this.paymentRepository = paymentRepository;
        this.systemConfigService = systemConfigService;
    }

    public CheckoutCalculation calculate(Reservation reservation, LocalDateTime actualCheckoutTime, int registeredGuestCount) {
        var config = systemConfigService.getConfig();
        BigDecimal totalPrice = reservation.getTotalPrice();

        BigDecimal depositPaid = BigDecimal.ZERO;
        List<Payment> existingPayments = paymentRepository.findAllByReservationId(reservation.getId());
        if (existingPayments != null && !existingPayments.isEmpty()) {
            depositPaid = existingPayments.stream()
                    .map(Payment::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        BigDecimal remainingBalance = totalPrice.subtract(depositPaid);
        if (remainingBalance.compareTo(BigDecimal.ZERO) < 0) {
            remainingBalance = BigDecimal.ZERO;
        }

        boolean isLate = false;
        BigDecimal lateCheckoutFee = BigDecimal.ZERO;
        if (actualCheckoutTime != null) {
            LocalTime standardCheckoutTime = LocalTime.of(11, 0);
            LocalDateTime standardCheckout = LocalDateTime.of(reservation.getCheckOutAt(), standardCheckoutTime);
            isLate = actualCheckoutTime.isAfter(standardCheckout);
            if (isLate) {
                lateCheckoutFee = config.getLateCheckoutFee();
            }
        }

        int totalRoomCapacity = reservation.getDetails().stream()
                .mapToInt(d -> d.getRoomType().getMaxOccupancy() * d.getRoomCount())
                .sum();
        int extraGuestCount = Math.max(0, registeredGuestCount - totalRoomCapacity);
        BigDecimal extraGuestFee = config.getExtraGuestFee().multiply(BigDecimal.valueOf(extraGuestCount));

        BigDecimal totalDue = remainingBalance.add(lateCheckoutFee).add(extraGuestFee);

        return new CheckoutCalculation(
                totalPrice, depositPaid, remainingBalance,
                lateCheckoutFee, extraGuestFee, totalDue,
                isLate, extraGuestCount
        );
    }

    @Transactional
    public Reservation complete(Long reservationId, CheckoutCalculation calc, BigDecimal amountReceived,
                                 PaymentMethod method, String principal, LocalDateTime actualCheckoutTime,
                                 boolean applyLateFee, BigDecimal extraGuestFeeOverride) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));

        if (reservation.getStatus() != ReservationStatus.CHECKED_IN
                && reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new IllegalStateException("Only CHECKED_IN or CONFIRMED reservations can be checked out");
        }

        if (reservation.getStatus() == ReservationStatus.CONFIRMED) {
            reservation.setStatus(ReservationStatus.CHECKED_IN);
            reservation.setCheckedInAt(LocalDateTime.now());
            reservation.setCheckedInBy(principal);
        }

        if (amountReceived.compareTo(calc.totalDue()) != 0) {
            throw new IllegalArgumentException("Received amount does not match total due");
        }

        Payment payment = new Payment();
        payment.setReservation(reservation);
        payment.setAmount(amountReceived);
        payment.setPaymentMethod(method);
        payment.setConfirmedAt(LocalDateTime.now());
        payment.setConfirmedBy(principal);
        paymentRepository.save(payment);

        reservation.setStatus(ReservationStatus.CHECKED_OUT);
        reservation.setCheckedOutAt(LocalDateTime.now());
        reservation.setCheckedOutBy(principal);

        BigDecimal appliedLateFee = applyLateFee ? calc.lateCheckoutFee() : BigDecimal.ZERO;
        reservation.setLateCheckoutFeeApplied(appliedLateFee);

        BigDecimal appliedExtraGuestFee = extraGuestFeeOverride != null
                ? extraGuestFeeOverride : calc.extraGuestFee();
        reservation.setExtraGuestFeeApplied(appliedExtraGuestFee);

        return reservationRepository.save(reservation);
    }
}
