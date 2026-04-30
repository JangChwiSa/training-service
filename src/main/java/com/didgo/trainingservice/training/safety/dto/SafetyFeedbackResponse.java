package com.didgo.trainingservice.training.safety.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record SafetyFeedbackResponse(
        @Schema(description = "?덉쟾 ?덈젴 ?붿빟 ?쇰뱶諛깆엯?덈떎.", example = "?遺遺꾩쓽 ?꾪뿕 ?곹솴???щ컮瑜닿쾶 ?먮떒?덉뒿?덈떎.")
        String summary,
        @Schema(description = "?덉쟾 ?덈젴 ?곸꽭 ?쇰뱶諛깆엯?덈떎.", example = "誘몃걚?ъ슫 諛붾떏??諛쒓껄?덉쓣 ??愿由ъ옄?먭쾶 ?뚮┛ ?좏깮? ?곸젅?⑸땲??")
        String detailText
) {
}
