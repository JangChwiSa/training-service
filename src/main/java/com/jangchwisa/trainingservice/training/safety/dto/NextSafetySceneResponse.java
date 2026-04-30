package com.jangchwisa.trainingservice.training.safety.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record NextSafetySceneResponse(
        @Schema(description = "방금 제출한 선택의 정답 여부입니다.")
        SafetySelectedResultResponse selectedResult,
        @Schema(description = "다음에 진행할 장면입니다. 마지막 장면이면 null일 수 있습니다.")
        SafetySceneResponse nextScene
) {
}
