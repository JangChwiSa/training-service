package com.didgo.trainingservice.training.safety.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record SafetyChoiceResponse(
        @Schema(description = "?좏깮吏 ID?낅땲?? ?ㅼ쓬 ?λ㈃ 吏꾪뻾 ?붿껌??choiceId濡??ъ슜?⑸땲??", example = "1")
        long choiceId,
        @Schema(description = "?ъ슜?먯뿉寃?蹂댁뿬以??좏깮吏 臾멸뎄?낅땲??")
        String text
) {
}
