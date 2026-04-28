package com.jangchwisa.trainingservice.training.progress.service;

import com.jangchwisa.trainingservice.training.progress.dto.DocumentProgressResponse;
import com.jangchwisa.trainingservice.training.progress.dto.FocusProgressResponse;
import com.jangchwisa.trainingservice.training.progress.dto.SafetyProgressResponse;
import com.jangchwisa.trainingservice.training.progress.dto.SocialProgressResponse;
import com.jangchwisa.trainingservice.training.progress.dto.TrainingProgressResponse;
import com.jangchwisa.trainingservice.training.progress.repository.TrainingProgressRepository;
import com.jangchwisa.trainingservice.training.session.entity.TrainingType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TrainingProgressService {

    private final TrainingProgressRepository trainingProgressRepository;

    public TrainingProgressService(TrainingProgressRepository trainingProgressRepository) {
        this.trainingProgressRepository = trainingProgressRepository;
    }

    @Transactional(readOnly = true)
    public TrainingProgressResponse getProgress(long userId, TrainingType trainingType) {
        return switch (trainingType) {
            case SOCIAL -> trainingProgressRepository.findSocialProgress(userId)
                    .orElseGet(TrainingProgressService::defaultSocialProgress);
            case SAFETY -> trainingProgressRepository.findSafetyProgress(userId)
                    .orElseGet(TrainingProgressService::defaultSafetyProgress);
            case DOCUMENT -> trainingProgressRepository.findDocumentProgress(userId)
                    .orElseGet(TrainingProgressService::defaultDocumentProgress);
            case FOCUS -> trainingProgressRepository.findFocusProgress(userId)
                    .orElseGet(TrainingProgressService::defaultFocusProgress);
        };
    }

    private static SocialProgressResponse defaultSocialProgress() {
        return new SocialProgressResponse(TrainingType.SOCIAL, null, null, null, 0, null);
    }

    private static SafetyProgressResponse defaultSafetyProgress() {
        return new SafetyProgressResponse(TrainingType.SAFETY, null, 0, 0, 0, null);
    }

    private static DocumentProgressResponse defaultDocumentProgress() {
        return new DocumentProgressResponse(TrainingType.DOCUMENT, null, 0, 0, null, 0, null);
    }

    private static FocusProgressResponse defaultFocusProgress() {
        return new FocusProgressResponse(TrainingType.FOCUS, 1, 1, null, null, null, null);
    }
}
