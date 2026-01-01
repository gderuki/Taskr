package com.gderuki.taskr.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Interceptor that logs incoming HTTP requests and their completion status.
 * Tracks request duration and logs query parameters and headers in debug mode.
 */
@Slf4j
@Component
public class LoggingInterceptor implements HandlerInterceptor {

    private static final String START_TIME_ATTRIBUTE = "requestStartTime";

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler) {

        log.info("Incoming request: {} {}", request.getMethod(), request.getRequestURI());

        logQueryParametersIfPresent(request);
        logRequestHeadersIfDebugEnabled(request);

        request.setAttribute(START_TIME_ATTRIBUTE, System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler,
            Exception ex) {

        final long duration = calculateRequestDuration(request);

        log.info("Completed request: {} {} - Status: {} - Duration: {}ms",
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                duration);

        if (ex != null) {
            log.error("Request failed: {} {} - Error: {}",
                    request.getMethod(),
                    request.getRequestURI(),
                    ex.getMessage(),
                    ex);
        }
    }

    private void logQueryParametersIfPresent(HttpServletRequest request) {
        Optional.ofNullable(request.getQueryString())
                .ifPresent(queryString -> log.debug("Query parameters: {}", queryString));
    }

    private void logRequestHeadersIfDebugEnabled(HttpServletRequest request) {
        if (!log.isDebugEnabled()) {
            return;
        }

        final String headers = Collections.list(request.getHeaderNames())
                .stream()
                .map(headerName -> String.format("%s: %s", headerName, request.getHeader(headerName)))
                .collect(Collectors.joining(", "));

        if (!headers.isBlank()) {
            log.debug("Request headers: {}", headers);
        }
    }

    private long calculateRequestDuration(HttpServletRequest request) {
        return Optional.ofNullable(request.getAttribute(START_TIME_ATTRIBUTE))
                .filter(Long.class::isInstance)
                .map(Long.class::cast)
                .map(startTime -> System.currentTimeMillis() - startTime)
                .orElse(0L);
    }
}
