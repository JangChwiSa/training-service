package com.didgo.trainingservice.training.safety.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record SafetySelectedResultResponse(
        @Schema(description = "Whether the selected choice is correct.", example = "true")
        boolean correct,
        @Schema(description = "Result text for the selected choice.")
        String resultText,
        @Schema(description = "Effect text for the selected choice.")
        String effectText,
        @Schema(description = "Feedback image URL.")
        String feedbackImageUrl,
        @Schema(description = "Alternative text for the feedback image.")
        String feedbackImageAlt
) {
    public SafetySelectedResultResponse(boolean correct) {
        this(correct, null, null, null, null);
    }
}
