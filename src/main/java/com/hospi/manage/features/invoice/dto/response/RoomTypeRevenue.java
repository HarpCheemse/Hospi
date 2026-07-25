package com.hospi.manage.features.invoice.dto.response;

import java.math.BigDecimal;

/** Revenue breakdown by room type. */
public record RoomTypeRevenue(
        String roomType,
        BigDecimal revenue,
        int bookings,
        BigDecimal percentage
) {}
