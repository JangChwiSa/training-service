package com.jangchwisa.trainingservice.training.summary.entity;

import com.jangchwisa.trainingservice.common.exception.ErrorCode;
import com.jangchwisa.trainingservice.common.exception.TrainingServiceException;
import com.jangchwisa.trainingservice.training.session.entity.TrainingType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public record TrainingSessionSummary(
        long sessionId,
        long userId,
        TrainingType trainingType,
        Long scenarioId,
        String scenarioTitle,
        String category,
        String title,
        Integer score,
        String summaryText,
        String feedbackSummary,
        Integer correctCount,
        Integer totalCount,
        BigDecimal accuracyRate,
        Integer wrongCount,
        Integer playedLevel,
        Integer averageReactionMs,
        LocalDateTime completedAt,
        LocalDateTime createdAt
) {

    public TrainingSessionSummary {
        if (sessionId <= 0) {
            throw new TrainingServiceException(ErrorCode.VALIDATION_ERROR, "Session id must be positive.");
        }
        if (userId <= 0) {
            throw new TrainingServiceException(ErrorCode.VALIDATION_ERROR, "User id must be positive.");
        }
        Objects.requireNonNull(trainingType, "trainingType must not be null");
        title = normalizeRequired(title, "Summary title is required.");
        scenarioTitle = normalize(scenarioTitle);
        category = normalize(category);
        summaryText = normalize(summaryText);
        feedbackSummary = normalize(feedbackSummary);
        if (score != null && (score < 0 || score > 100)) {
            throw new TrainingServiceException(ErrorCode.VALIDATION_ERROR, "Summary score must be between 0 and 100.");
        }
        if (correctCount != null && correctCount < 0) {
            throw new TrainingServiceException(ErrorCode.VALIDATION_ERROR, "Correct count must not be negative.");
        }
        if (totalCount != null && totalCount < 0) {
            throw new TrainingServiceException(ErrorCode.VALIDATION_ERROR, "Total count must not be negative.");
        }
        if (correctCount != null && totalCount != null && correctCount > totalCount) {
            throw new TrainingServiceException(ErrorCode.VALIDATION_ERROR, "Correct count must not exceed total count.");
        }
        if (accuracyRate != null && (accuracyRate.compareTo(BigDecimal.ZERO) < 0
                || accuracyRate.compareTo(BigDecimal.valueOf(100)) > 0)) {
            throw new TrainingServiceException(ErrorCode.VALIDATION_ERROR, "Accuracy rate must be between 0 and 100.");
        }
        if (wrongCount != null && wrongCount < 0) {
            throw new TrainingServiceException(ErrorCode.VALIDATION_ERROR, "Wrong count must not be negative.");
        }
        if (playedLevel != null && playedLevel < 1) {
            throw new TrainingServiceException(ErrorCode.VALIDATION_ERROR, "Played level must be positive.");
        }
        if (averageReactionMs != null && averageReactionMs < 0) {
            throw new TrainingServiceException(ErrorCode.VALIDATION_ERROR, "Average reaction must not be negative.");
        }
        Objects.requireNonNull(completedAt, "completedAt must not be null");
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
