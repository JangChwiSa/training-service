package com.didgo.trainingservice.training.progress.entity;

import com.didgo.trainingservice.common.exception.ErrorCode;
import com.didgo.trainingservice.common.exception.TrainingServiceException;
import com.didgo.trainingservice.training.session.entity.TrainingType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public record TrainingProgressCompletion(
        long userId,
        long sessionId,
        TrainingType trainingType,
        Integer score,
        String feedbackSummary,
        Integer correctCount,
        Integer totalCount,
        Integer currentLevel,
        Integer highestUnlockedLevel,
        Integer playedLevel,
        BigDecimal accuracyRate,
        Integer averageReactionMs,
        LocalDateTime completedAt
) {

    public TrainingProgressCompletion {
        if (userId <= 0 || sessionId <= 0) {
            throw new TrainingServiceException(ErrorCode.VALIDATION_ERROR, "Progress ids must be positive.");
        }
        Objects.requireNonNull(trainingType, "trainingType must not be null");
        if (score != null && (score < 0 || score > 100)) {
            throw new TrainingServiceException(ErrorCode.VALIDATION_ERROR, "Progress score must be between 0 and 100.");
        }
        feedbackSummary = normalize(feedbackSummary);
        if (correctCount != null && correctCount < 0) {
            throw new TrainingServiceException(ErrorCode.VALIDATION_ERROR, "Correct count must not be negative.");
        }
        if (totalCount != null && totalCount < 0) {
            throw new TrainingServiceException(ErrorCode.VALIDATION_ERROR, "Total count must not be negative.");
        }
        if (correctCount != null && totalCount != null && correctCount > totalCount) {
            throw new TrainingServiceException(ErrorCode.VALIDATION_ERROR, "Correct count must not exceed total count.");
        }
        if (currentLevel != null && currentLevel < 1) {
            throw new TrainingServiceException(ErrorCode.VALIDATION_ERROR, "Current level must be positive.");
        }
        if (highestUnlockedLevel != null && highestUnlockedLevel < 1) {
            throw new TrainingServiceException(ErrorCode.VALIDATION_ERROR, "Highest unlocked level must be positive.");
        }
        if (playedLevel != null && playedLevel < 1) {
            throw new TrainingServiceException(ErrorCode.VALIDATION_ERROR, "Played level must be positive.");
        }
        if (accuracyRate != null && (accuracyRate.compareTo(BigDecimal.ZERO) < 0
                || accuracyRate.compareTo(BigDecimal.valueOf(100)) > 0)) {
            throw new TrainingServiceException(ErrorCode.VALIDATION_ERROR, "Accuracy rate must be between 0 and 100.");
        }
        if (averageReactionMs != null && averageReactionMs < 0) {
            throw new TrainingServiceException(ErrorCode.VALIDATION_ERROR, "Average reaction must not be negative.");
        }
        Objects.requireNonNull(completedAt, "completedAt must not be null");
    }

    private static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
