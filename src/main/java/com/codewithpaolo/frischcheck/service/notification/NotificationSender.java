package com.codewithpaolo.frischcheck.service.notification;

/**
 * Ein Benachrichtigungskanal. Neue Kanäle (z. B. Slack, SMS) = neue Implementierung, sonst keine Änderung.
 */
public interface NotificationSender {

    String channelName();

    /**
     * @throws com.codewithpaolo.frischcheck.exception.NotificationException wenn der Versand fehlschlägt
     */
    void send(String subject, String message);
}
