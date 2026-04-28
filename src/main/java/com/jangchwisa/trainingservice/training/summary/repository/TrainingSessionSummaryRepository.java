package com.jangchwisa.trainingservice.training.summary.repository;

import com.jangchwisa.trainingservice.training.safety.entity.SafetyCategory;
import com.jangchwisa.trainingservice.training.session.entity.TrainingType;
import com.jangchwisa.trainingservice.training.summary.dto.TrainingSessionListItemResponse;
import java.util.List;

public interface TrainingSessionSummaryRepository {

    long countByUserIdAndTrainingType(long userId, TrainingType trainingType, SafetyCategory category);

    List<TrainingSessionListItemResponse> findByUserIdAndTrainingType(
            long userId,
            TrainingType trainingType,
            SafetyCategory category,
            int page,
            int size
    );
}
