package com.jangchwisa.trainingservice.training.safety.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record SafetySessionDetailResponse(
        @Schema(description = "조회한 안전 훈련 세션 ID입니다.", example = "20")
        long sessionId,
        @Schema(description = "안전 훈련 점수입니다.", example = "70")
        int score,
        @Schema(description = "정답 선택 수와 전체 선택 수 요약입니다.")
        SafetyChoiceSummaryResponse choiceSummary,
        @Schema(description = "상황 설명과 선택지 문구를 포함한 선택 이력입니다.")
        List<SafetyActionDetailResponse> actions,
        @Schema(description = "안전 훈련 완료 후 저장된 피드백입니다.")
        SafetyFeedbackResponse feedback
) {
}
