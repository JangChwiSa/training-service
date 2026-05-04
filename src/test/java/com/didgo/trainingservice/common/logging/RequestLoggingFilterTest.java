package com.didgo.trainingservice.common.logging;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.didgo.trainingservice.common.security.TrustedUserHeaderProperties;
import jakarta.servlet.FilterChain;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RequestLoggingFilterTest {

    private final TrustedUserHeaderProperties properties = new TrustedUserHeaderProperties();
    private final RequestLoggingFilter filter = new RequestLoggingFilter(properties);

    @Test
    void acceptsCorrelationAndTraceHeadersAndAddsOperationalContext() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/trainings/social/sessions/123/complete");
        request.addHeader(RequestLoggingFilter.CORRELATION_ID_HEADER, "corr-001");
        request.addHeader(RequestLoggingFilter.TRACE_ID_HEADER, "trace-001");
        request.addHeader("X-User-Id", "7");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> correlationId = new AtomicReference<>();
        AtomicReference<String> traceId = new AtomicReference<>();
        AtomicReference<String> userId = new AtomicReference<>();
        AtomicReference<String> sessionId = new AtomicReference<>();
        FilterChain chain = (servletRequest, servletResponse) -> {
            correlationId.set(MDC.get("correlationId"));
            traceId.set(MDC.get("traceId"));
            userId.set(MDC.get("userId"));
            sessionId.set(MDC.get("sessionId"));
            response.setStatus(204);
        };

        filter.doFilter(request, response, chain);

        assertThat(response.getHeader(RequestLoggingFilter.CORRELATION_ID_HEADER)).isEqualTo("corr-001");
        assertThat(correlationId).hasValue("corr-001");
        assertThat(traceId).hasValue("trace-001");
        assertThat(userId).hasValue("7");
        assertThat(sessionId).hasValue("123");
        assertThat(MDC.getCopyOfContextMap()).isNullOrEmpty();
    }

    @Test
    void generatesSafeCorrelationIdWhenIncomingHeaderIsUnsafe() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");
        request.addHeader(RequestLoggingFilter.CORRELATION_ID_HEADER, "bad value with spaces");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (servletRequest, servletResponse) -> response.setStatus(200));

        assertThat(response.getHeader(RequestLoggingFilter.CORRELATION_ID_HEADER))
                .isNotEqualTo("bad value with spaces")
                .matches("[0-9a-f-]{36}");
    }

    @Test
    void requestLogDoesNotIncludeQueryString() throws Exception {
        Logger logger = (Logger) LoggerFactory.getLogger(RequestLoggingFilter.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/trainings/social/sessions/42/detail");
            request.setQueryString("openai_api_key=secret-value&userId=99");
            request.addHeader("X-User-Id", "7");
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilter(request, response, (servletRequest, servletResponse) -> response.setStatus(200));

            assertThat(appender.list)
                    .anySatisfy(event -> {
                        assertThat(event.getLevel()).isEqualTo(Level.INFO);
                        assertThat(event.getFormattedMessage()).contains("path=/api/trainings/social/sessions/42/detail");
                        assertThat(event.getFormattedMessage()).doesNotContain("openai_api_key", "secret-value", "userId=99");
                    });
        } finally {
            logger.detachAppender(appender);
        }
    }
}
