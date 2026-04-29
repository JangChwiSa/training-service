package com.jangchwisa.trainingservice.training.summary.dto;

import com.jangchwisa.trainingservice.training.session.entity.TrainingType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record TrainingSessionListResponse(
        @Schema(description = "조회한 훈련 유형입니다.", example = "SOCIAL")
        TrainingType trainingType,
        @Schema(description = "현재 페이지 번호입니다. 0부터 시작합니다.", example = "0")
        int page,
        @Schema(description = "페이지당 항목 수입니다.", example = "10")
        int size,
        @Schema(description = "조건에 맞는 전체 완료 세션 수입니다.", example = "3")
        long totalElements,
        @Schema(description = "현재 페이지의 훈련 기록 목록입니다.")
        List<TrainingSessionListItemResponse> sessions
) {
}
