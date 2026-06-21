package com.hospi.manage.features.reservation.dto.request;

import jakarta.validation.constraints.Size;

public record CheckInForm(
        @Size(max = 10, message = "Booking code must not exceed 10 characters")
        String bookingCode
) {
}
