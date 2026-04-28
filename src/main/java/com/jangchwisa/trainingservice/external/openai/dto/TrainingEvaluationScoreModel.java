package com.jangchwisa.trainingservice.external.openai.dto;

public record TrainingEvaluationScoreModel(
        int score,
        String scoreType,
        String rawMetricsJson
) {
}
