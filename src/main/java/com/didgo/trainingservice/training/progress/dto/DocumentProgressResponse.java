package com.didgo.trainingservice.training.progress.dto;

import com.didgo.trainingservice.training.session.entity.TrainingType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DocumentProgressResponse(
        @Schema(description = "Training type.", example = "DOCUMENT")
        TrainingType trainingType,
        @Schema(description = "Most recent completed document training session id. Null when no completion exists.", example = "30")
        Long recentSessionId,
        @Schema(description = "Most recent document training correct answer count.", example = "4")
        int correctCount,
        @Schema(description = "Most recent document training total question count.", example = "5")
        int totalCount,
        @Schema(description = "Most recent document training score. Null when no completion exists.", example = "80")
        Integer recentScore,
        @Schema(description = "Current document training level reached by the user.", example = "2")
        int currentLevel,
        @Schema(description = "Highest document training level the user can currently play.", example = "3")
        int highestUnlockedLevel,
        @Schema(description = "Most recent document training level played by the user. Null when no completion exists.", example = "2")
        Integer lastPlayedLevel,
        @Schema(description = "Most recent document training accuracy rate in percent. Null when no completion exists.", example = "80.0")
        BigDecimal lastAccuracyRate,
        @Schema(description = "Completed document training count.", example = "4")
        int completedCount,
        @Schema(description = "Most recent document training completion time. Null when no completion exists.", example = "2026-04-27T10:40:00")
        LocalDateTime lastCompletedAt
) implements TrainingProgressResponse {
}
