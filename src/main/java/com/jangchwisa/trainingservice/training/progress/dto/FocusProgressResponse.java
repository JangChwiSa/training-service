package com.jangchwisa.trainingservice.training.progress.dto;

import com.jangchwisa.trainingservice.training.session.entity.TrainingType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FocusProgressResponse(
        TrainingType trainingType,
        int currentLevel,
        int highestUnlockedLevel,
        Integer lastPlayedLevel,
        BigDecimal lastAccuracyRate,
        Integer lastAverageReactionMs,
        LocalDateTime updatedAt
) implements TrainingProgressResponse {
}
