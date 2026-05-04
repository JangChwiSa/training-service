package com.didgo.trainingservice.training.safety.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record SafetySceneResponse(
        @Schema(description = "Safety scene id.", example = "1")
        long sceneId,
        @Schema(description = "Short label for the scene screen.")
        String screenInfo,
        @Schema(description = "Situation text shown to the user.")
        String situationText,
        @Schema(description = "Question text shown to the user.")
        String questionText,
        @Schema(description = "Scene image URL.")
        String imageUrl,
        @Schema(description = "Alternative text for the scene image.")
        String imageAlt,
        @Schema(description = "Choices available in the scene.")
        List<SafetyChoiceResponse> choices,
        @Schema(description = "Whether this scene is marked as an end scene.", example = "false")
        boolean endScene
) {
}
