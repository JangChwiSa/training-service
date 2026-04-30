package com.jangchwisa.trainingservice.training.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record DocumentAnswerSummaryResponse(
        @Schema(description = "정답 수입니다.", example = "1")
        int correctCount,
        @Schema(description = "전체 문제 수입니다.", example = "2")
        int totalCount
) {
}
