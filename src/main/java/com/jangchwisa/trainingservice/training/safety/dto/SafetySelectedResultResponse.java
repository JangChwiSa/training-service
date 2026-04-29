package com.jangchwisa.trainingservice.training.safety.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record SafetySelectedResultResponse(
        @Schema(description = "사용자가 선택한 선택지가 정답인지 여부입니다.", example = "true")
        boolean correct
) {
}
