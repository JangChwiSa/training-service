package com.didgo.trainingservice.training.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record DocumentAnswerDetailResponse(
        @Schema(description = "臾몄꽌 臾몄젣 ID?낅땲??", example = "1")
        long questionId,
        @Schema(description = "臾몄꽌 臾몄젣 吏덈Ц?낅땲??")
        String questionText,
        @Schema(description = "?ъ슜?먭? ?쒖텧???듬??낅땲??")
        String userAnswer,
        @Schema(description = "臾몄젣???뺣떟?낅땲??")
        String correctAnswer,
        @Schema(description = "?ъ슜???듬????뺣떟?몄? ?щ??낅땲??", example = "true")
        boolean correct,
        @Schema(description = "?뺣떟 ?먮떒??????댁꽕?낅땲??")
        String explanation
) {
}
