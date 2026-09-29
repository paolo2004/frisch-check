package com.codewithpaolo.frischcheck.service.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Verteilt eine Nachricht an alle aktiven Kanäle.
 * Fällt ein Kanal aus, werden die anderen trotzdem bedient.
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final List<NotificationSender> senders;

    public NotificationService(List<NotificationSender> senders) {
        this.senders = senders;
    }

    /** @return Anzahl Kanäle, über die erfolgreich gesendet wurde */
    public int notifyAll(String subject, String message) {
        if (senders.isEmpty()) {
            log.warn("Kein Benachrichtigungskanal aktiv – Nachricht nur im Log:\n{}\n{}", subject, message);
            return 0;
        }

        int successful = 0;
        for (NotificationSender sender : senders) {
            try {
                sender.send(subject, message);
                successful++;
            } catch (RuntimeException ex) {
                // bewusst ohne Stacktrace: Fehlermeldungen können sonst Zugangsdaten (z. B. Bot-Token in URLs) enthalten
                log.error("Versand über {} fehlgeschlagen: {}", sender.channelName(), ex.getMessage());
            }
        }
        return successful;
    }
}
