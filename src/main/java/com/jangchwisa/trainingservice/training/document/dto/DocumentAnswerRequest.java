package com.jangchwisa.trainingservice.training.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;

public record DocumentAnswerRequest(
        @Schema(description = "Document question ID received from the session start response.", example = "1")
        @Positive long questionId,
        @Schema(description = "User typed answer for SHORT_ANSWER questions.", example = "Room 2")
        String userAnswer,
        @Schema(description = "Selected choice ID for MULTIPLE_CHOICE questions.", example = "10")
        @Positive Long choiceId
) {
    public DocumentAnswerRequest(long questionId, String userAnswer) {
        this(questionId, userAnswer, null);
    }
}
