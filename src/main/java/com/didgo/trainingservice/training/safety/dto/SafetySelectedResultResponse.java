package com.didgo.trainingservice.training.safety.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record SafetySelectedResultResponse(
        @Schema(description = "Whether the selected choice is correct.", example = "true")
        boolean correct,
        @Schema(description = "Result text for the selected choice.")
        String resultText,
        @Schema(description = "Effect text or scene hint for the selected choice.")
        String effectText
) {
    public SafetySelectedResultResponse(boolean correct) {
        this(correct, null, null);
    }
}
