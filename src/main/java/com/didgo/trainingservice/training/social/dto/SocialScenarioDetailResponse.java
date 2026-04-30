package com.didgo.trainingservice.training.social.dto;

import com.didgo.trainingservice.training.social.entity.SocialJobType;
import io.swagger.v3.oas.annotations.media.Schema;

public record SocialScenarioDetailResponse(
        @Schema(description = "?ы쉶???쒕굹由ъ삤 ID?낅땲??", example = "1")
        long scenarioId,
        @Schema(description = "?쒕굹由ъ삤媛 ?랁븳 吏곷Т ?좏삎?낅땲??", example = "OFFICE")
        SocialJobType jobType,
        @Schema(description = "?쒕굹由ъ삤 ?쒕ぉ?낅땲??", example = "Ask a coworker for help")
        String title,
        @Schema(description = "?덈젴 ?곹솴??諛곌꼍 ?ㅻ챸?낅땲??")
        String backgroundText,
        @Schema(description = "?ъ슜?먭? ??묓빐???섎뒗 援ъ껜?곸씤 ?곹솴 ?ㅻ챸?낅땲??")
        String situationText,
        @Schema(description = "????곷? ?먮뒗 ?깆옣?몃Ъ ?뺣낫?낅땲??")
        String characterInfo,
        @Schema(description = "?쒕굹由ъ삤 ?쒖씠?꾩엯?덈떎.", example = "1")
        Integer difficulty
) {
}
