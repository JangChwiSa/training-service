package com.didgo.trainingservice.training.safety.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;

public record StartSafetySessionRequest(
        @Schema(description = "?쒖옉???덉쟾 ?쒕굹由ъ삤 ID?낅땲?? ?덉쟾 ?쒕굹由ъ삤 紐⑸줉 議고쉶 API?먯꽌 諛쏆? scenarioId瑜??ｌ뒿?덈떎.", example = "1")
        @Positive long scenarioId
) {
}
