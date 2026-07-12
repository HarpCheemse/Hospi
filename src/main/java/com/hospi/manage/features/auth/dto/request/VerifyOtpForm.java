package com.hospi.manage.features.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Form for verifying an OTP during password reset. */
public record VerifyOtpForm(
        @NotBlank
        @Size(max = 255, message = "Email must not exceed 255 characters")
        String email,

        @NotBlank
        @Size(min = 6, max = 6)
        String otp
) {
}
