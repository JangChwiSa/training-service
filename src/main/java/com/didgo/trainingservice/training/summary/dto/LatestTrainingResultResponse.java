package com.didgo.trainingservice.training.summary.dto;

import com.didgo.trainingservice.training.session.entity.TrainingType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record LatestTrainingResultResponse(
        @Schema(description = "?꾨즺???덈젴 ?몄뀡 ID?낅땲??", example = "10")
        long sessionId,
        @Schema(description = "?덈젴 ?좏삎?낅땲??", example = "SOCIAL")
        TrainingType trainingType,
        @Schema(description = "?덈젴 ?먯닔?낅땲?? 0遺??100 ?ъ씠 媛믪엯?덈떎.", example = "85")
        int score,
        @Schema(description = "?먯닔 ?곗젙 諛⑹떇?낅땲??", example = "AI_EVALUATION")
        String scoreType,
        @Schema(description = "?덈젴 ?꾨즺 ?쒓컖?낅땲??", example = "2026-04-27T10:30:00")
        LocalDateTime completedAt
) {
}
