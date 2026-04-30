package com.jangchwisa.trainingservice.training.completion;

import java.time.LocalDateTime;

public record TrainingCompletionResult(
        long sessionId,
        int score,
        String feedbackSummary,
        boolean completed,
        LocalDateTime completedAt
) {
}
