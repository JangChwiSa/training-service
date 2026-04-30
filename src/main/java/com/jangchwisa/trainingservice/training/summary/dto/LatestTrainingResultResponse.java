package com.jangchwisa.trainingservice.training.summary.dto;

import com.jangchwisa.trainingservice.training.session.entity.TrainingType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record LatestTrainingResultResponse(
        @Schema(description = "완료된 훈련 세션 ID입니다.", example = "10")
        long sessionId,
        @Schema(description = "훈련 유형입니다.", example = "SOCIAL")
        TrainingType trainingType,
        @Schema(description = "훈련 점수입니다. 0부터 100 사이 값입니다.", example = "85")
        int score,
        @Schema(description = "점수 산정 방식입니다.", example = "AI_EVALUATION")
        String scoreType,
        @Schema(description = "훈련 완료 시각입니다.", example = "2026-04-27T10:30:00")
        LocalDateTime completedAt
) {
}
