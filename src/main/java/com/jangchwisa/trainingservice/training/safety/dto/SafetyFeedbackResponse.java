package com.jangchwisa.trainingservice.training.safety.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record SafetyFeedbackResponse(
        @Schema(description = "안전 훈련 요약 피드백입니다.", example = "대부분의 위험 상황을 올바르게 판단했습니다.")
        String summary,
        @Schema(description = "안전 훈련 상세 피드백입니다.", example = "미끄러운 바닥을 발견했을 때 관리자에게 알린 선택은 적절합니다.")
        String detailText
) {
}
