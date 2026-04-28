package com.jangchwisa.trainingservice.training.focus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record FocusReactionRequest(
        @Positive long commandId,
        @NotBlank String userInput,
        @PositiveOrZero int reactionMs
) {
}
