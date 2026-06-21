package com.hospi.manage.features.reservation.dto;

import java.time.LocalDate;
import java.util.List;

public record ActiveBookingsView(
    List<ReservationListItemView> bookings,
    String filterStatus,
    LocalDate filterDate,
    String filterSearch,
    int page,
    int totalPages,
    long total
) {}