package com.didgo.trainingservice.training.safety.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record SafetySessionDetailResponse(
        @Schema(description = "議고쉶???덉쟾 ?덈젴 ?몄뀡 ID?낅땲??", example = "20")
        long sessionId,
        @Schema(description = "?덉쟾 ?덈젴 ?먯닔?낅땲??", example = "70")
        int score,
        @Schema(description = "?뺣떟 ?좏깮 ?섏? ?꾩껜 ?좏깮 ???붿빟?낅땲??")
        SafetyChoiceSummaryResponse choiceSummary,
        @Schema(description = "?곹솴 ?ㅻ챸怨??좏깮吏 臾멸뎄瑜??ы븿???좏깮 ?대젰?낅땲??")
        List<SafetyActionDetailResponse> actions,
        @Schema(description = "?덉쟾 ?덈젴 ?꾨즺 ????λ맂 ?쇰뱶諛깆엯?덈떎.")
        SafetyFeedbackResponse feedback
) {
}
