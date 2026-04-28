package com.jangchwisa.trainingservice.training.summary.repository;

import com.jangchwisa.trainingservice.training.safety.entity.SafetyCategory;
import com.jangchwisa.trainingservice.training.session.entity.TrainingType;
import com.jangchwisa.trainingservice.training.summary.dto.TrainingSessionListItemResponse;
import com.jangchwisa.trainingservice.training.summary.entity.TrainingSessionSummary;
import java.util.List;

public interface TrainingSessionSummaryRepository {

    default void save(TrainingSessionSummary summary) {
        throw new UnsupportedOperationException("save is not implemented.");
    }

    long countByUserIdAndTrainingType(long userId, TrainingType trainingType, SafetyCategory category);

    List<TrainingSessionListItemResponse> findByUserIdAndTrainingType(
            long userId,
            TrainingType trainingType,
            SafetyCategory category,
            int page,
            int size
    );
}
