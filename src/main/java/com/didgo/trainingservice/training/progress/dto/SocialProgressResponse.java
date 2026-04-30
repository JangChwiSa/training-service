package com.didgo.trainingservice.training.progress.dto;

import com.didgo.trainingservice.training.session.entity.TrainingType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record SocialProgressResponse(
        @Schema(description = "?덈젴 ?좏삎?낅땲??", example = "SOCIAL")
        TrainingType trainingType,
        @Schema(description = "理쒓렐 ?꾨즺???ы쉶???덈젴 ?몄뀡 ID?낅땲?? 湲곕줉???놁쑝硫?null?낅땲??", example = "10")
        Long recentSessionId,
        @Schema(description = "理쒓렐 ?ы쉶???덈젴 ?먯닔?낅땲?? 湲곕줉???놁쑝硫?null?낅땲??", example = "85")
        Integer recentScore,
        @Schema(description = "理쒓렐 ?ы쉶???덈젴 ?쇰뱶諛??붿빟?낅땲?? 湲곕줉???놁쑝硫?null?낅땲??")
        String recentFeedbackSummary,
        @Schema(description = "?꾨즺???ы쉶???덈젴 ?잛닔?낅땲??", example = "3")
        int completedCount,
        @Schema(description = "留덉?留??ы쉶???덈젴 ?꾨즺 ?쒓컖?낅땲?? 湲곕줉???놁쑝硫?null?낅땲??", example = "2026-04-27T10:00:00")
        LocalDateTime lastCompletedAt
) implements TrainingProgressResponse {
}
