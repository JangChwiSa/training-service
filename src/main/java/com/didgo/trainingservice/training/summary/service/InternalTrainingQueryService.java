package com.didgo.trainingservice.training.summary.service;

import com.didgo.trainingservice.training.summary.dto.InternalTrainingSummaryResponse;
import com.didgo.trainingservice.training.summary.dto.LatestTrainingResultsResponse;
import com.didgo.trainingservice.training.summary.repository.InternalTrainingQueryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InternalTrainingQueryService {

    private final InternalTrainingQueryRepository internalTrainingQueryRepository;

    public InternalTrainingQueryService(InternalTrainingQueryRepository internalTrainingQueryRepository) {
        this.internalTrainingQueryRepository = internalTrainingQueryRepository;
    }

    @Transactional(readOnly = true)
    public InternalTrainingSummaryResponse getSummary(long userId) {
        return internalTrainingQueryRepository.findTrainingSummary(userId);
    }

    @Transactional(readOnly = true)
    public LatestTrainingResultsResponse getLatestResults(long userId) {
        return new LatestTrainingResultsResponse(
                userId,
                internalTrainingQueryRepository.findLatestTrainingResults(userId)
        );
    }
}
