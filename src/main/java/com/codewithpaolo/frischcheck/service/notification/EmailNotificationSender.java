package com.codewithpaolo.frischcheck.service.notification;

import com.codewithpaolo.frischcheck.config.NotificationProperties;
import com.codewithpaolo.frischcheck.exception.NotificationException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * Aktiv nur mit app.notification.email.enabled=true. Benötigt zusätzlich spring.mail.* (SMTP).
 */
@Component
@ConditionalOnProperty(prefix = "app.notification.email", name = "enabled", havingValue = "true")
public class EmailNotificationSender implements NotificationSender {

    private final JavaMailSender mailSender;
    private final NotificationProperties.Email config;

    public EmailNotificationSender(JavaMailSender mailSender, NotificationProperties properties) {
        this.mailSender = mailSender;
        this.config = properties.email();
    }

    @Override
    public String channelName() {
        return "E-Mail";
    }

    @Override
    public void send(String subject, String message) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(config.from());
        mail.setTo(config.to().toArray(String[]::new));
        mail.setSubject(subject);
        mail.setText(message);
        try {
            mailSender.send(mail);
        } catch (MailException ex) {
            throw new NotificationException("E-Mail-Versand fehlgeschlagen: " + ex.getMessage());
        }
    }
}
