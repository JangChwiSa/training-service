package com.jangchwisa.trainingservice.training.progress.dto;

import com.jangchwisa.trainingservice.training.session.entity.TrainingType;
import java.time.LocalDateTime;

public record SocialProgressResponse(
        TrainingType trainingType,
        Long recentSessionId,
        Integer recentScore,
        String recentFeedbackSummary,
        int completedCount,
        LocalDateTime lastCompletedAt
) implements TrainingProgressResponse {
}
