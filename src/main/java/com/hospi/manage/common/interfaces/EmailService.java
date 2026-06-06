package com.hospi.manage.common.interfaces;

public interface EmailService {

    void send(
            String to,
            String subject,
            String content);

}