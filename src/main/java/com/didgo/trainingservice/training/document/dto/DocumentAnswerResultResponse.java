package com.didgo.trainingservice.training.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record DocumentAnswerResultResponse(
        @Schema(description = "梨꾩젏??臾몄꽌 臾몄젣 ID?낅땲??", example = "1")
        long questionId,
        @Schema(description = "?ъ슜???듬????뺣떟?몄? ?щ??낅땲??", example = "true")
        boolean correct,
        @Schema(description = "臾몄젣???뺣떟?낅땲??")
        String correctAnswer,
        @Schema(description = "?뺣떟 ?먮떒??????댁꽕?낅땲??")
        String explanation
) {
}
