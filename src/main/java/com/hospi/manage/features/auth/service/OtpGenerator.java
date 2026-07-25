package com.hospi.manage.features.auth.service;

import java.security.SecureRandom;

/** Utility that generates cryptographically random 6-digit OTP codes. */
public class OtpGenerator {

    private static final SecureRandom random = new SecureRandom();

    /** Generate a random 6-digit OTP code. */
    public static String generate6DigitOtp() {
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
}
