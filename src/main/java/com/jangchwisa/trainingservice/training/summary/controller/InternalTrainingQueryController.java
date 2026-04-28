package com.jangchwisa.trainingservice.training.summary.controller;

import com.jangchwisa.trainingservice.common.exception.ErrorCode;
import com.jangchwisa.trainingservice.common.exception.TrainingServiceException;
import com.jangchwisa.trainingservice.common.response.ApiResponse;
import com.jangchwisa.trainingservice.training.summary.dto.InternalTrainingSummaryResponse;
import com.jangchwisa.trainingservice.training.summary.dto.LatestTrainingResultsResponse;
import com.jangchwisa.trainingservice.training.summary.service.InternalTrainingQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InternalTrainingQueryController {

    private final InternalTrainingQueryService internalTrainingQueryService;

    public InternalTrainingQueryController(InternalTrainingQueryService internalTrainingQueryService) {
        this.internalTrainingQueryService = internalTrainingQueryService;
    }

    @GetMapping("/internal/trainings/users/{userId}/summary")
    public ApiResponse<InternalTrainingSummaryResponse> getSummary(@PathVariable long userId) {
        validateUserId(userId);
        return ApiResponse.success(internalTrainingQueryService.getSummary(userId));
    }

    @GetMapping("/internal/trainings/users/{userId}/latest-results")
    public ApiResponse<LatestTrainingResultsResponse> getLatestResults(@PathVariable long userId) {
        validateUserId(userId);
        return ApiResponse.success(internalTrainingQueryService.getLatestResults(userId));
    }

    private void validateUserId(long userId) {
        if (userId <= 0) {
            throw new TrainingServiceException(ErrorCode.VALIDATION_ERROR, "User id must be positive.");
        }
    }
}
