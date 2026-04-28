package com.jangchwisa.trainingservice.training.progress.dto;

import com.jangchwisa.trainingservice.training.session.entity.TrainingType;
import java.time.LocalDateTime;

public record SafetyProgressResponse(
        TrainingType trainingType,
        Long recentSessionId,
        int correctCount,
        int totalCount,
        int completedCount,
        LocalDateTime lastCompletedAt
) implements TrainingProgressResponse {
}
