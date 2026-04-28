package com.jangchwisa.trainingservice.training.progress.controller;

import com.jangchwisa.trainingservice.common.exception.ErrorCode;
import com.jangchwisa.trainingservice.common.exception.TrainingServiceException;
import com.jangchwisa.trainingservice.common.response.ApiResponse;
import com.jangchwisa.trainingservice.common.security.AuthenticatedUser;
import com.jangchwisa.trainingservice.common.security.CurrentUser;
import com.jangchwisa.trainingservice.training.progress.dto.TrainingProgressResponse;
import com.jangchwisa.trainingservice.training.progress.service.TrainingProgressService;
import com.jangchwisa.trainingservice.training.session.entity.TrainingType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TrainingProgressController {

    private final TrainingProgressService trainingProgressService;

    public TrainingProgressController(TrainingProgressService trainingProgressService) {
        this.trainingProgressService = trainingProgressService;
    }

    @GetMapping("/api/trainings/progress")
    public ApiResponse<TrainingProgressResponse> getProgress(
            @AuthenticatedUser CurrentUser currentUser,
            @RequestParam(required = false) String type
    ) {
        TrainingType trainingType = parseTrainingType(type);
        TrainingProgressResponse response = trainingProgressService.getProgress(currentUser.userId(), trainingType);
        return ApiResponse.success(response);
    }

    private TrainingType parseTrainingType(String type) {
        if (type == null || type.isBlank()) {
            return TrainingType.SOCIAL;
        }

        try {
            return TrainingType.valueOf(type.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new TrainingServiceException(ErrorCode.VALIDATION_ERROR, "Training type is invalid.");
        }
    }
}
