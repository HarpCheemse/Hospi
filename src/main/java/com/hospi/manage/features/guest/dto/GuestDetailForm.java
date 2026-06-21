package com.hospi.manage.features.guest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record GuestDetailForm(
        @NotBlank(message = "Full name is required")
        @Size(max = 30, message = "Full name must not exceed 30 characters")
        String guestName,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @Size(max = 255, message = "Email must not exceed 255 characters")
        String guestEmail,

        @NotBlank(message = "Phone is required")
        @Size(max = 30, message = "Phone must not exceed 30 characters")
        String guestPhone,

        @NotNull(message = "Date of birth is required")
        @Past(message = "Date of birth must be in the past")
        LocalDate guestDateOfBirth,

        @Size(max = 30, message = "Nationality must not exceed 30 characters")
        String guestNationality
) {
}
