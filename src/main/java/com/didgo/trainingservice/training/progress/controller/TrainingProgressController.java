package com.didgo.trainingservice.training.progress.controller;

import com.didgo.trainingservice.common.exception.ErrorCode;
import com.didgo.trainingservice.common.exception.TrainingServiceException;
import com.didgo.trainingservice.common.response.ApiResponse;
import com.didgo.trainingservice.common.security.AuthenticatedUser;
import com.didgo.trainingservice.common.security.CurrentUser;
import com.didgo.trainingservice.training.progress.dto.TrainingProgressResponse;
import com.didgo.trainingservice.training.progress.dto.TrainingProgressSummaryResponse;
import com.didgo.trainingservice.training.progress.service.TrainingProgressService;
import com.didgo.trainingservice.training.session.entity.TrainingType;
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
            summary = "?덈젴 ?섏? 議고쉶",
            description = "Asia/Seoul 湲곗? ?대쾲 ???꾨즺 ?대젰??諛뷀깢?쇰줈 ?좏깮???덈젴 ?좏삎???섏???議고쉶?⑸땲?? type???놁쑝硫?SOCIAL 湲곗??쇰줈 議고쉶?⑸땲??"
    )
    @GetMapping("/api/trainings/progress")
    public ApiResponse<TrainingProgressResponse> getProgress(
            @AuthenticatedUser CurrentUser currentUser,
            @Parameter(description = "議고쉶???덈젴 ?좏삎?낅땲?? SOCIAL, SAFETY, DOCUMENT, FOCUS 以??섎굹?대ŉ 鍮꾩슦硫?SOCIAL?낅땲??", example = "SOCIAL")
            @RequestParam(required = false) String type
    ) {
        TrainingType trainingType = parseTrainingType(type);
        TrainingProgressResponse response = trainingProgressService.getProgress(currentUser.userId(), trainingType);
        return ApiResponse.success(response);
    }

    @Operation(
            summary = "???붾㈃ ?덈젴 ?섏? ?붿빟 議고쉶",
            description = "Asia/Seoul 湲곗? ?대쾲 ???꾨즺 ?대젰??諛뷀깢?쇰줈 ???붾㈃???쒖떆??紐⑤뱺 ?덈젴 ?좏삎???섏?????踰덉뿉 議고쉶?⑸땲??"
    )
    @GetMapping("/api/trainings/progress/summary")
    public ApiResponse<TrainingProgressSummaryResponse> getProgressSummary(
            @AuthenticatedUser CurrentUser currentUser
    ) {
        TrainingProgressSummaryResponse response = trainingProgressService.getProgressSummary(currentUser.userId());
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
