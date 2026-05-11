package com.didgo.trainingservice.training.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record DocumentQuestionResponse(
        @Schema(description = "Document question ID.", example = "1")
        long questionId,
        @Schema(description = "Document display theme.", example = "ANNOUNCEMENT")
        String theme,
        @Schema(description = "Document question title.")
        String title,
        @Schema(description = "Document text the user should read.")
        String documentText,
        @Schema(description = "Question text.")
        String questionText,
        @Schema(description = "Question type.", example = "SHORT_ANSWER")
        String questionType,
        @Schema(description = "Multiple choice options. Empty for SHORT_ANSWER questions.")
        List<DocumentQuestionChoiceResponse> choices
) {
    public DocumentQuestionResponse(
            long questionId,
            String title,
            String documentText,
            String questionText,
            String questionType
    ) {
        this(questionId, "ANNOUNCEMENT", title, documentText, questionText, questionType, List.of());
    }

    public DocumentQuestionResponse(
            long questionId,
            String title,
            String documentText,
            String questionText,
            String questionType,
            List<DocumentQuestionChoiceResponse> choices
    ) {
        this(questionId, "ANNOUNCEMENT", title, documentText, questionText, questionType, choices);
    }
}
