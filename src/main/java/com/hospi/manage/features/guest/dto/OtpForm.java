package com.hospi.manage.features.guest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OtpForm(
        @NotBlank(message = "OTP is required")
        @Size(max = 6, message = "OTP must not exceed 6 characters")
        String otp
) {
}
