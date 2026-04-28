package com.jangchwisa.trainingservice.training.summary.repository;

import com.jangchwisa.trainingservice.training.summary.dto.InternalTrainingSummaryResponse;
import com.jangchwisa.trainingservice.training.summary.dto.LatestTrainingResultResponse;
import java.util.List;

public interface InternalTrainingQueryRepository {

    InternalTrainingSummaryResponse findTrainingSummary(long userId);

    List<LatestTrainingResultResponse> findLatestTrainingResults(long userId);
}
