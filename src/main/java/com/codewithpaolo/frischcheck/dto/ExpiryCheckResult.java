package com.codewithpaolo.frischcheck.dto;

import java.time.LocalDateTime;

/**
 * Ergebnis einer Ablaufprüfung.
 *
 * @param expiringProducts  Anzahl Produkte mit Status WARNUNG, KRITISCH oder ABGELAUFEN
 * @param channelsNotified  Anzahl Kanäle (E-Mail, Telegram), über die erfolgreich gesendet wurde
 */
public record ExpiryCheckResult(LocalDateTime checkedAt, int expiringProducts, int channelsNotified) {
}
