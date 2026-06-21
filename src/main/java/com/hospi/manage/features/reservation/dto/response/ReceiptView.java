package com.hospi.manage.features.receptionist.dto;

import com.hospi.manage.features.payment.entity.Payment;
import com.hospi.manage.features.reservation.entity.Reservation;

import java.math.BigDecimal;
import java.util.List;

public record ReceiptView(
        Reservation reservation,
        BigDecimal depositPaid,
        BigDecimal totalPaid
) {
    public static ReceiptView from(Reservation reservation, List<Payment> payments) {
        BigDecimal depositPaid = payments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPaid = depositPaid;
        if (reservation.getLateCheckoutFeeApplied() != null) {
            totalPaid = totalPaid.add(reservation.getLateCheckoutFeeApplied());
        }
        if (reservation.getExtraGuestFeeApplied() != null) {
            totalPaid = totalPaid.add(reservation.getExtraGuestFeeApplied());
        }

        return new ReceiptView(reservation, depositPaid, totalPaid);
    }
}
