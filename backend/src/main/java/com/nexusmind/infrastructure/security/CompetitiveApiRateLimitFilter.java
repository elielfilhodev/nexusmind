package com.nexusmind.infrastructure.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 11)
public class CompetitiveApiRateLimitFilter extends OncePerRequestFilter {

    private final RateLimitProperties rateLimitProperties;
    private final Cache<String, Window> windows;

    public CompetitiveApiRateLimitFilter(RateLimitProperties rateLimitProperties) {
        this.rateLimitProperties = rateLimitProperties;
        this.windows = Caffeine.newBuilder()
                .maximumSize(50_000)
                .expireAfterAccess(Duration.ofHours(2))
                .build();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path == null || !path.startsWith("/api/")) {
            return true;
        }
        return !(path.startsWith("/api/leaderboard")
                || path.startsWith("/api/players")
                || path.startsWith("/api/matches"));
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String key = clientKey(request);
        int perMinute = Math.max(10, rateLimitProperties.competitivePerMinuteOrDefault());
        long now = System.currentTimeMillis();
        Window w = windows.get(key, k -> new Window(now));
        synchronized (w) {
            if (now - w.windowStartMs >= 60_000) {
                w.windowStartMs = now;
                w.count.set(0);
            }
            if (w.count.incrementAndGet() > perMinute) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"RATE_LIMIT\",\"message\":\"Muitas requisições ao módulo competitivo. Aguarde.\"}");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private static String clientKey(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return Optional.ofNullable(request.getRemoteAddr()).orElse("unknown");
    }

    private static final class Window {
        private volatile long windowStartMs;
        private final AtomicInteger count = new AtomicInteger(0);

        private Window(long start) {
            this.windowStartMs = start;
        }
    }
}
