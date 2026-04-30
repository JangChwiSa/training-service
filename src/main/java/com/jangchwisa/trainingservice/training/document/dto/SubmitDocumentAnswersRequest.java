package com.jangchwisa.trainingservice.training.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record SubmitDocumentAnswersRequest(
        @Schema(description = "문서 이해 훈련 문제별 답변 목록입니다. 최소 1개 이상 필요합니다.")
        @NotEmpty List<@Valid DocumentAnswerRequest> answers
) {
}
