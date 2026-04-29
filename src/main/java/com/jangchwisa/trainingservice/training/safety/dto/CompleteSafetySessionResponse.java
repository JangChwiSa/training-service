package com.jangchwisa.trainingservice.training.safety.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record CompleteSafetySessionResponse(
        @Schema(description = "완료 처리된 안전 훈련 세션 ID입니다.", example = "20")
        long sessionId,
        @Schema(description = "안전 훈련 점수입니다. 0부터 100 사이 값입니다.", example = "70")
        int score,
        @Schema(description = "정답 선택 수입니다.", example = "7")
        int correctCount,
        @Schema(description = "전체 선택 수입니다.", example = "10")
        int totalCount,
        @Schema(description = "세션 완료 여부입니다.", example = "true")
        boolean completed
) {
}
