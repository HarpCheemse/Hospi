package com.hospi.manage.features.reservation.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class DateSearchForm {

    @NotNull(message = "Check-in date is required")
    private LocalDate checkInAt;

    @NotNull(message = "Check-out date is required")
    private LocalDate checkOutAt;

    public LocalDate getCheckInAt() {
        return checkInAt;
    }

    public void setCheckInAt(LocalDate checkInAt) {
        this.checkInAt = checkInAt;
    }

    public LocalDate getCheckOutAt() {
        return checkOutAt;
    }

    public void setCheckOutAt(LocalDate checkOutAt) {
        this.checkOutAt = checkOutAt;
    }
}
