package com.hospi.manage.features.credential.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.beans.ConstructorProperties;

/** Form for changing the current user's password. */
public record ChangePasswordForm(

        @NotBlank(message = "Current password is required")
        @Size(max = 255, message = "Password must not exceed 255 characters")
        String currentPassword,

        @NotBlank(message = "New password is required")
        @Size(max = 255, message = "Password must not exceed 255 characters")
        String newPassword,

        @NotBlank(message = "Please confirm your new password")
        @Size(max = 255, message = "Password must not exceed 255 characters")
        String confirmPassword

) {
    @ConstructorProperties({"currentPassword", "newPassword", "confirmPassword"})
    public ChangePasswordForm {
    }
}