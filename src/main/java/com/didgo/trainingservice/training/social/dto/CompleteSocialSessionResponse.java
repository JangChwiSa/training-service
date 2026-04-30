package com.didgo.trainingservice.training.social.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record CompleteSocialSessionResponse(
        @Schema(description = "?꾨즺 泥섎━???ы쉶???덈젴 ?몄뀡 ID?낅땲??", example = "10")
        long sessionId,
        @Schema(description = "?ы쉶???덈젴 ?됯? ?먯닔?낅땲?? 0遺??100 ?ъ씠 媛믪엯?덈떎.", example = "85")
        int score,
        @Schema(description = "?덈젴 寃곌낵 ?쇰뱶諛??붿빟?낅땲??")
        String feedbackSummary,
        @Schema(description = "?몄뀡 ?꾨즺 ?щ??낅땲??", example = "true")
        boolean completed
) {
}
