package com.hospi.manage.features.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VerifyOtpForm(
        @NotBlank
        @Size(max = 255, message = "Email must not exceed 255 characters")
        String email,

        @NotBlank
        @Size(min = 6, max = 6)
        String otp
) {
}