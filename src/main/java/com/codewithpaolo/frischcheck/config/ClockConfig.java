package com.codewithpaolo.frischcheck.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Eine zentrale {@link Clock} statt {@code LocalDate.now()} überall im Code:
 * so lässt sich "heute" in Tests auf ein festes Datum setzen.
 */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock(@Value("${app.timezone}") String timezone) {
        return Clock.system(ZoneId.of(timezone));
    }
}
