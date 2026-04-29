package com.jangchwisa.trainingservice.training.progress.controller;

import com.jangchwisa.trainingservice.common.exception.ErrorCode;
import com.jangchwisa.trainingservice.common.exception.TrainingServiceException;
import com.jangchwisa.trainingservice.common.response.ApiResponse;
import com.jangchwisa.trainingservice.common.security.AuthenticatedUser;
import com.jangchwisa.trainingservice.common.security.CurrentUser;
import com.jangchwisa.trainingservice.training.progress.dto.TrainingProgressResponse;
import com.jangchwisa.trainingservice.training.progress.service.TrainingProgressService;
import com.jangchwisa.trainingservice.training.session.entity.TrainingType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TrainingProgressController {

    private final TrainingProgressService trainingProgressService;

    public TrainingProgressController(TrainingProgressService trainingProgressService) {
        this.trainingProgressService = trainingProgressService;
    }

    @Operation(
            summary = "훈련 수준 조회",
            description = "Asia/Seoul 기준 이번 달 완료 이력을 바탕으로 선택한 훈련 유형의 수준을 조회합니다. type이 없으면 SOCIAL 기준으로 조회합니다."
    )
    @GetMapping("/api/trainings/progress")
    public ApiResponse<TrainingProgressResponse> getProgress(
            @AuthenticatedUser CurrentUser currentUser,
            @Parameter(description = "조회할 훈련 유형입니다. SOCIAL, SAFETY, DOCUMENT, FOCUS 중 하나이며 비우면 SOCIAL입니다.", example = "SOCIAL")
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
