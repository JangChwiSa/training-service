package com.jangchwisa.trainingservice.training.progress.dto;

import com.jangchwisa.trainingservice.training.session.entity.TrainingType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record SocialProgressResponse(
        @Schema(description = "훈련 유형입니다.", example = "SOCIAL")
        TrainingType trainingType,
        @Schema(description = "최근 완료한 사회성 훈련 세션 ID입니다. 기록이 없으면 null입니다.", example = "10")
        Long recentSessionId,
        @Schema(description = "최근 사회성 훈련 점수입니다. 기록이 없으면 null입니다.", example = "85")
        Integer recentScore,
        @Schema(description = "최근 사회성 훈련 피드백 요약입니다. 기록이 없으면 null입니다.")
        String recentFeedbackSummary,
        @Schema(description = "완료한 사회성 훈련 횟수입니다.", example = "3")
        int completedCount,
        @Schema(description = "마지막 사회성 훈련 완료 시각입니다. 기록이 없으면 null입니다.", example = "2026-04-27T10:00:00")
        LocalDateTime lastCompletedAt
) implements TrainingProgressResponse {
}
