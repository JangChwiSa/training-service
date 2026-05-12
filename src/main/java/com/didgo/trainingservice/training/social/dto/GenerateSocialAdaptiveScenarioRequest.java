package com.didgo.trainingservice.training.social.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record GenerateSocialAdaptiveScenarioRequest(
        @Schema(description = "Job type for the adaptive social scenario.", example = "OFFICE")
        @NotBlank String jobType
) {
}
