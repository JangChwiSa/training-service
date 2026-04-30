package com.jangchwisa.trainingservice.training.safety.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record SafetyActionDetailResponse(
        @Schema(description = "사용자가 선택한 장면 ID입니다.", example = "1")
        long sceneId,
        @Schema(description = "장면의 상황 설명입니다.", example = "작업장 바닥에 물이 흘러 있습니다.")
        String situationText,
        @Schema(description = "사용자가 선택한 선택지 문구입니다.", example = "관리자에게 알린다")
        String selectedChoice,
        @Schema(description = "선택이 정답인지 여부입니다.", example = "true")
        boolean correct
) {
}
