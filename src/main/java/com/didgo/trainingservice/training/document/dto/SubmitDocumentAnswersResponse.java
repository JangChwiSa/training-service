package com.didgo.trainingservice.training.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record SubmitDocumentAnswersResponse(
        @Schema(description = "?꾨즺 泥섎━??臾몄꽌 ?댄빐 ?덈젴 ?몄뀡 ID?낅땲??", example = "50")
        long sessionId,
        @Schema(description = "臾몄꽌 ?댄빐 ?덈젴 ?먯닔?낅땲?? 0遺??100 ?ъ씠 媛믪엯?덈떎.", example = "100")
        int score,
        @Schema(description = "?뺣떟 ?섏엯?덈떎.", example = "1")
        int correctCount,
        @Schema(description = "?꾩껜 臾몄젣 ?섏엯?덈떎.", example = "1")
        int totalCount,
        @Schema(description = "臾몄젣蹂?梨꾩젏 寃곌낵 紐⑸줉?낅땲??")
        List<DocumentAnswerResultResponse> results,
        @Schema(description = "?몄뀡 ?꾨즺 ?щ??낅땲??", example = "true")
        boolean completed
) {
}
