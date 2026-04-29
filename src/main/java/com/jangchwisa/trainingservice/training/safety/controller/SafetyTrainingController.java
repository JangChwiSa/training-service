package com.jangchwisa.trainingservice.training.safety.controller;

import com.jangchwisa.trainingservice.common.exception.ErrorCode;
import com.jangchwisa.trainingservice.common.exception.TrainingServiceException;
import com.jangchwisa.trainingservice.common.response.ApiResponse;
import com.jangchwisa.trainingservice.common.security.AuthenticatedUser;
import com.jangchwisa.trainingservice.common.security.CurrentUser;
import com.jangchwisa.trainingservice.training.safety.dto.CompleteSafetySessionResponse;
import com.jangchwisa.trainingservice.training.safety.dto.NextSafetySceneRequest;
import com.jangchwisa.trainingservice.training.safety.dto.NextSafetySceneResponse;
import com.jangchwisa.trainingservice.training.safety.dto.SafetyScenarioListItemResponse;
import com.jangchwisa.trainingservice.training.safety.dto.SafetySessionDetailResponse;
import com.jangchwisa.trainingservice.training.safety.dto.StartSafetySessionRequest;
import com.jangchwisa.trainingservice.training.safety.dto.StartSafetySessionResponse;
import com.jangchwisa.trainingservice.training.safety.entity.SafetyCategory;
import com.jangchwisa.trainingservice.training.safety.service.SafetyTrainingService;
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
            summary = "안전 시나리오 목록 조회",
            description = "안전 훈련 시나리오 목록을 조회합니다. category 값이 있으면 해당 카테고리만 필터링합니다."
    )
    @GetMapping("/api/trainings/safety/scenarios")
    public ApiResponse<List<SafetyScenarioListItemResponse>> getScenarios(
            @AuthenticatedUser CurrentUser currentUser,
            @Parameter(description = "필터링할 안전 카테고리입니다. 비우면 전체를 조회합니다.", example = "COMMUTE_SAFETY")
            @RequestParam(required = false) String category
    ) {
        return ApiResponse.success(safetyTrainingService.getScenarios(parseCategory(category)));
    }

    @Operation(
            summary = "안전 훈련 세션 시작",
            description = "선택한 안전 시나리오로 훈련 세션을 만들고 첫 장면과 선택지를 반환합니다."
    )
    @PostMapping("/api/trainings/safety/sessions")
    public ApiResponse<StartSafetySessionResponse> startSession(
            @AuthenticatedUser CurrentUser currentUser,
            @Valid @RequestBody StartSafetySessionRequest request
    ) {
        return ApiResponse.success(safetyTrainingService.startSession(currentUser, request.scenarioId()));
    }

    @Operation(
            summary = "안전 훈련 다음 장면 진행",
            description = "사용자의 선택을 저장하고 정답 여부와 다음 장면 정보를 반환합니다."
    )
    @PostMapping("/api/trainings/safety/sessions/{sessionId}/next-scene")
    public ApiResponse<NextSafetySceneResponse> nextScene(
            @AuthenticatedUser CurrentUser currentUser,
            @Parameter(description = "진행 중인 안전 훈련 세션 ID입니다.", example = "20")
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
            summary = "안전 훈련 상세 조회",
            description = "완료된 안전 훈련의 점수, 선택 이력, 피드백을 조회합니다."
    )
    @GetMapping("/api/trainings/safety/sessions/{sessionId}/detail")
    public ApiResponse<SafetySessionDetailResponse> getSessionDetail(
            @AuthenticatedUser CurrentUser currentUser,
            @Parameter(description = "조회할 안전 훈련 세션 ID입니다.", example = "20")
            @PathVariable long sessionId
    ) {
        return ApiResponse.success(safetyTrainingService.getSessionDetail(currentUser, sessionId));
    }

    @Operation(
            summary = "안전 훈련 완료",
            description = "저장된 선택 이력을 기준으로 안전 훈련 점수와 진행 요약을 계산하고 완료 처리합니다."
    )
    @PostMapping("/api/trainings/safety/sessions/{sessionId}/complete")
    public ApiResponse<CompleteSafetySessionResponse> completeSession(
            @AuthenticatedUser CurrentUser currentUser,
            @Parameter(description = "완료 처리할 안전 훈련 세션 ID입니다.", example = "20")
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
