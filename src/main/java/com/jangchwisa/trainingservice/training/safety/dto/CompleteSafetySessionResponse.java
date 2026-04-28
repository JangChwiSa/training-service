package com.jangchwisa.trainingservice.training.safety.dto;

public record CompleteSafetySessionResponse(
        long sessionId,
        int score,
        int correctCount,
        int totalCount,
        boolean completed
) {
}
