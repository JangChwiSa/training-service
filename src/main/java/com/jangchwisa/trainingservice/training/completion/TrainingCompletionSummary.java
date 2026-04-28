package com.jangchwisa.trainingservice.training.completion;

import java.math.BigDecimal;

public record TrainingCompletionSummary(
        Long scenarioId,
        String scenarioTitle,
        String category,
        String title,
        String summaryText,
        Integer correctCount,
        Integer totalCount,
        BigDecimal accuracyRate,
        Integer wrongCount,
        Integer playedLevel,
        Integer averageReactionMs
) {
}
