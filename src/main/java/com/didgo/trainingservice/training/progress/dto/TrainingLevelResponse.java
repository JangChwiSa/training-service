package com.didgo.trainingservice.training.progress.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.didgo.trainingservice.training.session.entity.TrainingType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.ALWAYS)
public record TrainingLevelResponse(
        @Schema(description = "Training type.", example = "SOCIAL")
        TrainingType trainingType,
        @Schema(description = "Computed monthly training level. Null when it cannot be computed.", example = "4")
        Integer level,
        @Schema(description = "Monthly period start in Asia/Seoul.", example = "2026-04-01T00:00:00")
        LocalDateTime periodStart,
        @Schema(description = "Exclusive monthly period end in Asia/Seoul.", example = "2026-05-01T00:00:00")
        LocalDateTime periodEnd,
        @Schema(description = "Timezone used for monthly period calculation.", example = "Asia/Seoul")
        String timezone,
        @Schema(description = "Monthly completed training count.", example = "3")
        int completedCount,
        @Schema(description = "Minimum monthly completion count required to compute level.", example = "3")
        int minRequiredCount,
        @Schema(description = "Data basis used for level calculation.", example = "MONTHLY_COMPLETED_SUMMARIES")
        String basis,
        @Schema(description = "Reason level is null. Null when level is computed.", example = "INSUFFICIENT_COMPLETIONS")
        String reason,
        @Schema(description = "Type-specific calculation metrics.")
        Map<String, Object> metrics
) implements TrainingProgressResponse {
}
