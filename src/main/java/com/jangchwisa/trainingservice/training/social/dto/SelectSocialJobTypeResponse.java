package com.jangchwisa.trainingservice.training.social.dto;

import com.jangchwisa.trainingservice.training.social.entity.SocialJobType;
import io.swagger.v3.oas.annotations.media.Schema;

public record SelectSocialJobTypeResponse(
        @Schema(description = "선택된 사회성 훈련 직무 유형입니다.", example = "OFFICE")
        SocialJobType jobType,
        @Schema(description = "직무 유형 선택 후 프론트가 이동할 다음 화면 식별자입니다.", example = "SCENARIO_SELECTION")
        String nextPage
) {
}
