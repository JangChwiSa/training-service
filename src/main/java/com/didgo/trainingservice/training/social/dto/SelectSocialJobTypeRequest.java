package com.didgo.trainingservice.training.social.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record SelectSocialJobTypeRequest(
        @Schema(description = "?ы쉶???덈젴 吏곷Т ?좏삎?낅땲?? OFFICE???щТ吏? LABOR???⑥닚 ?몃Т?낅땲??", example = "OFFICE")
        @NotBlank String jobType
) {
}
