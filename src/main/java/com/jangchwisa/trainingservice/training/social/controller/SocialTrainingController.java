package com.jangchwisa.trainingservice.training.social.controller;

import com.jangchwisa.trainingservice.common.exception.ErrorCode;
import com.jangchwisa.trainingservice.common.exception.TrainingServiceException;
import com.jangchwisa.trainingservice.common.response.ApiResponse;
import com.jangchwisa.trainingservice.common.security.AuthenticatedUser;
import com.jangchwisa.trainingservice.common.security.CurrentUser;
import com.jangchwisa.trainingservice.training.social.dto.CompleteSocialSessionRequest;
import com.jangchwisa.trainingservice.training.social.dto.CompleteSocialSessionResponse;
import com.jangchwisa.trainingservice.training.social.dto.SelectSocialJobTypeRequest;
import com.jangchwisa.trainingservice.training.social.dto.SelectSocialJobTypeResponse;
import com.jangchwisa.trainingservice.training.social.dto.SocialScenarioDetailResponse;
import com.jangchwisa.trainingservice.training.social.dto.SocialScenarioListItemResponse;
import com.jangchwisa.trainingservice.training.social.dto.SocialSessionDetailResponse;
import com.jangchwisa.trainingservice.training.social.dto.StartSocialSessionRequest;
import com.jangchwisa.trainingservice.training.social.dto.StartSocialSessionResponse;
import com.jangchwisa.trainingservice.training.social.entity.SocialJobType;
import com.jangchwisa.trainingservice.training.social.service.SocialTrainingService;
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
public class SocialTrainingController {

    private final SocialTrainingService socialTrainingService;

    public SocialTrainingController(SocialTrainingService socialTrainingService) {
        this.socialTrainingService = socialTrainingService;
    }

    @Operation(
            summary = "사회성 직무 유형 선택",
            description = "사회성 훈련에서 사무직(OFFICE) 또는 단순 노무(LABOR)를 선택하고 다음 화면 정보를 반환합니다."
    )
    @PostMapping("/api/trainings/social/job-type")
    public ApiResponse<SelectSocialJobTypeResponse> selectJobType(
            @AuthenticatedUser CurrentUser currentUser,
            @Valid @RequestBody SelectSocialJobTypeRequest request
    ) {
        return ApiResponse.success(socialTrainingService.selectJobType(parseJobType(request.jobType())));
    }

    @Operation(
            summary = "사회성 시나리오 목록 조회",
            description = "선택한 직무 유형에 맞는 활성 사회성 훈련 시나리오 목록을 조회합니다."
    )
    @GetMapping("/api/trainings/social/scenarios")
    public ApiResponse<List<SocialScenarioListItemResponse>> getScenarios(
            @AuthenticatedUser CurrentUser currentUser,
            @Parameter(description = "조회할 사회성 직무 유형입니다. OFFICE 또는 LABOR를 입력합니다.", example = "OFFICE")
            @RequestParam String jobType
    ) {
        return ApiResponse.success(socialTrainingService.getScenarios(parseJobType(jobType)));
    }

    @Operation(
            summary = "사회성 시나리오 상세 조회",
            description = "선택한 사회성 시나리오의 배경, 상황, 대화 상대 정보를 조회합니다."
    )
    @GetMapping("/api/trainings/social/scenarios/{scenarioId}")
    public ApiResponse<SocialScenarioDetailResponse> getScenarioDetail(
            @AuthenticatedUser CurrentUser currentUser,
            @Parameter(description = "조회할 사회성 시나리오 ID입니다.", example = "1")
            @PathVariable long scenarioId
    ) {
        return ApiResponse.success(socialTrainingService.getScenarioDetail(scenarioId));
    }

    @Operation(
            summary = "사회성 훈련 세션 시작",
            description = "선택한 직무 유형과 시나리오로 사회성 훈련 세션을 생성합니다."
    )
    @PostMapping("/api/trainings/social/sessions")
    public ApiResponse<StartSocialSessionResponse> startSession(
            @AuthenticatedUser CurrentUser currentUser,
            @Valid @RequestBody StartSocialSessionRequest request
    ) {
        return ApiResponse.success(socialTrainingService.startSession(
                currentUser,
                parseJobType(request.jobType()),
                request.scenarioId()
        ));
    }

    @Operation(
            summary = "사회성 훈련 상세 조회",
            description = "완료된 사회성 훈련의 점수, 피드백, 대화 로그를 조회합니다."
    )
    @GetMapping("/api/trainings/social/sessions/{sessionId}/detail")
    public ApiResponse<SocialSessionDetailResponse> getSessionDetail(
            @AuthenticatedUser CurrentUser currentUser,
            @Parameter(description = "조회할 사회성 훈련 세션 ID입니다.", example = "10")
            @PathVariable long sessionId
    ) {
        return ApiResponse.success(socialTrainingService.getSessionDetail(currentUser, sessionId));
    }

    @Operation(
            summary = "사회성 훈련 완료",
            description = "사회성 훈련 대화 로그를 제출하고 점수, 피드백, 진행 요약을 저장한 뒤 완료 처리합니다."
    )
    @PostMapping("/api/trainings/social/sessions/{sessionId}/complete")
    public ApiResponse<CompleteSocialSessionResponse> completeSession(
            @AuthenticatedUser CurrentUser currentUser,
            @Parameter(description = "완료 처리할 사회성 훈련 세션 ID입니다.", example = "10")
            @PathVariable long sessionId,
            @Valid @RequestBody CompleteSocialSessionRequest request
    ) {
        return ApiResponse.success(socialTrainingService.completeSession(currentUser, sessionId, request));
    }

    private SocialJobType parseJobType(String jobType) {
        if (jobType == null || jobType.isBlank()) {
            throw new TrainingServiceException(ErrorCode.VALIDATION_ERROR, "Social job type is required.");
        }

        try {
            return SocialJobType.valueOf(jobType.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new TrainingServiceException(ErrorCode.VALIDATION_ERROR, "Social job type is invalid.");
        }
    }
}
