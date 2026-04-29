package com.jangchwisa.trainingservice.training.safety.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;

public record StartSafetySessionRequest(
        @Schema(description = "시작할 안전 시나리오 ID입니다. 안전 시나리오 목록 조회 API에서 받은 scenarioId를 넣습니다.", example = "1")
        @Positive long scenarioId
) {
}
