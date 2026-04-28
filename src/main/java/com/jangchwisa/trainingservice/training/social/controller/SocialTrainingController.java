package com.jangchwisa.trainingservice.training.social.controller;

import com.jangchwisa.trainingservice.common.exception.ErrorCode;
import com.jangchwisa.trainingservice.common.exception.TrainingServiceException;
import com.jangchwisa.trainingservice.common.response.ApiResponse;
import com.jangchwisa.trainingservice.common.security.AuthenticatedUser;
import com.jangchwisa.trainingservice.common.security.CurrentUser;
import com.jangchwisa.trainingservice.training.social.dto.SelectSocialJobTypeRequest;
import com.jangchwisa.trainingservice.training.social.dto.SelectSocialJobTypeResponse;
import com.jangchwisa.trainingservice.training.social.dto.SocialScenarioDetailResponse;
import com.jangchwisa.trainingservice.training.social.dto.SocialScenarioListItemResponse;
import com.jangchwisa.trainingservice.training.social.dto.SocialSessionDetailResponse;
import com.jangchwisa.trainingservice.training.social.dto.StartSocialSessionRequest;
import com.jangchwisa.trainingservice.training.social.dto.StartSocialSessionResponse;
import com.jangchwisa.trainingservice.training.social.entity.SocialJobType;
import com.jangchwisa.trainingservice.training.social.service.SocialTrainingService;
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

    @PostMapping("/api/trainings/social/job-type")
    public ApiResponse<SelectSocialJobTypeResponse> selectJobType(
            @AuthenticatedUser CurrentUser currentUser,
            @Valid @RequestBody SelectSocialJobTypeRequest request
    ) {
        return ApiResponse.success(socialTrainingService.selectJobType(parseJobType(request.jobType())));
    }

    @GetMapping("/api/trainings/social/scenarios")
    public ApiResponse<List<SocialScenarioListItemResponse>> getScenarios(
            @AuthenticatedUser CurrentUser currentUser,
            @RequestParam String jobType
    ) {
        return ApiResponse.success(socialTrainingService.getScenarios(parseJobType(jobType)));
    }

    @GetMapping("/api/trainings/social/scenarios/{scenarioId}")
    public ApiResponse<SocialScenarioDetailResponse> getScenarioDetail(
            @AuthenticatedUser CurrentUser currentUser,
            @PathVariable long scenarioId
    ) {
        return ApiResponse.success(socialTrainingService.getScenarioDetail(scenarioId));
    }

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

    @GetMapping("/api/trainings/social/sessions/{sessionId}/detail")
    public ApiResponse<SocialSessionDetailResponse> getSessionDetail(
            @AuthenticatedUser CurrentUser currentUser,
            @PathVariable long sessionId
    ) {
        return ApiResponse.success(socialTrainingService.getSessionDetail(currentUser, sessionId));
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
