package com.didgo.trainingservice.training.safety.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record CompleteSafetySessionResponse(
        @Schema(description = "?꾨즺 泥섎━???덉쟾 ?덈젴 ?몄뀡 ID?낅땲??", example = "20")
        long sessionId,
        @Schema(description = "?덉쟾 ?덈젴 ?먯닔?낅땲?? 0遺??100 ?ъ씠 媛믪엯?덈떎.", example = "70")
        int score,
        @Schema(description = "?뺣떟 ?좏깮 ?섏엯?덈떎.", example = "7")
        int correctCount,
        @Schema(description = "?꾩껜 ?좏깮 ?섏엯?덈떎.", example = "10")
        int totalCount,
        @Schema(description = "?몄뀡 ?꾨즺 ?щ??낅땲??", example = "true")
        boolean completed
) {
}
