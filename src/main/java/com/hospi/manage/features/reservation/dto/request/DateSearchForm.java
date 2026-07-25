package com.hospi.manage.features.reservation.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/** Form for searching availability by check-in and check-out dates. */
public record DateSearchForm(
    @NotNull(message = "Check-in date is required") LocalDate checkInAt,
    @NotNull(message = "Check-out date is required") LocalDate checkOutAt
) {}
