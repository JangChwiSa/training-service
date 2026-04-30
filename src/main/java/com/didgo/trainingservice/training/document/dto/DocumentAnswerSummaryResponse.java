package com.didgo.trainingservice.training.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record DocumentAnswerSummaryResponse(
        @Schema(description = "?뺣떟 ?섏엯?덈떎.", example = "1")
        int correctCount,
        @Schema(description = "?꾩껜 臾몄젣 ?섏엯?덈떎.", example = "2")
        int totalCount
) {
}
