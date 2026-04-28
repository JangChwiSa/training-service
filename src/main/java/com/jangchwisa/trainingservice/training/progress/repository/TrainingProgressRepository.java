package com.jangchwisa.trainingservice.training.progress.repository;

import com.jangchwisa.trainingservice.training.progress.dto.DocumentProgressResponse;
import com.jangchwisa.trainingservice.training.progress.dto.FocusProgressResponse;
import com.jangchwisa.trainingservice.training.progress.dto.SafetyProgressResponse;
import com.jangchwisa.trainingservice.training.progress.dto.SocialProgressResponse;
import com.jangchwisa.trainingservice.training.progress.entity.TrainingProgressCompletion;
import java.util.Optional;

public interface TrainingProgressRepository {

    default void applyCompletion(TrainingProgressCompletion completion) {
        throw new UnsupportedOperationException("applyCompletion is not implemented.");
    }

    Optional<SocialProgressResponse> findSocialProgress(long userId);

    Optional<SafetyProgressResponse> findSafetyProgress(long userId);

    Optional<DocumentProgressResponse> findDocumentProgress(long userId);

    Optional<FocusProgressResponse> findFocusProgress(long userId);
}
