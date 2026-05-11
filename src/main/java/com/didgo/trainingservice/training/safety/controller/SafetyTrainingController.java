package com.didgo.trainingservice.training.safety.controller;

import com.didgo.trainingservice.common.exception.ErrorCode;
import com.didgo.trainingservice.common.exception.TrainingServiceException;
import com.didgo.trainingservice.common.response.ApiResponse;
import com.didgo.trainingservice.common.security.AuthenticatedUser;
import com.didgo.trainingservice.common.security.CurrentUser;
import com.didgo.trainingservice.training.safety.dto.AdvanceSafetySceneRequest;
import com.didgo.trainingservice.training.safety.dto.CompleteSafetySessionResponse;
import com.didgo.trainingservice.training.safety.dto.NextSafetySceneRequest;
import com.didgo.trainingservice.training.safety.dto.NextSafetySceneResponse;
import com.didgo.trainingservice.training.safety.dto.SafetyScenarioListItemResponse;
import com.didgo.trainingservice.training.safety.dto.SafetySessionDetailResponse;
import com.didgo.trainingservice.training.safety.dto.StartSafetySessionRequest;
import com.didgo.trainingservice.training.safety.dto.StartSafetySessionResponse;
import com.didgo.trainingservice.training.safety.entity.SafetyCategory;
import com.didgo.trainingservice.training.safety.service.SafetyTrainingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

    @Operation(
            summary = "Get safety scenarios",
            description = "Returns safety training scenarios. If category is omitted, all scenarios are returned."
    )
    @GetMapping("/api/trainings/safety/scenarios")
    public ApiResponse<List<SafetyScenarioListItemResponse>> getScenarios(
            @AuthenticatedUser CurrentUser currentUser,
            @Parameter(description = "Optional safety category filter.", example = "COMMUTE_SAFETY")
            @RequestParam(required = false) String category
    ) {
        return ApiResponse.success(safetyTrainingService.getScenarios(parseCategory(category)));
    }

    @Operation(
            summary = "Start safety session",
            description = "Starts a safety training session from the selected scenario."
    )
    @PostMapping("/api/trainings/safety/sessions")
    public ApiResponse<StartSafetySessionResponse> startSession(
            @AuthenticatedUser CurrentUser currentUser,
            @Valid @RequestBody StartSafetySessionRequest request
    ) {
        return ApiResponse.success(safetyTrainingService.startSession(currentUser, request.scenarioId()));
    }

    @Operation(
            summary = "Move to next safety scene",
            description = "Processes the selected choice and returns the next scene."
    )
    @PostMapping("/api/trainings/safety/sessions/{sessionId}/next-scene")
    public ApiResponse<NextSafetySceneResponse> nextScene(
            @AuthenticatedUser CurrentUser currentUser,
            @Parameter(description = "Safety session ID.", example = "20")
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

    @Operation(
            summary = "Advance safety narrative scene",
            description = "Returns the next safety scene without storing a choice log. Use this for narrative scenes with no choices."
    )
    @PostMapping("/api/trainings/safety/sessions/{sessionId}/advance-scene")
    public ApiResponse<NextSafetySceneResponse> advanceScene(
            @AuthenticatedUser CurrentUser currentUser,
            @Parameter(description = "Safety session ID.", example = "20")
            @PathVariable long sessionId,
            @Valid @RequestBody AdvanceSafetySceneRequest request
    ) {
        return ApiResponse.success(safetyTrainingService.advanceScene(currentUser, sessionId, request.sceneId()));
    }

    @Operation(
            summary = "Get safety session detail",
            description = "Returns score, actions, and feedback for a safety session."
    )
    @GetMapping("/api/trainings/safety/sessions/{sessionId}/detail")
    public ApiResponse<SafetySessionDetailResponse> getSessionDetail(
            @AuthenticatedUser CurrentUser currentUser,
            @Parameter(description = "Safety session ID.", example = "20")
            @PathVariable long sessionId
    ) {
        return ApiResponse.success(safetyTrainingService.getSessionDetail(currentUser, sessionId));
    }

    @Operation(
            summary = "Complete safety session",
            description = "Completes the safety session and returns the final result."
    )
    @PostMapping("/api/trainings/safety/sessions/{sessionId}/complete")
    public ApiResponse<CompleteSafetySessionResponse> completeSession(
            @AuthenticatedUser CurrentUser currentUser,
            @Parameter(description = "Safety session ID.", example = "20")
            @PathVariable long sessionId
    ) {
        return ApiResponse.success(safetyTrainingService.completeSession(currentUser, sessionId));
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
