package com.codewithpaolo.frischcheck.service;

import com.codewithpaolo.frischcheck.model.Category;
import com.codewithpaolo.frischcheck.model.ExpiryLevel;
import com.codewithpaolo.frischcheck.model.Product;
import com.codewithpaolo.frischcheck.model.ProductStatus;
import com.codewithpaolo.frischcheck.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Kern der Anwendung: entscheidet, wie dringend ein Produkt ist.
 *
 * <pre>
 * Tage bis Ablauf  &lt; 0             -> ABGELAUFEN
 * Tage bis Ablauf &lt;= criticalDays  -> KRITISCH
 * Tage bis Ablauf &lt;= warningDays   -> WARNUNG
 * sonst                          -> OK
 * </pre>
 */
@Service
public class ExpiryService {

    private final ProductRepository productRepository;
    private final Clock clock;

    public ExpiryService(ProductRepository productRepository, Clock clock) {
        this.productRepository = productRepository;
        this.clock = clock;
    }

    public LocalDate today() {
        return LocalDate.now(clock);
    }

    /** Positiv = Tage bis zum Ablauf, 0 = läuft heute ab, negativ = bereits abgelaufen. */
    public long daysUntilExpiry(Product product) {
        return ChronoUnit.DAYS.between(today(), product.getExpiryDate());
    }

    public ExpiryLevel evaluate(Product product) {
        long daysLeft = daysUntilExpiry(product);
        Category category = product.getCategory();

        if (daysLeft < 0) {
            return ExpiryLevel.ABGELAUFEN;
        }
        if (daysLeft <= category.getCriticalDays()) {
            return ExpiryLevel.KRITISCH;
        }
        if (daysLeft <= category.getWarningDays()) {
            return ExpiryLevel.WARNUNG;
        }
        return ExpiryLevel.OK;
    }

    /** Lesbarer Text für Oberfläche und Benachrichtigungen, z. B. "läuft morgen ab". */
    public static String describeDays(long daysLeft) {
        if (daysLeft < -1) return "seit " + (-daysLeft) + " Tagen abgelaufen";
        if (daysLeft == -1) return "seit gestern abgelaufen";
        if (daysLeft == 0) return "läuft heute ab";
        if (daysLeft == 1) return "läuft morgen ab";
        return "läuft in " + daysLeft + " Tagen ab";
    }

    /** Alle Produkte auf Lager, die Aufmerksamkeit brauchen – sortiert nach FEFO. */
    @Transactional(readOnly = true)
    public List<Product> findExpiringProducts() {
        return productRepository.findByStatusOrderByExpiryDateAscNameAsc(ProductStatus.AUF_LAGER).stream()
                .filter(product -> evaluate(product).needsAttention())
                .toList();
    }
}
