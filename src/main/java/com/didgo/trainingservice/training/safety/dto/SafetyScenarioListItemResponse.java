package com.didgo.trainingservice.training.safety.dto;

import com.didgo.trainingservice.training.safety.entity.SafetyCategory;
import io.swagger.v3.oas.annotations.media.Schema;

public record SafetyScenarioListItemResponse(
        @Schema(description = "?덉쟾 ?쒕굹由ъ삤 ID?낅땲?? ?몄뀡 ?쒖옉 ?붿껌??scenarioId濡??ъ슜?⑸땲??", example = "1")
        long scenarioId,
        @Schema(description = "?덉쟾 ?덈젴 移댄뀒怨좊━?낅땲??", example = "COMMUTE_SAFETY")
        SafetyCategory category,
        @Schema(description = "?덉쟾 ?쒕굹由ъ삤 ?쒕ぉ?낅땲??")
        String title,
        @Schema(description = "?덉쟾 ?쒕굹由ъ삤 ?ㅻ챸?낅땲??")
        String description
) {
}
