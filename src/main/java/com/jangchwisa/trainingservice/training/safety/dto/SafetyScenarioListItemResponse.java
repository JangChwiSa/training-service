package com.jangchwisa.trainingservice.training.safety.dto;

import com.jangchwisa.trainingservice.training.safety.entity.SafetyCategory;
import io.swagger.v3.oas.annotations.media.Schema;

public record SafetyScenarioListItemResponse(
        @Schema(description = "안전 시나리오 ID입니다. 세션 시작 요청의 scenarioId로 사용합니다.", example = "1")
        long scenarioId,
        @Schema(description = "안전 훈련 카테고리입니다.", example = "COMMUTE_SAFETY")
        SafetyCategory category,
        @Schema(description = "안전 시나리오 제목입니다.")
        String title,
        @Schema(description = "안전 시나리오 설명입니다.")
        String description
) {
}
