package com.hospi.manage.features.payment.service;

import com.hospi.manage.common.exception.ResourceNotFoundException;
import com.hospi.manage.features.admin.config.entity.SystemConfig;
import com.hospi.manage.features.admin.config.service.SystemConfigService;
import com.hospi.manage.features.payment.entity.Payment;
import com.hospi.manage.features.payment.enums.PaymentMethod;
import com.hospi.manage.features.payment.repository.PaymentRepository;
import com.hospi.manage.features.reservation.entity.Reservation;
import com.hospi.manage.features.reservation.enums.BookingSource;
import com.hospi.manage.features.reservation.enums.ReservationStatus;
import com.hospi.manage.features.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/** Orchestrates PayPal operations and payment record persistence. */
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final SystemConfigService systemConfigService;
    private final PayPalService payPalService;

    /** Create a PayPal order for the deposit amount. */
    @Transactional
    public String createOnlineBookingPayment(BigDecimal amount, String returnUrl, String cancelUrl) throws IOException {
        return payPalService.createOrder(amount.setScale(2,
                        RoundingMode.HALF_UP).toString(),
                returnUrl,
                cancelUrl);
    }

    /** Capture a PayPal order. Returns true if COMPLETED. */
    @Transactional
    public boolean captureOnlineBookingPayment(String orderId)
            throws IOException {
        return payPalService.captureOrder(orderId);
    }

    /** Confirm an offline reservation and record staff-processed payment. */
    @Transactional
    public Payment confirmPayment(Long reservationId, PaymentMethod method, String confirmedBy) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation"));

        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("Booking is not in PENDING status");
        }
        if (reservation.getSource() != BookingSource.OFFLINE) {
            throw new IllegalStateException("Only offline bookings can be confirmed at reception");
        }
        if (paymentRepository.existsByReservationId(reservationId)) {
            throw new IllegalStateException("Payment already exists for this reservation");
        }

        Payment payment = new Payment();
        payment.setReservation(reservation);
        payment.setAmount(reservation.getTotalPrice());
        payment.setPaymentMethod(method);
        payment.setConfirmedAt(LocalDateTime.now());
        payment.setConfirmedBy(confirmedBy);

        reservation.setStatus(ReservationStatus.CONFIRMED);

        paymentRepository.save(payment);
        reservationRepository.save(reservation);

        return payment;
    }

    /** Refund a captured PayPal order by order ID. Used when payment captured but reservation expired. */
    @Transactional
    public boolean refundOnlineBookingPayment(String orderId)
            throws IOException {
        return payPalService.refundOrder(orderId);
    }

    /** Calculate refund amount based on system config (full vs partial refund window). */
    public BigDecimal calculateRefund(Reservation reservation) {
        SystemConfig config = systemConfigService.getConfig();
        long hoursSinceCreation = ChronoUnit.HOURS.between(reservation.getCreatedAt(),
                LocalDateTime.now());

        if (hoursSinceCreation <= config.getFullRefundWindowHours()) {
            return reservation.getTotalPrice();
        }

        return reservation.getTotalPrice().multiply(config.getRefundPercentage()).divide(BigDecimal.valueOf(100),
                RoundingMode.HALF_UP);
    }
}
