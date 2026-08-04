package com.ufcg.psoft.project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

@Configuration
public class TestClockConfig {

    public static final Clock FIXED_CLOCK = Clock.fixed(
        Instant.parse("2026-08-04T20:00:00Z"),
        ZoneOffset.UTC
    );

    @Bean
    @Primary
    public Clock fixedClock() {
        return FIXED_CLOCK;
    }
}
