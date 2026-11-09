package com.codewithpaolo.frischcheck.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Löst die Ablaufprüfung zeitgesteuert aus (Standard: täglich 08:00 Uhr, siehe app.expiry.check-cron).
 * Enthält bewusst keine Logik – die steckt testbar im {@link ExpiryCheckService}.
 */
@Component
public class ExpiryCheckScheduler {

    private final ExpiryCheckService expiryCheckService;

    public ExpiryCheckScheduler(ExpiryCheckService expiryCheckService) {
        this.expiryCheckService = expiryCheckService;
    }

    @Scheduled(cron = "${app.expiry.check-cron}", zone = "${app.timezone}")
    public void runDailyCheck() {
        expiryCheckService.runCheck();
    }
}
