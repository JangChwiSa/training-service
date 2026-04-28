package com.jangchwisa.trainingservice.event.outbox;

import com.jangchwisa.trainingservice.common.exception.ErrorCode;
import com.jangchwisa.trainingservice.common.exception.TrainingServiceException;
import com.jangchwisa.trainingservice.training.session.entity.TrainingType;
import java.time.LocalDateTime;
import java.util.Objects;

public record OutboxEvent(
        String eventId,
        String eventType,
        String aggregateType,
        long aggregateId,
        long sessionId,
        long userId,
        TrainingType trainingType,
        String payloadJson,
        String status,
        int retryCount,
        int maxRetryCount,
        LocalDateTime nextRetryAt,
        LocalDateTime publishedAt,
        String lastErrorMessage,
        String dlqReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public OutboxEvent {
        eventId = normalizeRequired(eventId, "Event id is required.");
        eventType = normalizeRequired(eventType, "Event type is required.");
        aggregateType = normalizeRequired(aggregateType, "Aggregate type is required.");
        if (aggregateId <= 0 || sessionId <= 0 || userId <= 0) {
            throw new TrainingServiceException(ErrorCode.VALIDATION_ERROR, "Outbox aggregate ids must be positive.");
        }
        Objects.requireNonNull(trainingType, "trainingType must not be null");
        payloadJson = normalizeRequired(payloadJson, "Outbox payload is required.");
        status = normalizeRequired(status, "Outbox status is required.");
        if (retryCount < 0 || maxRetryCount < 0 || retryCount > maxRetryCount) {
            throw new TrainingServiceException(ErrorCode.VALIDATION_ERROR, "Outbox retry counts are invalid.");
        }
        lastErrorMessage = normalize(lastErrorMessage);
        dlqReason = normalize(dlqReason);
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        Objects.requireNonNull(updatedAt, "updatedAt must not be null");
    }

    private static String normalizeRequired(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new TrainingServiceException(ErrorCode.VALIDATION_ERROR, message);
        }
        return value.trim();
    }

    private static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
