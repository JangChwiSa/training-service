package com.jangchwisa.trainingservice.training.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record DocumentQuestionChoiceResponse(
        @Schema(description = "Document question choice ID.", example = "1")
        long choiceId,
        @Schema(description = "Choice display order.", example = "1")
        int choiceOrder,
        @Schema(description = "Choice text shown to the user.")
        String text
) {
}
