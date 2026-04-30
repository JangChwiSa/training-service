package com.didgo.trainingservice.training.progress.dto;

import com.didgo.trainingservice.training.session.entity.TrainingType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record SafetyProgressResponse(
        @Schema(description = "?덈젴 ?좏삎?낅땲??", example = "SAFETY")
        TrainingType trainingType,
        @Schema(description = "理쒓렐 ?꾨즺???덉쟾 ?덈젴 ?몄뀡 ID?낅땲?? 湲곕줉???놁쑝硫?null?낅땲??", example = "20")
        Long recentSessionId,
        @Schema(description = "?꾩쟻 ?먮뒗 理쒓렐 ?덉쟾 ?덈젴 ?뺣떟 ?섏엯?덈떎.", example = "7")
        int correctCount,
        @Schema(description = "?꾩쟻 ?먮뒗 理쒓렐 ?덉쟾 ?덈젴 ?꾩껜 ?좏깮 ?섏엯?덈떎.", example = "10")
        int totalCount,
        @Schema(description = "?꾨즺???덉쟾 ?덈젴 ?잛닔?낅땲??", example = "2")
        int completedCount,
        @Schema(description = "留덉?留??덉쟾 ?덈젴 ?꾨즺 ?쒓컖?낅땲?? 湲곕줉???놁쑝硫?null?낅땲??", example = "2026-04-27T10:20:00")
        LocalDateTime lastCompletedAt
) implements TrainingProgressResponse {
}
