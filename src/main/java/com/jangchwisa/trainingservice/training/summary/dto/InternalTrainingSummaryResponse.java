package com.jangchwisa.trainingservice.training.summary.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record InternalTrainingSummaryResponse(
        @Schema(description = "사용자의 최근 사회성 훈련 점수입니다. 기록이 없으면 null입니다.", example = "85")
        Integer socialRecentScore,
        @Schema(description = "사용자의 안전 훈련 정답 수입니다.", example = "7")
        int safetyCorrectCount,
        @Schema(description = "사용자의 안전 훈련 전체 선택 수입니다.", example = "10")
        int safetyTotalCount,
        @Schema(description = "사용자의 문서 이해 훈련 정답 수입니다.", example = "8")
        int documentCorrectCount,
        @Schema(description = "사용자의 문서 이해 훈련 전체 문제 수입니다.", example = "10")
        int documentTotalCount,
        @Schema(description = "사용자의 현재 집중력 훈련 단계입니다.", example = "3")
        int focusCurrentLevel
) {
}
