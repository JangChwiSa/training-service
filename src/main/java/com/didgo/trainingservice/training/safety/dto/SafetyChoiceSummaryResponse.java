package com.didgo.trainingservice.training.safety.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record SafetyChoiceSummaryResponse(
        @Schema(description = "?뺣떟 ?좏깮 ?섏엯?덈떎.", example = "7")
        int correctCount,
        @Schema(description = "?꾩껜 ?좏깮 ?섏엯?덈떎.", example = "10")
        int totalCount
) {
}
