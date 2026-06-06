package com.hospi.manage.features.auth.dto;

public record ResetPasswordForm(
        String newPassword,

        String confirmPassword
) {
}