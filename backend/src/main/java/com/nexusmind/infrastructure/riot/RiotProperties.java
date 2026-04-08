package com.nexusmind.infrastructure.riot;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.riot")
public record RiotProperties(
        String apiKey,
        Duration connectTimeout,
        Duration readTimeout,
        int maxRetries,
        /** Espaço mínimo entre chamadas outbound (protege quota e reduz 429). */
        long minIntervalMillis,
        /** TTL padrão de caches @Cacheable (segundos). */
        int defaultCacheTtlSeconds
) {
    public RiotProperties {
        if (connectTimeout == null) {
            connectTimeout = Duration.ofSeconds(5);
        }
        if (readTimeout == null) {
            readTimeout = Duration.ofSeconds(25);
        }
        if (maxRetries < 0) {
            maxRetries = 2;
        }
        if (minIntervalMillis < 0) {
            minIntervalMillis = 35;
        }
        if (defaultCacheTtlSeconds <= 0) {
            defaultCacheTtlSeconds = 120;
        }
    }
}
