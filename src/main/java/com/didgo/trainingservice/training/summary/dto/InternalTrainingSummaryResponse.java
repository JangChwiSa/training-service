package com.didgo.trainingservice.training.summary.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record InternalTrainingSummaryResponse(
        @Schema(description = "?ъ슜?먯쓽 理쒓렐 ?ы쉶???덈젴 ?먯닔?낅땲?? 湲곕줉???놁쑝硫?null?낅땲??", example = "85")
        Integer socialRecentScore,
        @Schema(description = "?ъ슜?먯쓽 ?덉쟾 ?덈젴 ?뺣떟 ?섏엯?덈떎.", example = "7")
        int safetyCorrectCount,
        @Schema(description = "?ъ슜?먯쓽 ?덉쟾 ?덈젴 ?꾩껜 ?좏깮 ?섏엯?덈떎.", example = "10")
        int safetyTotalCount,
        @Schema(description = "?ъ슜?먯쓽 臾몄꽌 ?댄빐 ?덈젴 ?뺣떟 ?섏엯?덈떎.", example = "8")
        int documentCorrectCount,
        @Schema(description = "?ъ슜?먯쓽 臾몄꽌 ?댄빐 ?덈젴 ?꾩껜 臾몄젣 ?섏엯?덈떎.", example = "10")
        int documentTotalCount,
        @Schema(description = "?ъ슜?먯쓽 ?꾩옱 吏묒쨷???덈젴 ?④퀎?낅땲??", example = "3")
        int focusCurrentLevel
) {
}
