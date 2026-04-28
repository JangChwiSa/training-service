package com.jangchwisa.trainingservice.training.evaluation;

import com.jangchwisa.trainingservice.external.openai.dto.TrainingEvaluationLog;
import com.jangchwisa.trainingservice.training.session.entity.TrainingType;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record TrainingEvaluationCommand(
        TrainingType trainingType,
        String scenarioTitle,
        List<TrainingEvaluationLog> logs,
        Map<String, Object> metrics,
        int deterministicScore,
        String deterministicScoreType,
        String fallbackSummary,
        String fallbackDetailText,
        boolean adaptiveFeedback
) {

    public TrainingEvaluationCommand {
        Objects.requireNonNull(trainingType, "trainingType must not be null");
        if (deterministicScore < 0 || deterministicScore > 100) {
            throw new IllegalArgumentException("Deterministic score must be between 0 and 100.");
        }
        deterministicScoreType = normalizeRequired(deterministicScoreType, "Deterministic score type is required.");
        fallbackSummary = normalizeRequired(fallbackSummary, "Fallback summary is required.");
        fallbackDetailText = normalize(fallbackDetailText);
        scenarioTitle = normalize(scenarioTitle);
        logs = List.copyOf(logs == null ? List.of() : logs);
        metrics = Map.copyOf(metrics == null ? Map.of() : metrics);
        rejectUserIdentity(metrics);
    }

    boolean shouldUseAiEvaluation() {
        return trainingType == TrainingType.SOCIAL || adaptiveFeedback;
    }

    private static void rejectUserIdentity(Map<String, Object> metrics) {
        for (String key : metrics.keySet()) {
            if ("userId".equalsIgnoreCase(key) || "user_id".equalsIgnoreCase(key)) {
                throw new IllegalArgumentException("Training evaluation command must not include user identity.");
            }
        }
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
