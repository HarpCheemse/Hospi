package com.hospi.manage.features.auth.enums;

/**
 * Purpose types for one-time password challenges.
 */
public enum OtpType {
    /** Verify the guest's email address during booking. */
    EMAIL_VERIFY,
    /** Reset a staff account password. */
    PASSWORD_RESET,
    /** Confirm an online booking via OTP. */
    BOOKING_CONFIRM,
    /** Track an existing booking using an OTP link. */
    BOOKING_TRACK
}