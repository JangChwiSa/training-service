package com.jangchwisa.trainingservice.external.openai.dto;

import com.jangchwisa.trainingservice.training.session.entity.TrainingType;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record TrainingEvaluationRequest(
        TrainingType trainingType,
        String scenarioTitle,
        List<TrainingEvaluationLog> logs,
        Map<String, Object> metrics
) {

    public TrainingEvaluationRequest {
        Objects.requireNonNull(trainingType, "trainingType must not be null");
        scenarioTitle = normalize(scenarioTitle);
        logs = List.copyOf(logs == null ? List.of() : logs);
        metrics = Map.copyOf(metrics == null ? Map.of() : metrics);
        rejectUserIdentity(metrics);
    }

    private static void rejectUserIdentity(Map<String, Object> metrics) {
        for (String key : metrics.keySet()) {
            if ("userId".equalsIgnoreCase(key) || "user_id".equalsIgnoreCase(key)) {
                throw new IllegalArgumentException("OpenAI evaluation request must not include user identity.");
            }
        }
    }

    private static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
