package com.hospi.manage.common.interfaces;

/** Factory methods for email templates used across the system. */
public final class EmailTemplates {

    private EmailTemplates() {}

    /** Welcome email sent when a new staff account is created. */
    public static EmailTemplate welcome(String name, String email, String password, String role) {
        return new EmailTemplate(
                "Welcome to Hospi - Your Account Has Been Created",
                """
                Hello %s,

                A staff account has been created for you on the Hospi system.

                Email: %s
                Role: %s
                Temporary password: %s

                Please log in and change your password immediately.

                Regards,
                Hospi Administration
                """.formatted(name, email, role, password)
        );
    }

    /** OTP verification code sent during guest booking flow. */
    public static EmailTemplate otpVerification(String code) {
        return new EmailTemplate(
                "Your Hospi Verification Code",
                """
                Your verification code is: %s

                This code expires in 10 minutes.
                If you did not request this, please ignore this email.
                """.formatted(code)
        );
    }

    /** Password reset OTP sent to staff. */
    public static EmailTemplate passwordReset(String code) {
        return new EmailTemplate(
                "Hospi Password Reset Code",
                """
                Your password reset code is: %s

                This code expires in 10 minutes.
                If you did not request a password reset, please ignore this email.
                """.formatted(code)
        );
    }
}
