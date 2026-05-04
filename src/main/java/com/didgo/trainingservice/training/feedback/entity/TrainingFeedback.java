package com.didgo.trainingservice.training.feedback.entity;

import com.didgo.trainingservice.common.exception.ErrorCode;
import com.didgo.trainingservice.common.exception.TrainingServiceException;
import java.time.LocalDateTime;
import java.util.Objects;

public record TrainingFeedback(
        long sessionId,
        String feedbackType,
        String feedbackSource,
        String summary,
        String detailText,
        LocalDateTime createdAt
) {

    public TrainingFeedback {
        if (sessionId <= 0) {
            throw new TrainingServiceException(ErrorCode.VALIDATION_ERROR, "Session id must be positive.");
        }
        feedbackType = normalizeRequired(feedbackType, "Feedback type is required.");
        feedbackSource = normalizeRequired(feedbackSource, "Feedback source is required.");
        summary = normalizeRequired(summary, "Feedback summary is required.");
        detailText = normalize(detailText);
        Objects.requireNonNull(createdAt, "createdAt must not be null");
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
