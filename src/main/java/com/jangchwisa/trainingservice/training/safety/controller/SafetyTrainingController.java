package com.jangchwisa.trainingservice.training.safety.controller;

import com.jangchwisa.trainingservice.common.exception.ErrorCode;
import com.jangchwisa.trainingservice.common.exception.TrainingServiceException;
import com.jangchwisa.trainingservice.common.response.ApiResponse;
import com.jangchwisa.trainingservice.common.security.AuthenticatedUser;
import com.jangchwisa.trainingservice.common.security.CurrentUser;
import com.jangchwisa.trainingservice.training.safety.dto.NextSafetySceneRequest;
import com.jangchwisa.trainingservice.training.safety.dto.NextSafetySceneResponse;
import com.jangchwisa.trainingservice.training.safety.dto.SafetyScenarioListItemResponse;
import com.jangchwisa.trainingservice.training.safety.dto.SafetySessionDetailResponse;
import com.jangchwisa.trainingservice.training.safety.dto.StartSafetySessionRequest;
import com.jangchwisa.trainingservice.training.safety.dto.StartSafetySessionResponse;
import com.jangchwisa.trainingservice.training.safety.entity.SafetyCategory;
import com.jangchwisa.trainingservice.training.safety.service.SafetyTrainingService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SafetyTrainingController {

    private final SafetyTrainingService safetyTrainingService;

    public SafetyTrainingController(SafetyTrainingService safetyTrainingService) {
        this.safetyTrainingService = safetyTrainingService;
    }

    @GetMapping("/api/trainings/safety/scenarios")
    public ApiResponse<List<SafetyScenarioListItemResponse>> getScenarios(
            @AuthenticatedUser CurrentUser currentUser,
            @RequestParam(required = false) String category
    ) {
        return ApiResponse.success(safetyTrainingService.getScenarios(parseCategory(category)));
    }

    @PostMapping("/api/trainings/safety/sessions")
    public ApiResponse<StartSafetySessionResponse> startSession(
            @AuthenticatedUser CurrentUser currentUser,
            @Valid @RequestBody StartSafetySessionRequest request
    ) {
        return ApiResponse.success(safetyTrainingService.startSession(currentUser, request.scenarioId()));
    }

    @PostMapping("/api/trainings/safety/sessions/{sessionId}/next-scene")
    public ApiResponse<NextSafetySceneResponse> nextScene(
            @AuthenticatedUser CurrentUser currentUser,
            @PathVariable long sessionId,
            @Valid @RequestBody NextSafetySceneRequest request
    ) {
        return ApiResponse.success(safetyTrainingService.nextScene(
                currentUser,
                sessionId,
                request.sceneId(),
                request.choiceId()
        ));
    }

    @GetMapping("/api/trainings/safety/sessions/{sessionId}/detail")
    public ApiResponse<SafetySessionDetailResponse> getSessionDetail(
            @AuthenticatedUser CurrentUser currentUser,
            @PathVariable long sessionId
    ) {
        return ApiResponse.success(safetyTrainingService.getSessionDetail(currentUser, sessionId));
    }

    private SafetyCategory parseCategory(String category) {
        if (category == null || category.isBlank()) {
            return null;
        }

        try {
            return SafetyCategory.valueOf(category.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new TrainingServiceException(ErrorCode.VALIDATION_ERROR, "Safety category is invalid.");
        }
    }
}
