package com.didgo.trainingservice.training.safety.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record NextSafetySceneResponse(
        @Schema(description = "Whether the selected choice completed the flow.", example = "false")
        boolean completed,
        @Schema(description = "Next scene when the flow continues. Null when the flow is completed.")
        SafetySceneResponse nextScene,
        @Schema(description = "Choice result returned when the flow completes. Null while the flow continues.")
        SafetySelectedResultResponse result
) {
}
