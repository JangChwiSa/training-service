package com.jangchwisa.trainingservice.training.progress.repository;

import com.jangchwisa.trainingservice.training.progress.dto.DocumentProgressResponse;
import com.jangchwisa.trainingservice.training.progress.dto.FocusProgressResponse;
import com.jangchwisa.trainingservice.training.progress.dto.SafetyProgressResponse;
import com.jangchwisa.trainingservice.training.progress.dto.SocialProgressResponse;
import com.jangchwisa.trainingservice.training.progress.entity.MonthlyTrainingSummaryEntry;
import com.jangchwisa.trainingservice.training.progress.entity.TrainingProgressCompletion;
import com.jangchwisa.trainingservice.training.session.entity.TrainingType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TrainingProgressRepository {

    default void applyCompletion(TrainingProgressCompletion completion) {
        throw new UnsupportedOperationException("applyCompletion is not implemented.");
    }

    default List<MonthlyTrainingSummaryEntry> findMonthlyCompletedSummaries(
            long userId,
            TrainingType trainingType,
            LocalDateTime periodStart,
            LocalDateTime periodEnd
    ) {
        throw new UnsupportedOperationException("findMonthlyCompletedSummaries is not implemented.");
    }

    default Optional<SocialProgressResponse> findSocialProgress(long userId) {
        throw new UnsupportedOperationException("findSocialProgress is not implemented.");
    }

    default Optional<SafetyProgressResponse> findSafetyProgress(long userId) {
        throw new UnsupportedOperationException("findSafetyProgress is not implemented.");
    }

    default Optional<DocumentProgressResponse> findDocumentProgress(long userId) {
        throw new UnsupportedOperationException("findDocumentProgress is not implemented.");
    }

    default Optional<FocusProgressResponse> findFocusProgress(long userId) {
        throw new UnsupportedOperationException("findFocusProgress is not implemented.");
    }
}
