package com.didgo.trainingservice.training.social.controller;

import com.didgo.trainingservice.common.exception.ErrorCode;
import com.didgo.trainingservice.common.exception.TrainingServiceException;
import com.didgo.trainingservice.common.response.ApiResponse;
import com.didgo.trainingservice.common.security.AuthenticatedUser;
import com.didgo.trainingservice.common.security.CurrentUser;
import com.didgo.trainingservice.training.social.dto.CompleteSocialSessionRequest;
import com.didgo.trainingservice.training.social.dto.CompleteSocialSessionResponse;
import com.didgo.trainingservice.training.social.dto.GenerateSocialAdaptiveScenarioRequest;
import com.didgo.trainingservice.training.social.dto.SelectSocialJobTypeRequest;
import com.didgo.trainingservice.training.social.dto.SelectSocialJobTypeResponse;
import com.didgo.trainingservice.training.social.dto.SocialAdaptiveScenarioResponse;
import com.didgo.trainingservice.training.social.dto.SocialScenarioDetailResponse;
import com.didgo.trainingservice.training.social.dto.SocialScenarioListItemResponse;
import com.didgo.trainingservice.training.social.dto.SocialSessionDetailResponse;
import com.didgo.trainingservice.training.social.dto.StartSocialSessionRequest;
import com.didgo.trainingservice.training.social.dto.StartSocialSessionResponse;
import com.didgo.trainingservice.training.social.entity.SocialJobType;
import com.didgo.trainingservice.training.social.service.SocialTrainingService;
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
            summary = "?ы쉶??吏곷Т ?좏삎 ?좏깮",
            description = "?ы쉶???덈젴?먯꽌 ?щТ吏?OFFICE) ?먮뒗 ?⑥닚 ?몃Т(LABOR)瑜??좏깮?섍퀬 ?ㅼ쓬 ?붾㈃ ?뺣낫瑜?諛섑솚?⑸땲??"
    )
    @PostMapping("/api/trainings/social/job-type")
    public ApiResponse<SelectSocialJobTypeResponse> selectJobType(
            @AuthenticatedUser CurrentUser currentUser,
            @Valid @RequestBody SelectSocialJobTypeRequest request
    ) {
        return ApiResponse.success(socialTrainingService.selectJobType(parseJobType(request.jobType())));
    }

    @Operation(
            summary = "?ы쉶???쒕굹由ъ삤 紐⑸줉 議고쉶",
            description = "?좏깮??吏곷Т ?좏삎??留욌뒗 ?쒖꽦 ?ы쉶???덈젴 ?쒕굹由ъ삤 紐⑸줉??議고쉶?⑸땲??"
    )
    @GetMapping("/api/trainings/social/scenarios")
    public ApiResponse<List<SocialScenarioListItemResponse>> getScenarios(
            @AuthenticatedUser CurrentUser currentUser,
            @Parameter(description = "議고쉶???ы쉶??吏곷Т ?좏삎?낅땲?? OFFICE ?먮뒗 LABOR瑜??낅젰?⑸땲??", example = "OFFICE")
            @RequestParam String jobType
    ) {
        return ApiResponse.success(socialTrainingService.getScenarios(parseJobType(jobType)));
    }

    @Operation(
            summary = "?ы쉶???쒕굹由ъ삤 ?곸꽭 議고쉶",
            description = "?좏깮???ы쉶???쒕굹由ъ삤??諛곌꼍, ?곹솴, ????곷? ?뺣낫瑜?議고쉶?⑸땲??"
    )
    @GetMapping("/api/trainings/social/scenarios/{scenarioId}")
    public ApiResponse<SocialScenarioDetailResponse> getScenarioDetail(
            @AuthenticatedUser CurrentUser currentUser,
            @Parameter(description = "議고쉶???ы쉶???쒕굹由ъ삤 ID?낅땲??", example = "1")
            @PathVariable long scenarioId
    ) {
        return ApiResponse.success(socialTrainingService.getScenarioDetail(currentUser, scenarioId));
    }

    @Operation(
            summary = "Generate adaptive social scenario",
            description = "Analyzes recent social training history and creates a user-specific scenario for weak-point practice."
    )
    @PostMapping("/api/trainings/social/adaptive-scenarios")
    public ApiResponse<SocialAdaptiveScenarioResponse> generateAdaptiveScenario(
            @AuthenticatedUser CurrentUser currentUser,
            @Valid @RequestBody GenerateSocialAdaptiveScenarioRequest request
    ) {
        return ApiResponse.success(socialTrainingService.generateAdaptiveScenario(
                currentUser,
                parseJobType(request.jobType())
        ));
    }

    @Operation(
            summary = "?ы쉶???덈젴 ?몄뀡 ?쒖옉",
            description = "?좏깮??吏곷Т ?좏삎怨??쒕굹由ъ삤濡??ы쉶???덈젴 ?몄뀡???앹꽦?⑸땲??"
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
            summary = "?ы쉶???덈젴 ?곸꽭 議고쉶",
            description = "?꾨즺???ы쉶???덈젴???먯닔, ?쇰뱶諛? ???濡쒓렇瑜?議고쉶?⑸땲??"
    )
    @GetMapping("/api/trainings/social/sessions/{sessionId}/detail")
    public ApiResponse<SocialSessionDetailResponse> getSessionDetail(
            @AuthenticatedUser CurrentUser currentUser,
            @Parameter(description = "議고쉶???ы쉶???덈젴 ?몄뀡 ID?낅땲??", example = "10")
            @PathVariable long sessionId
    ) {
        return ApiResponse.success(socialTrainingService.getSessionDetail(currentUser, sessionId));
    }

    @Operation(
            summary = "?ы쉶???덈젴 ?꾨즺",
            description = "?ы쉶???덈젴 ???濡쒓렇瑜??쒖텧?섍퀬 ?먯닔, ?쇰뱶諛? 吏꾪뻾 ?붿빟????ν븳 ???꾨즺 泥섎━?⑸땲??"
    )
    @PostMapping("/api/trainings/social/sessions/{sessionId}/complete")
    public ApiResponse<CompleteSocialSessionResponse> completeSession(
            @AuthenticatedUser CurrentUser currentUser,
            @Parameter(description = "?꾨즺 泥섎━???ы쉶???덈젴 ?몄뀡 ID?낅땲??", example = "10")
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
