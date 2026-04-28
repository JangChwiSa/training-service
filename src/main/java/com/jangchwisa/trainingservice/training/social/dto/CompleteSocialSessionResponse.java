package com.jangchwisa.trainingservice.training.social.dto;

public record CompleteSocialSessionResponse(
        long sessionId,
        int score,
        String feedbackSummary,
        boolean completed
) {
}
