package com.jangchwisa.trainingservice.training.summary.dto;

import com.jangchwisa.trainingservice.training.session.entity.TrainingType;
import java.time.LocalDateTime;

public record LatestTrainingResultResponse(
        long sessionId,
        TrainingType trainingType,
        int score,
        String scoreType,
        LocalDateTime completedAt
) {
}
