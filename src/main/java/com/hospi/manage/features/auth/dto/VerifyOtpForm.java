package com.hospi.manage.features.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VerifyOtpForm(
        @NotBlank
        String email,

        @NotBlank
        @Size(min = 6, max = 6)
        String otp
) {
}