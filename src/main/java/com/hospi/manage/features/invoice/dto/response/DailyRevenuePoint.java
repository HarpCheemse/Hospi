package com.hospi.manage.features.invoice.dto.response;

import java.math.BigDecimal;

/** A single day's revenue data point (for line chart). */
public record DailyRevenuePoint(
        String label,
        BigDecimal revenue,
        int bookings
) {}
