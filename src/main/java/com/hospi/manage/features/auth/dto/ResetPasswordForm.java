package com.hospi.manage.features.auth.dto;

import jakarta.validation.constraints.Size;

public record ResetPasswordForm(
        @Size(max = 255, message = "Password must not exceed 255 characters")
        String newPassword,

        @Size(max = 255, message = "Password must not exceed 255 characters")
        String confirmPassword
) {
}