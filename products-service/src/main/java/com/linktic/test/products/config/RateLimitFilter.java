package com.linktic.test.products.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final Map<String, UserRequestInfo> requestCounts = new ConcurrentHashMap<>();
    private static final int MAX_REQUESTS_PER_MINUTE = 60;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String clientIp = request.getRemoteAddr();
        long currentTime = System.currentTimeMillis() / 60000; // current minute

        UserRequestInfo info = requestCounts.compute(clientIp, (key, val) -> {
            if (val == null || val.minute != currentTime) {
                return new UserRequestInfo(currentTime, new AtomicInteger(1));
            }
            val.count.incrementAndGet();
            return val;
        });

        if (info.count.get() > MAX_REQUESTS_PER_MINUTE) {
            response.setStatus(429);
            response.getWriter().write("{\"error\": \"Too many requests\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private static class UserRequestInfo {
        long minute;
        AtomicInteger count;

        UserRequestInfo(long minute, AtomicInteger count) {
            this.minute = minute;
            this.count = count;
        }
    }
}
