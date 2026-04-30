package com.didgo.trainingservice.training.summary.controller;

import com.didgo.trainingservice.common.exception.ErrorCode;
import com.didgo.trainingservice.common.exception.TrainingServiceException;
import com.didgo.trainingservice.common.response.ApiResponse;
import com.didgo.trainingservice.training.summary.dto.InternalTrainingSummaryResponse;
import com.didgo.trainingservice.training.summary.dto.LatestTrainingResultsResponse;
import com.didgo.trainingservice.training.summary.service.InternalTrainingQueryService;
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
            summary = "?대? ?ъ슜???덈젴 ?붿빟 議고쉶",
            description = "API Gateway ???대? ?쒕퉬?ㅺ? ?댁젙蹂??붾㈃ 援ъ꽦???꾪빐 ?ъ슜???덈젴 ?붿빟??議고쉶?⑸땲??"
    )
    @GetMapping("/internal/trainings/users/{userId}/summary")
    public ApiResponse<InternalTrainingSummaryResponse> getSummary(
            @Parameter(description = "議고쉶 ????ъ슜??ID?낅땲?? ?대? ?쒕퉬???몄텧?먯꽌留??ъ슜?⑸땲??", example = "1")
            @PathVariable long userId
    ) {
        validateUserId(userId);
        return ApiResponse.success(internalTrainingQueryService.getSummary(userId));
    }

    @Operation(
            summary = "?대? 理쒖떊 ?덈젴 寃곌낵 議고쉶",
            description = "Report Service ???대? ?쒕퉬?ㅺ? 由ы룷??蹂듦뎄???숆린?붾? ?꾪빐 理쒖떊 ?꾨즺 ?덈젴 寃곌낵瑜?議고쉶?⑸땲??"
    )
    @GetMapping("/internal/trainings/users/{userId}/latest-results")
    public ApiResponse<LatestTrainingResultsResponse> getLatestResults(
            @Parameter(description = "議고쉶 ????ъ슜??ID?낅땲?? ?대? ?쒕퉬???몄텧?먯꽌留??ъ슜?⑸땲??", example = "1")
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
