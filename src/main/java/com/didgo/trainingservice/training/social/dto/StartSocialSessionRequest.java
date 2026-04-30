package com.didgo.trainingservice.training.social.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record StartSocialSessionRequest(
        @Schema(description = "?ы쉶???덈젴 吏곷Т ?좏삎?낅땲?? OFFICE ?먮뒗 LABOR瑜??낅젰?⑸땲??", example = "OFFICE")
        @NotBlank String jobType,
        @Schema(description = "?쒖옉???ы쉶???쒕굹由ъ삤 ID?낅땲?? ?쒕굹由ъ삤 紐⑸줉 議고쉶 API?먯꽌 諛쏆? scenarioId瑜??ｌ뒿?덈떎.", example = "1")
        @Positive long scenarioId
) {
}
