package com.codewithpaolo.frischcheck.service.notification;

import com.codewithpaolo.frischcheck.config.NotificationProperties;
import com.codewithpaolo.frischcheck.exception.NotificationException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;

/**
 * Sendet über die Telegram Bot API (https://core.telegram.org/bots/api#sendmessage).
 * Aktiv nur mit app.notification.telegram.enabled=true.
 */
@Component
@ConditionalOnProperty(prefix = "app.notification.telegram", name = "enabled", havingValue = "true")
public class TelegramNotificationSender implements NotificationSender {

    /** Telegram erlaubt max. 4096 Zeichen pro Nachricht. */
    private static final int MAX_LENGTH = 4096;

    private final RestClient restClient;
    private final NotificationProperties.Telegram config;

    public TelegramNotificationSender(RestClient.Builder restClientBuilder, NotificationProperties properties) {
        this.config = properties.telegram();
        if (!StringUtils.hasText(config.botToken()) || !StringUtils.hasText(config.chatId())) {
            throw new IllegalStateException(
                    "Telegram ist aktiviert, aber TELEGRAM_BOT_TOKEN oder TELEGRAM_CHAT_ID fehlt");
        }
        this.restClient = restClientBuilder.baseUrl("https://api.telegram.org").build();
    }

    @Override
    public String channelName() {
        return "Telegram";
    }

    @Override
    public void send(String subject, String message) {
        String text = subject + "\n\n" + message;
        if (text.length() > MAX_LENGTH) {
            text = text.substring(0, MAX_LENGTH - 20) + "\n… (gekürzt)";
        }
        try {
            restClient.post()
                    .uri("/bot{token}/sendMessage", config.botToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("chat_id", config.chatId(), "text", text))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException ex) {
            // Antwort von Telegram enthält eine lesbare Beschreibung, aber nie den Token
            throw new NotificationException("Telegram antwortete mit " + ex.getStatusCode()
                    + ": " + ex.getResponseBodyAsString());
        } catch (RestClientException ex) {
            // ex.getMessage() enthält hier die URL inkl. Bot-Token -> nicht weitergeben
            throw new NotificationException("Telegram nicht erreichbar (" + ex.getClass().getSimpleName() + ")");
        }
    }
}
