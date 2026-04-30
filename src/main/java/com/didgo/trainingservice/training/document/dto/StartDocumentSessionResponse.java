package com.didgo.trainingservice.training.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record StartDocumentSessionResponse(
        @Schema(description = "?앹꽦??臾몄꽌 ?댄빐 ?덈젴 ?몄뀡 ID?낅땲?? ?듬? ?쒖텧 ?먮뒗 ?곸꽭 議고쉶 ?붿껌??path 媛믪쑝濡??ъ슜?⑸땲??", example = "50")
        long sessionId,
        @Schema(description = "?대쾲 臾몄꽌 ?댄빐 ?몄뀡?먯꽌 ??댁빞 ??臾몄젣 紐⑸줉?낅땲??")
        List<DocumentQuestionResponse> questions
) {
}
