package com.codewithpaolo.frischcheck.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Typsichere Abbildung von {@code app.expiry.*} aus der application.yml.
 */
@ConfigurationProperties(prefix = "app.expiry")
public record ExpiryProperties(String checkCron, List<CategoryThreshold> categories) {

    public ExpiryProperties {
        categories = categories == null ? List.of() : categories;
    }

    public record CategoryThreshold(String name, int warningDays, int criticalDays) {
    }
}
