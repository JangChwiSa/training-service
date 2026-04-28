package com.jangchwisa.trainingservice.training.document.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record SubmitDocumentAnswersRequest(
        @NotEmpty List<@Valid DocumentAnswerRequest> answers
) {
}
