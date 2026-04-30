package com.didgo.trainingservice.training.social.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record SocialFeedbackResponse(
        @Schema(description = "?덈젴 寃곌낵?????吏㏃? ?쇰뱶諛??붿빟?낅땲??")
        String summary,
        @Schema(description = "?덈젴 寃곌낵??????곸꽭 ?쇰뱶諛?臾몄옣?낅땲??")
        String detailText
) {
}
