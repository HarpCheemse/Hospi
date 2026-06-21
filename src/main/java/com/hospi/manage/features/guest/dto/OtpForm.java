package com.hospi.manage.features.guest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class OtpForm {

    @NotBlank(message = "OTP is required")
    @Size(max = 6, message = "OTP must not exceed 6 characters")
    private String otp;

    public OtpForm() {
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }
}
