package com.hospi.manage.features.dashboard.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

/** View model for a recent reservation row on the manager dashboard. */
public record RecentReservationView(
        String guestName,
        LocalDate checkInAt,
        LocalDate checkOutAt,
        String statusName,
        BigDecimal totalPrice
) {}
