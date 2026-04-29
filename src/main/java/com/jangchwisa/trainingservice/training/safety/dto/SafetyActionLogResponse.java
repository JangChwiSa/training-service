package com.jangchwisa.trainingservice.training.safety.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record SafetyActionLogResponse(
        @Schema(description = "사용자가 선택을 수행한 장면 ID입니다.", example = "1")
        long sceneId,
        @Schema(description = "사용자가 선택한 선택지 ID입니다.", example = "1")
        long choiceId,
        @Schema(description = "해당 선택이 정답인지 여부입니다.", example = "true")
        boolean correct
) {
}
