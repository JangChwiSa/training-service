package com.jangchwisa.trainingservice.training.social.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record CompleteSocialSessionResponse(
        @Schema(description = "완료 처리된 사회성 훈련 세션 ID입니다.", example = "10")
        long sessionId,
        @Schema(description = "사회성 훈련 평가 점수입니다. 0부터 100 사이 값입니다.", example = "85")
        int score,
        @Schema(description = "훈련 결과 피드백 요약입니다.")
        String feedbackSummary,
        @Schema(description = "세션 완료 여부입니다.", example = "true")
        boolean completed
) {
}
