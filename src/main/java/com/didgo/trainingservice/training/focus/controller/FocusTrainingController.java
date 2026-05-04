package com.didgo.trainingservice.training.focus.controller;

import com.didgo.trainingservice.common.response.ApiResponse;
import com.didgo.trainingservice.common.security.AuthenticatedUser;
import com.didgo.trainingservice.common.security.CurrentUser;
import com.didgo.trainingservice.training.focus.dto.CompleteFocusSessionRequest;
import com.didgo.trainingservice.training.focus.dto.CompleteFocusSessionResponse;
import com.didgo.trainingservice.training.focus.dto.FocusProgressResponse;
import com.didgo.trainingservice.training.focus.dto.StartFocusSessionRequest;
import com.didgo.trainingservice.training.focus.dto.StartFocusSessionResponse;
import com.didgo.trainingservice.training.focus.service.FocusTrainingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FocusTrainingController {

    private final FocusTrainingService focusTrainingService;

    public FocusTrainingController(FocusTrainingService focusTrainingService) {
        this.focusTrainingService = focusTrainingService;
    }

    @Operation(
            summary = "吏묒쨷??吏꾪뻾 ?곹깭 議고쉶",
            description = "?ъ슜?먯쓽 ?꾩옱 吏묒쨷???덈젴 ?④퀎, ?닿툑 ?④퀎, 理쒓렐 ?섑뻾 寃곌낵瑜?議고쉶?⑸땲??"
    )
    @GetMapping("/api/trainings/focus/progress")
    public ApiResponse<FocusProgressResponse> getProgress(@AuthenticatedUser CurrentUser currentUser) {
        return ApiResponse.success(focusTrainingService.getProgress(currentUser.userId()));
    }

    @Operation(
            summary = "吏묒쨷???덈젴 ?몄뀡 ?쒖옉",
            description = "?좏깮???④퀎濡?吏묒쨷???덈젴 ?몄뀡???앹꽦?섍퀬 ?섑뻾??吏??紐⑸줉??諛섑솚?⑸땲??"
    )
    @PostMapping("/api/trainings/focus/sessions")
    public ApiResponse<StartFocusSessionResponse> startSession(
            @AuthenticatedUser CurrentUser currentUser,
            @Valid @RequestBody StartFocusSessionRequest request
    ) {
        return ApiResponse.success(focusTrainingService.startSession(currentUser, request.level()));
    }

    @Operation(
            summary = "吏묒쨷???덈젴 ?꾨즺",
            description = "?ъ슜?먯쓽 諛섏쓳 濡쒓렇瑜??쒖텧?섍퀬 ?뺥솗?? ?됯퇏 諛섏쓳?띾룄, ?먯닔? ?닿툑 ?곹깭瑜?怨꾩궛?⑸땲??"
    )
    @PostMapping("/api/trainings/focus/sessions/{sessionId}/complete")
    public ApiResponse<CompleteFocusSessionResponse> completeSession(
            @AuthenticatedUser CurrentUser currentUser,
            @Parameter(description = "?꾨즺 泥섎━??吏묒쨷???덈젴 ?몄뀡 ID?낅땲??", example = "40")
            @PathVariable long sessionId,
            @Valid @RequestBody CompleteFocusSessionRequest request
    ) {
        return ApiResponse.success(focusTrainingService.completeSession(currentUser, sessionId, request));
    }
}
