package com.didgo.trainingservice.training.social.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record SocialSessionDetailResponse(
        @Schema(description = "議고쉶???ы쉶???덈젴 ?몄뀡 ID?낅땲??", example = "10")
        long sessionId,
        @Schema(description = "?ы쉶???덈젴 ?됯? ?먯닔?낅땲?? 0遺??100 ?ъ씠 媛믪엯?덈떎.", example = "85")
        int score,
        @Schema(description = "?먯닔 ?곗젙 諛⑹떇?낅땲??", example = "AI_EVALUATION")
        String scoreType,
        @Schema(description = "AI媛 ?앹꽦???ы쉶???덈젴 ?쇰뱶諛깆엯?덈떎.")
        SocialFeedbackResponse feedback,
        @Schema(description = "?덈젴 ?꾨즺 ????λ맂 ?꾩껜 ???濡쒓렇?낅땲??")
        List<SocialDialogLogResponse> dialogLogs
) {
}
