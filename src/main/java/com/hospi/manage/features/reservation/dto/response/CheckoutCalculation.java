package com.hospi.manage.features.reservation.dto.response;

import java.math.BigDecimal;

public record CheckoutCalculation(
    BigDecimal totalPrice,
    BigDecimal depositPaid,
    BigDecimal remainingBalance,
    BigDecimal lateCheckoutFee,
    BigDecimal extraGuestFee,
    BigDecimal totalDue,
    boolean isLate,
    int extraGuestCount
) {}
