package com.didgo.trainingservice.training.safety.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record SafetyActionLogResponse(
        @Schema(description = "?ъ슜?먭? ?좏깮???섑뻾???λ㈃ ID?낅땲??", example = "1")
        long sceneId,
        @Schema(description = "?ъ슜?먭? ?좏깮???좏깮吏 ID?낅땲??", example = "1")
        long choiceId,
        @Schema(description = "?대떦 ?좏깮???뺣떟?몄? ?щ??낅땲??", example = "true")
        boolean correct
) {
}
