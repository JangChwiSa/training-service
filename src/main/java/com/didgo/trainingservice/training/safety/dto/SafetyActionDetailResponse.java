package com.didgo.trainingservice.training.safety.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record SafetyActionDetailResponse(
        @Schema(description = "Selected scene ID.", example = "1")
        long sceneId,
        @Schema(description = "Situation text for the scene.", example = "There is water on the floor.")
        String situationText,
        @Schema(description = "Selected choice text.", example = "Report it to the manager.")
        String selectedChoice,
        @Schema(description = "Whether the selected choice is correct.", example = "true")
        boolean correct
) {
}
