package com.hospi.manage.common.interfaces;

public interface EmailService {

    void send(
            String to,
            String subject,
            String content);

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