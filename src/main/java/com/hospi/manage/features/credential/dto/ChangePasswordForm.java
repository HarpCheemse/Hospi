package com.hospi.manage.features.credential.dto;

import jakarta.validation.constraints.NotBlank;

import java.beans.ConstructorProperties;

public record ChangePasswordForm(

        @NotBlank(message = "Current password is required")
        String currentPassword,

        @NotBlank(message = "New password is required")
        String newPassword,

        @NotBlank(message = "Please confirm your new password")
        String confirmPassword

) {
    @ConstructorProperties({"currentPassword", "newPassword", "confirmPassword"})
    public ChangePasswordForm {
    }
}