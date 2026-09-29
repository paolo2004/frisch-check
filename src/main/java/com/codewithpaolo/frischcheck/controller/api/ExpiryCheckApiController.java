package com.codewithpaolo.frischcheck.controller.api;

import com.codewithpaolo.frischcheck.dto.ExpiryCheckResult;
import com.codewithpaolo.frischcheck.service.ExpiryCheckService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Startet die Ablaufprüfung sofort – praktisch zum Testen der Benachrichtigungen,
 * ohne bis 8:00 Uhr zu warten.
 */
@RestController
@RequestMapping("/api/expiry-check")
public class ExpiryCheckApiController {

    private final ExpiryCheckService expiryCheckService;

    public ExpiryCheckApiController(ExpiryCheckService expiryCheckService) {
        this.expiryCheckService = expiryCheckService;
    }

    @PostMapping
    public ExpiryCheckResult runNow() {
        return expiryCheckService.runCheck();
    }
}
