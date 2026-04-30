package com.didgo.trainingservice.training.social.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record SocialScenarioListItemResponse(
        @Schema(description = "?ы쉶???쒕굹由ъ삤 ID?낅땲?? ?몄뀡 ?쒖옉 ?붿껌??scenarioId濡??ъ슜?⑸땲??", example = "1")
        long scenarioId,
        @Schema(description = "?쒕굹由ъ삤 ?쒕ぉ?낅땲??", example = "Ask a coworker for help")
        String title,
        @Schema(description = "?쒕굹由ъ삤 ?쒖씠?꾩엯?덈떎.", example = "1")
        Integer difficulty
) {
}
