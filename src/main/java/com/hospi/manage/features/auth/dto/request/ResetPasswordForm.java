package com.hospi.manage.features.auth.dto.request;

import jakarta.validation.constraints.Size;

/** Form for setting a new password after a reset. */
public record ResetPasswordForm(
        @Size(max = 255, message = "Password must not exceed 255 characters")
        String newPassword,

        @Size(max = 255, message = "Password must not exceed 255 characters")
        String confirmPassword
) {
}
