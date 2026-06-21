package com.hospi.manage.features.reservation.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record DateSearchForm(
    @NotNull(message = "Check-in date is required") LocalDate checkInAt,
    @NotNull(message = "Check-out date is required") LocalDate checkOutAt
) {}
