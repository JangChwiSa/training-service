package com.didgo.trainingservice.training.social.dto;

import com.didgo.trainingservice.training.session.entity.TrainingSessionStatus;
import io.swagger.v3.oas.annotations.media.Schema;

public record StartSocialSessionResponse(
        @Schema(description = "?앹꽦???ы쉶???덈젴 ?몄뀡 ID?낅땲?? ?댄썑 ?곸꽭 議고쉶 ?먮뒗 ?꾨즺 ?붿껌??path 媛믪쑝濡??ъ슜?⑸땲??", example = "10")
        long sessionId,
        @Schema(description = "?몄뀡???곌껐???ы쉶???쒕굹由ъ삤 ID?낅땲??", example = "1")
        long scenarioId,
        @Schema(description = "?몄뀡 吏꾪뻾 ?곹깭?낅땲??", example = "IN_PROGRESS")
        TrainingSessionStatus status
) {
}
