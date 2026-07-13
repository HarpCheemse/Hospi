package com.hospi.manage.features.reservation.dto.response;

import com.hospi.manage.features.payment.entity.Payment;
import com.hospi.manage.features.reservation.entity.Reservation;

import java.math.BigDecimal;
import java.util.List;

/**
 * View model for the check-out receipt page.
 * No JPA entities are exposed.
 */
public record ReceiptView(
        ReservationSummaryView reservation,
        BigDecimal depositPaid,
        BigDecimal totalPaid
) {
    /** Create from JPA entities — converts to view models internally. */
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

        return new ReceiptView(ReservationSummaryView.from(reservation), depositPaid, totalPaid);
    }
}
