package com.jangchwisa.trainingservice.external.openai.dto;

public record TrainingEvaluationFeedback(
        String summary,
        String detailText
) {

    public TrainingEvaluationFeedback {
        summary = normalizeRequired(summary, "Evaluation feedback summary is required.");
        detailText = normalize(detailText);
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
