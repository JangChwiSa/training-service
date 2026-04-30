package com.didgo.trainingservice.training.summary.service;

import com.didgo.trainingservice.training.session.entity.TrainingType;
import com.didgo.trainingservice.training.summary.dto.TrainingSessionListItemResponse;
import com.didgo.trainingservice.training.summary.dto.TrainingSessionListResponse;
import com.didgo.trainingservice.training.summary.repository.TrainingSessionSummaryRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TrainingSessionListService {

    private final TrainingSessionSummaryRepository trainingSessionSummaryRepository;

    public TrainingSessionListService(TrainingSessionSummaryRepository trainingSessionSummaryRepository) {
        this.trainingSessionSummaryRepository = trainingSessionSummaryRepository;
    }

    @Transactional(readOnly = true)
    public TrainingSessionListResponse getSessions(
            long userId,
            TrainingType trainingType,
            int page,
            int size
    ) {
        long totalElements = trainingSessionSummaryRepository.countByUserIdAndTrainingType(
                userId,
                trainingType
        );
        List<TrainingSessionListItemResponse> sessions = trainingSessionSummaryRepository.findByUserIdAndTrainingType(
                userId,
                trainingType,
                page,
                size
        );

        return new TrainingSessionListResponse(trainingType, page, size, totalElements, sessions);
    }
}
