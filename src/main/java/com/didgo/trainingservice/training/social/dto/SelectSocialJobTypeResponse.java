package com.didgo.trainingservice.training.social.dto;

import com.didgo.trainingservice.training.social.entity.SocialJobType;
import io.swagger.v3.oas.annotations.media.Schema;

public record SelectSocialJobTypeResponse(
        @Schema(description = "?좏깮???ы쉶???덈젴 吏곷Т ?좏삎?낅땲??", example = "OFFICE")
        SocialJobType jobType,
        @Schema(description = "吏곷Т ?좏삎 ?좏깮 ???꾨줎?멸? ?대룞???ㅼ쓬 ?붾㈃ ?앸퀎?먯엯?덈떎.", example = "SCENARIO_SELECTION")
        String nextPage
) {
}
