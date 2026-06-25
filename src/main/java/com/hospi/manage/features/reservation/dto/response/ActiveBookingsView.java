package com.hospi.manage.features.reservation.dto.response;

import java.time.LocalDate;
import java.util.List;

/** View model for the active bookings list page with pagination and filters. */
public record ActiveBookingsView(
    List<ReservationListItemView> bookings,
    String filterStatus,
    LocalDate filterDate,
    String filterSearch,
    int page,
    int totalPages,
    long total
) {}