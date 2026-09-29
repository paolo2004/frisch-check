package com.codewithpaolo.frischcheck.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Aktiviert {@code @Scheduled} (tägliche Ablaufprüfung).
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
