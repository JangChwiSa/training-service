package com.jangchwisa.trainingservice.training.summary.dto;

import com.jangchwisa.trainingservice.training.safety.entity.SafetyCategory;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TrainingSessionListItemResponse(
        long sessionId,
        Long scenarioId,
        String scenarioTitle,
        SafetyCategory category,
        Integer score,
        String feedbackSummary,
        Integer correctCount,
        Integer totalCount,
        Integer playedLevel,
        BigDecimal accuracyRate,
        Integer wrongCount,
        Integer averageReactionMs,
        LocalDateTime completedAt
) {
}
