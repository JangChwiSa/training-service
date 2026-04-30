package com.jangchwisa.trainingservice.common.logging;

import com.jangchwisa.trainingservice.common.security.TrustedUserHeaderProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    public static final String REQUEST_ID_HEADER = "X-Request-Id";
    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
    private static final Pattern SAFE_ID_PATTERN = Pattern.compile("[A-Za-z0-9._:-]{1,128}");
    private static final Pattern SESSION_ID_PATTERN = Pattern.compile("/sessions/(\\d+)(?:/|$)");

    private final TrustedUserHeaderProperties trustedUserHeaderProperties;

    public RequestLoggingFilter(TrustedUserHeaderProperties trustedUserHeaderProperties) {
        this.trustedUserHeaderProperties = trustedUserHeaderProperties;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        long startNanos = System.nanoTime();
        String path = request.getRequestURI();
        String correlationId = resolveCorrelationId(request);
        String traceId = resolveTraceId(request, correlationId);
        String userId = resolvePositiveNumber(request.getHeader(trustedUserHeaderProperties.trustedUserIdHeader()));
        String sessionId = resolveSessionId(path);
        response.setHeader(CORRELATION_ID_HEADER, correlationId);

        try {
            putMdc("correlationId", correlationId);
            putMdc("traceId", traceId);
            putMdc("userId", userId);
            putMdc("sessionId", sessionId);
            filterChain.doFilter(request, response);
            logRequest(request, response, path, startNanos, null);
        } catch (ServletException | IOException | RuntimeException exception) {
            logRequest(request, response, path, startNanos, exception);
            throw exception;
        } finally {
            MDC.remove("correlationId");
            MDC.remove("traceId");
            MDC.remove("userId");
            MDC.remove("sessionId");
        }
    }

    private String resolveCorrelationId(HttpServletRequest request) {
        String correlationId = firstSafeHeader(request, CORRELATION_ID_HEADER);
        if (correlationId != null) {
            return correlationId;
        }
        String requestId = firstSafeHeader(request, REQUEST_ID_HEADER);
        return requestId != null ? requestId : UUID.randomUUID().toString();
    }

    private String resolveTraceId(HttpServletRequest request, String correlationId) {
        String traceId = firstSafeHeader(request, TRACE_ID_HEADER);
        return traceId != null ? traceId : correlationId;
    }

    private String firstSafeHeader(HttpServletRequest request, String headerName) {
        String value = request.getHeader(headerName);
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String normalized = value.trim();
        return SAFE_ID_PATTERN.matcher(normalized).matches() ? normalized : null;
    }

    private String resolvePositiveNumber(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String normalized = value.trim();
        try {
            long parsed = Long.parseLong(normalized);
            return parsed > 0 ? normalized : null;
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String resolveSessionId(String path) {
        Matcher matcher = SESSION_ID_PATTERN.matcher(path);
        return matcher.find() ? matcher.group(1) : null;
    }

    private void putMdc(String key, String value) {
        if (StringUtils.hasText(value)) {
            MDC.put(key, value);
        }
    }

    private void logRequest(
            HttpServletRequest request,
            HttpServletResponse response,
            String path,
            long startNanos,
            Exception exception
    ) {
        long durationMs = (System.nanoTime() - startNanos) / 1_000_000;
        int status = exception == null ? response.getStatus() : HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        if (exception == null) {
            log.info(
                    "HTTP request completed. method={}, path={}, status={}, durationMs={}",
                    request.getMethod(),
                    path,
                    status,
                    durationMs
            );
            return;
        }

        log.warn(
                "HTTP request failed before response handling. method={}, path={}, status={}, durationMs={}, exception={}",
                request.getMethod(),
                path,
                status,
                durationMs,
                exception.getClass().getSimpleName()
        );
    }
}
