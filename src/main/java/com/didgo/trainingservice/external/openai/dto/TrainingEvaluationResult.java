package com.didgo.trainingservice.external.openai.dto;

import java.util.Objects;

public record TrainingEvaluationResult(
        int score,
        String scoreType,
        TrainingEvaluationFeedback feedback,
        String rawMetricsJson
) {

    public TrainingEvaluationResult {
        if (score < 0 || score > 100) {
            throw new IllegalArgumentException("Evaluation score must be between 0 and 100.");
        }
        scoreType = normalizeRequired(scoreType, "Evaluation score type is required.");
        Objects.requireNonNull(feedback, "feedback must not be null");
        rawMetricsJson = normalize(rawMetricsJson);
    }

    private static String normalizeRequired(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
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
