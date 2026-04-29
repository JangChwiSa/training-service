package com.jangchwisa.trainingservice.training.progress.dto;

import com.jangchwisa.trainingservice.training.session.entity.TrainingType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record DocumentProgressResponse(
        @Schema(description = "훈련 유형입니다.", example = "DOCUMENT")
        TrainingType trainingType,
        @Schema(description = "최근 완료한 문서 이해 훈련 세션 ID입니다. 기록이 없으면 null입니다.", example = "30")
        Long recentSessionId,
        @Schema(description = "누적 또는 최근 문서 이해 훈련 정답 수입니다.", example = "8")
        int correctCount,
        @Schema(description = "누적 또는 최근 문서 이해 훈련 전체 문제 수입니다.", example = "10")
        int totalCount,
        @Schema(description = "최근 문서 이해 훈련 점수입니다. 기록이 없으면 null입니다.", example = "80")
        Integer recentScore,
        @Schema(description = "완료한 문서 이해 훈련 횟수입니다.", example = "4")
        int completedCount,
        @Schema(description = "마지막 문서 이해 훈련 완료 시각입니다. 기록이 없으면 null입니다.", example = "2026-04-27T10:40:00")
        LocalDateTime lastCompletedAt
) implements TrainingProgressResponse {
}
