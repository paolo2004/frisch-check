package com.codewithpaolo.frischcheck.service;

import com.codewithpaolo.frischcheck.dto.ExpiryCheckResult;
import com.codewithpaolo.frischcheck.model.ExpiryLevel;
import com.codewithpaolo.frischcheck.model.Product;
import com.codewithpaolo.frischcheck.service.notification.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Führt die Ablaufprüfung aus und verschickt eine zusammengefasste Warnung.
 * Wird täglich vom {@link ExpiryCheckScheduler} und manuell per REST aufgerufen.
 */
@Service
public class ExpiryCheckService {

    private static final Logger log = LoggerFactory.getLogger(ExpiryCheckService.class);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final ExpiryService expiryService;
    private final NotificationService notificationService;
    private final Clock clock;

    public ExpiryCheckService(ExpiryService expiryService, NotificationService notificationService, Clock clock) {
        this.expiryService = expiryService;
        this.notificationService = notificationService;
        this.clock = clock;
    }

    public ExpiryCheckResult runCheck() {
        LocalDateTime now = LocalDateTime.now(clock);
        List<Product> expiring = expiryService.findExpiringProducts();

        if (expiring.isEmpty()) {
            log.info("Ablaufprüfung: keine Produkte mit Handlungsbedarf");
            return new ExpiryCheckResult(now, 0, 0);
        }

        String subject = "Ablaufwarnung: " + expiring.size() + " Produkt(e) brauchen Aufmerksamkeit";
        int channels = notificationService.notifyAll(subject, buildMessage(expiring));
        log.info("Ablaufprüfung: {} Produkt(e) gemeldet über {} Kanal/Kanäle", expiring.size(), channels);
        return new ExpiryCheckResult(now, expiring.size(), channels);
    }

    String buildMessage(List<Product> expiring) {
        // Gruppieren nach Dringlichkeit; innerhalb der Gruppe bleibt die FEFO-Reihenfolge erhalten
        Map<ExpiryLevel, List<Product>> byLevel = expiring.stream()
                .collect(Collectors.groupingBy(expiryService::evaluate));

        StringBuilder sb = new StringBuilder("Stand: ").append(expiryService.today().format(DATE)).append("\n");
        for (ExpiryLevel level : List.of(ExpiryLevel.ABGELAUFEN, ExpiryLevel.KRITISCH, ExpiryLevel.WARNUNG)) {
            List<Product> products = byLevel.getOrDefault(level, List.of());
            if (products.isEmpty()) {
                continue;
            }
            sb.append("\n").append(level.name()).append(" (").append(products.size()).append(")\n");
            products.forEach(p -> sb.append(formatLine(p)).append("\n"));
        }
        return sb.toString();
    }

    private String formatLine(Product p) {
        return "- %s (%s) – %s %s – %s – %s (%s)".formatted(
                p.getName(),
                p.getCategory().getName(),
                p.getQuantity().stripTrailingZeros().toPlainString(),
                p.getUnit().getLabel(),
                p.getStorageLocation().getLabel(),
                ExpiryService.describeDays(expiryService.daysUntilExpiry(p)),
                p.getExpiryDate().format(DATE));
    }
}
