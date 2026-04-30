package com.jangchwisa.trainingservice.training.completion;

import java.math.BigDecimal;

public record TrainingCompletionScore(
        int score,
        String scoreType,
        Integer correctCount,
        Integer totalCount,
        BigDecimal accuracyRate,
        Integer wrongCount,
        Integer averageReactionMs,
        String rawMetricsJson
) {
}
