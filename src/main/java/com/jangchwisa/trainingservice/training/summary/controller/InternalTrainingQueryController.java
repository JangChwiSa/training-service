package com.jangchwisa.trainingservice.training.summary.controller;

import com.jangchwisa.trainingservice.common.exception.ErrorCode;
import com.jangchwisa.trainingservice.common.exception.TrainingServiceException;
import com.jangchwisa.trainingservice.common.response.ApiResponse;
import com.jangchwisa.trainingservice.training.summary.dto.InternalTrainingSummaryResponse;
import com.jangchwisa.trainingservice.training.summary.dto.LatestTrainingResultsResponse;
import com.jangchwisa.trainingservice.training.summary.service.InternalTrainingQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InternalTrainingQueryController {

    private final InternalTrainingQueryService internalTrainingQueryService;

    public InternalTrainingQueryController(InternalTrainingQueryService internalTrainingQueryService) {
        this.internalTrainingQueryService = internalTrainingQueryService;
    }

    @Operation(
            summary = "내부 사용자 훈련 요약 조회",
            description = "API Gateway 등 내부 서비스가 내정보 화면 구성을 위해 사용자 훈련 요약을 조회합니다."
    )
    @GetMapping("/internal/trainings/users/{userId}/summary")
    public ApiResponse<InternalTrainingSummaryResponse> getSummary(
            @Parameter(description = "조회 대상 사용자 ID입니다. 내부 서비스 호출에서만 사용합니다.", example = "1")
            @PathVariable long userId
    ) {
        validateUserId(userId);
        return ApiResponse.success(internalTrainingQueryService.getSummary(userId));
    }

    @Operation(
            summary = "내부 최신 훈련 결과 조회",
            description = "Report Service 등 내부 서비스가 리포트 복구나 동기화를 위해 최신 완료 훈련 결과를 조회합니다."
    )
    @GetMapping("/internal/trainings/users/{userId}/latest-results")
    public ApiResponse<LatestTrainingResultsResponse> getLatestResults(
            @Parameter(description = "조회 대상 사용자 ID입니다. 내부 서비스 호출에서만 사용합니다.", example = "1")
            @PathVariable long userId
    ) {
        validateUserId(userId);
        return ApiResponse.success(internalTrainingQueryService.getLatestResults(userId));
    }

    private void validateUserId(long userId) {
        if (userId <= 0) {
            throw new TrainingServiceException(ErrorCode.VALIDATION_ERROR, "User id must be positive.");
        }
    }
}
