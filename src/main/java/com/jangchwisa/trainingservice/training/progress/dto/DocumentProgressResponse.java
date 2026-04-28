package com.jangchwisa.trainingservice.training.progress.dto;

import com.jangchwisa.trainingservice.training.session.entity.TrainingType;
import java.time.LocalDateTime;

public record DocumentProgressResponse(
        TrainingType trainingType,
        Long recentSessionId,
        int correctCount,
        int totalCount,
        Integer recentScore,
        int completedCount,
        LocalDateTime lastCompletedAt
) implements TrainingProgressResponse {
}
