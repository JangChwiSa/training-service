package com.didgo.trainingservice.external.openai.dto;

public record TrainingEvaluationLog(
        String role,
        String content
) {

    public TrainingEvaluationLog {
        role = normalizeRequired(role, "Evaluation log role is required.");
        content = normalizeRequired(content, "Evaluation log content is required.");
    }

    private static String normalizeRequired(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }
}
