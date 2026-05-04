package com.didgo.trainingservice.training.focus.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;

public record StartFocusSessionRequest(
        @Schema(description = "?쒖옉??吏묒쨷???덈젴 ?④퀎?낅땲?? ?꾩옱 ?닿툑???④퀎 ?댄븯???묒닔瑜??낅젰?⑸땲??", example = "1")
        @Positive int level
) {
}
