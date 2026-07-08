package com.hospi.manage.features.invoice.dto.response;

import java.math.BigDecimal;

/** A single data point on the revenue chart. */
public record RevenueChartPoint(
        String label,
        BigDecimal revenue,
        int bookings
) {}
