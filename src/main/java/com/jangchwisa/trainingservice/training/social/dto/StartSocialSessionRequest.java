package com.jangchwisa.trainingservice.training.social.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record StartSocialSessionRequest(
        @Schema(description = "사회성 훈련 직무 유형입니다. OFFICE 또는 LABOR를 입력합니다.", example = "OFFICE")
        @NotBlank String jobType,
        @Schema(description = "시작할 사회성 시나리오 ID입니다. 시나리오 목록 조회 API에서 받은 scenarioId를 넣습니다.", example = "1")
        @Positive long scenarioId
) {
}
