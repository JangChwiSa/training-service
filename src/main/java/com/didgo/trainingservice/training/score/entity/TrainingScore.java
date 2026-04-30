package com.didgo.trainingservice.training.score.entity;

import com.didgo.trainingservice.common.exception.ErrorCode;
import com.didgo.trainingservice.common.exception.TrainingServiceException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public record TrainingScore(
        long sessionId,
        int score,
        String scoreType,
        Integer correctCount,
        Integer totalCount,
        BigDecimal accuracyRate,
        Integer wrongCount,
        Integer averageReactionMs,
        String rawMetricsJson,
        LocalDateTime createdAt
) {

    public TrainingScore {
        if (sessionId <= 0) {
            throw new TrainingServiceException(ErrorCode.VALIDATION_ERROR, "Session id must be positive.");
        }
        if (score < 0 || score > 100) {
            throw new TrainingServiceException(ErrorCode.VALIDATION_ERROR, "Training score must be between 0 and 100.");
        }
        scoreType = normalizeRequired(scoreType, "Score type is required.");
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
        if (averageReactionMs != null && averageReactionMs < 0) {
            throw new TrainingServiceException(ErrorCode.VALIDATION_ERROR, "Average reaction must not be negative.");
        }
        rawMetricsJson = normalize(rawMetricsJson);
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
