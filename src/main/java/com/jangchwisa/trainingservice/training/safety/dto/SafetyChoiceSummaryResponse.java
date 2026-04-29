package com.jangchwisa.trainingservice.training.safety.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record SafetyChoiceSummaryResponse(
        @Schema(description = "정답 선택 수입니다.", example = "7")
        int correctCount,
        @Schema(description = "전체 선택 수입니다.", example = "10")
        int totalCount
) {
}
