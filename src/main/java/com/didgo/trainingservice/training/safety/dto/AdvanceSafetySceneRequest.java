package com.didgo.trainingservice.training.safety.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;

public record AdvanceSafetySceneRequest(
        @Schema(description = "Current safety scene id.", example = "1")
        @Positive long sceneId
) {
}
