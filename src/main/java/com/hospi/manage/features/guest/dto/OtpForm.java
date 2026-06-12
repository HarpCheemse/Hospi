package com.hospi.manage.features.guest.dto;

import jakarta.validation.constraints.NotBlank;

public class OtpForm {

    @NotBlank(message = "OTP is required")
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
