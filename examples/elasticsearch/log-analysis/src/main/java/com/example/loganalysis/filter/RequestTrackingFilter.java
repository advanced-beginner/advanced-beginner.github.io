package com.example.loganalysis.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class RequestTrackingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestTrackingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        try {
            String requestId = UUID.randomUUID().toString().substring(0, 8);
            String userId = extractUserId(request);

            MDC.put("requestId", requestId);
            MDC.put("userId", userId);
            MDC.put("httpMethod", request.getMethod());
            MDC.put("httpPath", request.getRequestURI());

            long startTime = System.currentTimeMillis();

            chain.doFilter(request, response);

            long duration = System.currentTimeMillis() - startTime;
            MDC.put("durationMs", String.valueOf(duration));
            MDC.put("httpStatus", String.valueOf(response.getStatus()));

            log.info("Request completed");
        } finally {
            MDC.clear();
        }
    }

    private String extractUserId(HttpServletRequest request) {
        String userId = request.getHeader("X-User-Id");
        return userId != null ? userId : "anonymous";
    }
}
