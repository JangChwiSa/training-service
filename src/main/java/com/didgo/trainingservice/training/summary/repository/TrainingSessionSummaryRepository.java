package com.didgo.trainingservice.training.summary.repository;

import com.didgo.trainingservice.training.session.entity.TrainingType;
import com.didgo.trainingservice.training.summary.dto.TrainingSessionListItemResponse;
import com.didgo.trainingservice.training.summary.entity.TrainingSessionSummary;
import java.util.List;

public interface TrainingSessionSummaryRepository {

    default void save(TrainingSessionSummary summary) {
        throw new UnsupportedOperationException("save is not implemented.");
    }

    long countByUserIdAndTrainingType(long userId, TrainingType trainingType);

    List<TrainingSessionListItemResponse> findByUserIdAndTrainingType(
            long userId,
            TrainingType trainingType,
            int page,
            int size
    );
}
