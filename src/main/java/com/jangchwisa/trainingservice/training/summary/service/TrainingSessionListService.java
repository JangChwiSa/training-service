package com.jangchwisa.trainingservice.training.summary.service;

import com.jangchwisa.trainingservice.training.safety.entity.SafetyCategory;
import com.jangchwisa.trainingservice.training.session.entity.TrainingType;
import com.jangchwisa.trainingservice.training.summary.dto.TrainingSessionListItemResponse;
import com.jangchwisa.trainingservice.training.summary.dto.TrainingSessionListResponse;
import com.jangchwisa.trainingservice.training.summary.repository.TrainingSessionSummaryRepository;
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
            SafetyCategory category,
            int page,
            int size
    ) {
        SafetyCategory categoryFilter = trainingType == TrainingType.SAFETY ? category : null;
        long totalElements = trainingSessionSummaryRepository.countByUserIdAndTrainingType(
                userId,
                trainingType,
                categoryFilter
        );
        List<TrainingSessionListItemResponse> sessions = trainingSessionSummaryRepository.findByUserIdAndTrainingType(
                userId,
                trainingType,
                categoryFilter,
                page,
                size
        );

        return new TrainingSessionListResponse(trainingType, page, size, totalElements, sessions);
    }
}
