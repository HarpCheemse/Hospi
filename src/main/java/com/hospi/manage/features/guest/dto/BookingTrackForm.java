package com.hospi.manage.features.guest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Form for tracking a booking by email and booking code. */
public record BookingTrackForm(
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @Size(max = 255, message = "Email must not exceed 255 characters")
        String email,

        @NotBlank(message = "Booking code is required")
        @Size(max = 50, message = "Booking code must not exceed 50 characters")
        String bookingCode
) {
}
