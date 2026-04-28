package com.jangchwisa.trainingservice.training.document.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record DocumentAnswerRequest(
        @Positive long questionId,
        @NotBlank String userAnswer
) {
}
