package com.jangchwisa.trainingservice.event.publisher;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jangchwisa.trainingservice.event.outbox.OutboxEvent;
import com.jangchwisa.trainingservice.event.outbox.OutboxEventRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class OutboxPublisherService {

    private static final String DLQ_REASON_INVALID_PAYLOAD = "INVALID_PAYLOAD";
    private static final String DLQ_REASON_MAX_RETRY_EXCEEDED = "MAX_RETRY_EXCEEDED";

    private final OutboxEventRepository outboxEventRepository;
    private final EventBrokerPublisher eventBrokerPublisher;
    private final ObjectMapper objectMapper;
    private final OutboxPublisherProperties properties;
    private final Clock clock;

    public OutboxPublisherService(
            OutboxEventRepository outboxEventRepository,
            EventBrokerPublisher eventBrokerPublisher,
            ObjectMapper objectMapper,
            OutboxPublisherProperties properties,
            Clock clock
    ) {
        this.outboxEventRepository = outboxEventRepository;
        this.eventBrokerPublisher = eventBrokerPublisher;
        this.objectMapper = objectMapper;
        this.properties = properties;
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "${training.outbox.publisher.fixed-delay-ms:5000}")
    public void publishScheduled() {
        if (!properties.enabled()) {
            return;
        }
        publishDueEvents();
    }

    public int publishDueEvents() {
        LocalDateTime now = now();
        int publishedCount = 0;
        for (OutboxEvent event : outboxEventRepository.findPublishableEvents(now, properties.batchSize())) {
            if (publishOne(event)) {
                publishedCount++;
            }
        }
        return publishedCount;
    }

    private boolean publishOne(OutboxEvent event) {
        LocalDateTime now = now();
        try {
            validatePayload(event);
            eventBrokerPublisher.publish(event);
            outboxEventRepository.markPublished(event.eventId(), now);
            return true;
        } catch (InvalidOutboxPayloadException exception) {
            outboxEventRepository.markDlq(event.eventId(), DLQ_REASON_INVALID_PAYLOAD, exception.getMessage(), now);
            return false;
        } catch (RuntimeException exception) {
            handlePublishFailure(event, exception, now);
            return false;
        }
    }

    private void validatePayload(OutboxEvent event) {
        try {
            JsonNode payload = objectMapper.readTree(event.payloadJson());
            requireText(payload, "eventId");
            requireText(payload, "eventType");
            requireNumber(payload, "userId");
            requireNumber(payload, "sessionId");
            requireText(payload, "trainingType");
            requireNumber(payload, "score");
            requireText(payload, "scoreType");
            requireText(payload, "completedAt");
            if (!event.eventId().equals(payload.get("eventId").asText())) {
                throw new InvalidOutboxPayloadException("Payload eventId does not match outbox event id.");
            }
        } catch (InvalidOutboxPayloadException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new InvalidOutboxPayloadException("Payload JSON is invalid.", exception);
        }
    }

    private void handlePublishFailure(OutboxEvent event, RuntimeException exception, LocalDateTime now) {
        int nextRetryCount = event.retryCount() + 1;
        String message = normalizeErrorMessage(exception);
        if (nextRetryCount >= event.maxRetryCount()) {
            outboxEventRepository.markDlq(event.eventId(), DLQ_REASON_MAX_RETRY_EXCEEDED, message, now);
            return;
        }

        outboxEventRepository.markRetry(
                event.eventId(),
                nextRetryCount,
                now.plusSeconds(retryBackoffSeconds(nextRetryCount)),
                message,
                now
        );
    }

    private long retryBackoffSeconds(int retryCount) {
        long multiplier = 1L << Math.min(retryCount - 1, 10);
        long seconds = properties.retryBackoffBaseSeconds() * multiplier;
        return Math.min(seconds, properties.retryBackoffMaxSeconds());
    }

    private String normalizeErrorMessage(RuntimeException exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            return exception.getClass().getSimpleName();
        }
        return message.length() > 1000 ? message.substring(0, 1000) : message;
    }

    private void requireText(JsonNode payload, String fieldName) {
        JsonNode value = payload.get(fieldName);
        if (value == null || !value.isTextual() || value.asText().isBlank()) {
            throw new InvalidOutboxPayloadException("Payload field is required: " + fieldName);
        }
    }

    private void requireNumber(JsonNode payload, String fieldName) {
        JsonNode value = payload.get(fieldName);
        if (value == null || !value.isNumber()) {
            throw new InvalidOutboxPayloadException("Payload numeric field is required: " + fieldName);
        }
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }

    private static class InvalidOutboxPayloadException extends RuntimeException {

        InvalidOutboxPayloadException(String message) {
            super(message);
        }

        InvalidOutboxPayloadException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
