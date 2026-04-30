package com.jangchwisa.trainingservice.training.progress.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

public record TrainingProgressSummaryResponse(
        @Schema(description = "Monthly period start in Asia/Seoul.", example = "2026-04-01T00:00:00")
        LocalDateTime periodStart,
        @Schema(description = "Exclusive monthly period end in Asia/Seoul.", example = "2026-05-01T00:00:00")
        LocalDateTime periodEnd,
        @Schema(description = "Timezone used for monthly period calculation.", example = "Asia/Seoul")
        String timezone,
        @Schema(description = "Monthly level summaries ordered by SOCIAL, SAFETY, DOCUMENT, FOCUS.")
        List<TrainingLevelResponse> items
) implements TrainingProgressResponse {
}
