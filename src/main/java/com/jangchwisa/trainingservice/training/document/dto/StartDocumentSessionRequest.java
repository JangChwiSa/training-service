package com.jangchwisa.trainingservice.training.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record StartDocumentSessionRequest(
        @Schema(description = "Document training level. Allowed values are 1 through 5.", example = "1")
        @NotNull
        @Min(1)
        @Max(5)
        Integer level
) {
}
