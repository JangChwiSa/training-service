package com.didgo.trainingservice.external.openai.dto;

public record TrainingEvaluationScoreModel(
        int score,
        String scoreType,
        String rawMetricsJson
) {
}
