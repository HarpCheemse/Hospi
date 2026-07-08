package com.hospi.manage.features.invoice.dto.response;

import java.math.BigDecimal;

/** Revenue breakdown by payment method. */
public record PaymentMethodRevenue(
        String method,
        BigDecimal revenue,
        BigDecimal percentage
) {}
