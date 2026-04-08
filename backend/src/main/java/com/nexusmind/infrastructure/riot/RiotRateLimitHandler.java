package com.nexusmind.infrastructure.riot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;

/**
 * Controle simples de cadência + backoff em 429 (Retry-After quando presente).
 */
@Component
public class RiotRateLimitHandler {

    private static final Logger log = LoggerFactory.getLogger(RiotRateLimitHandler.class);

    private final RiotProperties props;
    private final Object lock = new Object();
    private volatile long lastOutboundMs = 0L;

    public RiotRateLimitHandler(RiotProperties props) {
        this.props = props;
    }

    public void acquireOutboundSlot() {
        long min = Math.max(0L, props.minIntervalMillis());
        if (min == 0) {
            return;
        }
        synchronized (lock) {
            long now = System.currentTimeMillis();
            long elapsed = now - lastOutboundMs;
            long wait = min - elapsed;
            if (wait > 0) {
                sleepQuiet(wait);
            }
            lastOutboundMs = System.currentTimeMillis();
        }
    }

    public void backoffAfter429(RestClientResponseException ex) {
        int seconds = 2;
        String ra = ex.getResponseHeaders() != null ? ex.getResponseHeaders().getFirst("Retry-After") : null;
        if (ra != null) {
            try {
                seconds = Math.min(120, Math.max(1, Integer.parseInt(ra.trim())));
            } catch (NumberFormatException ignored) {
                /* usa default */
            }
        }
        log.warn("Riot API 429 — aguardando {} s antes de retentar", seconds);
        sleepQuiet(seconds * 1000L);
    }

    private static void sleepQuiet(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
