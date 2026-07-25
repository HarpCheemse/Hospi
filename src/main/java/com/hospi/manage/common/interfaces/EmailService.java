package com.hospi.manage.common.interfaces;

/** Service for sending emails and masking email addresses for display. */
public interface EmailService {

    /** Send an email to the given recipient. */
    void send(
            String to,
            String subject,
            String content);

    /** Mask an email address for safe display (e.g. joh...@example.com). */
    default String maskEmail(String email) {
        if (email == null || email.isBlank()) {
            return "***";
        }
        int atIndex = email.indexOf('@');
        if (atIndex > 0) {
            return email.substring(0, Math.min(atIndex, 3)) + "...";
        }
        return "***";
    }

}