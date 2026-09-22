package com.computaquest.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.time.Instant;

@Slf4j
@Component
@Order(1)
public class RateLimitFilter implements Filter {

    private static final int MAX_REQUESTS_PER_MINUTE = 20;
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<Instant>> requestCounts = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String path = request.getRequestURI();
        if (!path.startsWith("/api/auth/")) {
            chain.doFilter(req, res);
            return;
        }

        String clientIp = getClientIp(request);
        Instant now = Instant.now();
        Instant oneMinuteAgo = now.minusSeconds(60);

        CopyOnWriteArrayList<Instant> timestamps = requestCounts.computeIfAbsent(clientIp, k -> new CopyOnWriteArrayList<>());
        timestamps.removeIf(t -> t.isBefore(oneMinuteAgo));

        if (timestamps.size() >= MAX_REQUESTS_PER_MINUTE) {
            log.warn("Rate limit exceeded for IP: {} on path: {}", clientIp, path);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Demasiadas peticiones. Intenta de nuevo en un minuto.\"}");
            return;
        }

        timestamps.add(now);
        chain.doFilter(req, res);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
