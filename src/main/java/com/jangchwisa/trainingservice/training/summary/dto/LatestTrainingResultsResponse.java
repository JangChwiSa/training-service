package com.jangchwisa.trainingservice.training.summary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record LatestTrainingResultsResponse(
        @Schema(description = "조회 대상 사용자 ID입니다.", example = "1")
        long userId,
        @Schema(description = "사용자의 최신 완료 훈련 결과 목록입니다.")
        List<LatestTrainingResultResponse> results
) {
}
