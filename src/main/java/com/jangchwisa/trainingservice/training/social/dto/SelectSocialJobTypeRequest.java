package com.jangchwisa.trainingservice.training.social.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record SelectSocialJobTypeRequest(
        @Schema(description = "사회성 훈련 직무 유형입니다. OFFICE는 사무직, LABOR는 단순 노무입니다.", example = "OFFICE")
        @NotBlank String jobType
) {
}
