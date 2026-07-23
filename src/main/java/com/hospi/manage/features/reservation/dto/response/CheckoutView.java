package com.hospi.manage.features.reservation.dto.response;

import com.hospi.manage.features.payment.enums.PaymentMethod;
import java.math.BigDecimal;
import java.util.List;

/** View model for the checkout page — charges, deposit, late fee, and remaining due. */
public record CheckoutView(
        ReservationSummaryView reservation,
        BigDecimal roomCharges,
        BigDecimal taxAmount,
        BigDecimal totalCharges,
        BigDecimal depositPaid,
        BigDecimal lateCheckoutFee,
        BigDecimal lateFeeHours,
        boolean showLateFee,
        BigDecimal remainingDue,
        List<PaymentMethod> availableMethods,
        String hotelCheckOutTime,
        String roomNumbers
) {}
