package com.jangchwisa.trainingservice.training.progress.repository;

import com.jangchwisa.trainingservice.training.progress.dto.DocumentProgressResponse;
import com.jangchwisa.trainingservice.training.progress.dto.FocusProgressResponse;
import com.jangchwisa.trainingservice.training.progress.dto.SafetyProgressResponse;
import com.jangchwisa.trainingservice.training.progress.dto.SocialProgressResponse;
import java.util.Optional;

public interface TrainingProgressRepository {

    Optional<SocialProgressResponse> findSocialProgress(long userId);

    Optional<SafetyProgressResponse> findSafetyProgress(long userId);

    Optional<DocumentProgressResponse> findDocumentProgress(long userId);

    Optional<FocusProgressResponse> findFocusProgress(long userId);
}
