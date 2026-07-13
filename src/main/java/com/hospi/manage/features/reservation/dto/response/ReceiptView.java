package com.hospi.manage.features.reservation.dto.response;

import com.hospi.manage.features.payment.entity.Payment;
import com.hospi.manage.features.reservation.entity.Reservation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * View model for the check-out receipt page.
 * No JPA entities are exposed.
 */
public record ReceiptView(
        Reservation reservation,
        BigDecimal roomCharges,
        BigDecimal lateCheckoutFee,
        BigDecimal extraGuestFee,
        BigDecimal totalCharges,
        BigDecimal depositPaid,
        BigDecimal checkoutPayment,
        BigDecimal totalPaid
) {
    /** Create from JPA entities — converts to view models internally. */
    public static ReceiptView from(Reservation reservation, List<Payment> payments) {
        BigDecimal roomCharges = reservation.getTotalPrice();
        BigDecimal lateCheckoutFee = reservation.getLateCheckoutFeeApplied() != null ? reservation.getLateCheckoutFeeApplied() : BigDecimal.ZERO;
        BigDecimal extraGuestFee = reservation.getExtraGuestFeeApplied() != null ? reservation.getExtraGuestFeeApplied() : BigDecimal.ZERO;
        BigDecimal totalCharges = roomCharges.add(lateCheckoutFee).add(extraGuestFee);

        BigDecimal depositPaid = BigDecimal.ZERO;
        BigDecimal checkoutPayment = BigDecimal.ZERO;

        if (payments != null && !payments.isEmpty()) {
            LocalDateTime checkedOutAt = reservation.getCheckedOutAt();
            if (checkedOutAt != null) {
                for (Payment p : payments) {
                    /**
                     * Heuristic: Distinguish checkout payment from deposit using a 5-second buffer.
                     * Since the payment and checkout completion are distinct database operations occurring
                     * near-simultaneously during the checkout flow, payments confirmed within 5 seconds before
                     * or after the checkout completion time are classified as checkout payments. Any older
                     * payments are classified as deposits.
                     */
                    if (p.getConfirmedAt() != null && p.getConfirmedAt().isAfter(checkedOutAt.minusSeconds(5))) {
                        checkoutPayment = checkoutPayment.add(p.getAmount());
                    } else {
                        depositPaid = depositPaid.add(p.getAmount());
                    }
                }
            } else {
                depositPaid = payments.stream()
                        .map(Payment::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
            }
        }

        BigDecimal totalPaid = depositPaid.add(checkoutPayment);

        return new ReceiptView(
                reservation, roomCharges, lateCheckoutFee, extraGuestFee, totalCharges,
                depositPaid, checkoutPayment, totalPaid
        );
    }
}
