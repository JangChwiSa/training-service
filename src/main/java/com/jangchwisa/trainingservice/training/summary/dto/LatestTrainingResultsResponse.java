package com.jangchwisa.trainingservice.training.summary.dto;

import java.util.List;

public record LatestTrainingResultsResponse(
        long userId,
        List<LatestTrainingResultResponse> results
) {
}
