package utils;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.io.IOException;
import java.util.Properties;

public final class EmailUtils {

    private static final String HOST = "smtp.gmail.com";
    private static final String PORT = "587";
    private static final String FROM;
    private static final String PASSWORD;

    static {
        Properties config = new Properties();
        try {
            config.load(EmailUtils.class
                    .getClassLoader()
                    .getResourceAsStream("config/config.properties"));
            FROM = config.getProperty("mail.username");
            PASSWORD = config.getProperty("mail.password");
        } catch (IOException e) {
            throw new RuntimeException("Failed to load mail config", e);
        }
    }

    private EmailUtils() {
    }

    public static void send(String toEmail, String subject, String body)
            throws MessagingException {

        Properties props = new Properties();
        props.put("mail.smtp.com.hospi.manage.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", HOST);
        props.put("mail.smtp.port", PORT);

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM, PASSWORD);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(FROM));
        message.setRecipients(Message.RecipientType.TO,
                InternetAddress.parse(toEmail));
        message.setSubject(subject);
        message.setText(body);

        Transport.send(message);
    }
}
