package com.codewithpaolo.frischcheck.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Typsichere Abbildung von {@code app.notification.*} aus der application.yml.
 */
@ConfigurationProperties(prefix = "app.notification")
public record NotificationProperties(Email email, Telegram telegram) {

    public record Email(boolean enabled, String from, List<String> to) {
    }

    public record Telegram(boolean enabled, String botToken, String chatId) {
    }
}
