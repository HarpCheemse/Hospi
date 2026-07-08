package com.hospi.manage.features.invoice.dto.response;

import java.math.BigDecimal;

/** Revenue breakdown by booking source (online vs offline). */
public record BookingSourceRevenue(
        String source,
        BigDecimal revenue,
        BigDecimal percentage
) {}
