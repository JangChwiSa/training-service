package com.didgo.trainingservice.training.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record SubmitDocumentAnswersRequest(
        @Schema(description = "臾몄꽌 ?댄빐 ?덈젴 臾몄젣蹂??듬? 紐⑸줉?낅땲?? 理쒖냼 1媛??댁긽 ?꾩슂?⑸땲??")
        @NotEmpty List<@Valid DocumentAnswerRequest> answers
) {
}
