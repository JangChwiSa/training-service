package com.didgo.trainingservice.training.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record DocumentAnswerResultResponse(
        @Schema(description = "Document question ID.", example = "1")
        long questionId,
        @Schema(description = "Question text shown to the learner.")
        String questionText,
        @Schema(description = "Answer submitted by the learner.")
        String userAnswer,
        @Schema(description = "Whether the submitted answer is correct.", example = "true")
        boolean correct,
        @Schema(description = "Correct answer.")
        String correctAnswer,
        @Schema(description = "Explanation for the correct answer.")
        String explanation
) {
}
