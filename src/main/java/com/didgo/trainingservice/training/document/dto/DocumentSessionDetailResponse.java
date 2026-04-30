package com.didgo.trainingservice.training.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record DocumentSessionDetailResponse(
        @Schema(description = "議고쉶??臾몄꽌 ?댄빐 ?덈젴 ?몄뀡 ID?낅땲??", example = "50")
        long sessionId,
        @Schema(description = "臾몄꽌 ?댄빐 ?덈젴 ?먯닔?낅땲?? 0遺??100 ?ъ씠 媛믪엯?덈떎.", example = "80")
        int score,
        @Schema(description = "臾몄꽌 ?댄빐 ?덈젴 ?뺣떟 ?붿빟?낅땲??")
        DocumentAnswerSummaryResponse answerSummary,
        @Schema(description = "臾몄젣蹂??ъ슜???듬?, ?뺣떟, ?댁꽕 ?곸꽭 紐⑸줉?낅땲??")
        List<DocumentAnswerDetailResponse> answers
) {
}
