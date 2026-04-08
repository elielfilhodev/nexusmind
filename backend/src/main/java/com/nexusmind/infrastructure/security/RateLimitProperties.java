package com.nexusmind.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.rate-limit")
public record RateLimitProperties(int perMinute, Integer competitivePerMinute) {

    public int competitivePerMinuteOrDefault() {
        if (competitivePerMinute == null || competitivePerMinute <= 0) {
            return 60;
        }
        return competitivePerMinute;
    }
}
