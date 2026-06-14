package com.hospi.manage.common.service;

import com.hospi.manage.common.interfaces.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    @Value("${app.mail.console:false}")
    private boolean consoleMode;

    @Override
    public void send(String to, String subject, String content) {
        if (consoleMode) {
            viaConsole(to,
                    subject,
                    content);
            return;
        }
        viaEmail(to,
                subject,
                content);
    }

    private void viaEmail(String to, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);

        mailSender.send(message);
    }

    private void viaConsole(String to, String subject, String content) {
        log.info("""
                        
                        ===== EMAIL =====
                        To: {}
                        Subject: {}
                        
                        {}
                        
                        =================
                        """,
                to,
                subject,
                content);

        return;
    }
}