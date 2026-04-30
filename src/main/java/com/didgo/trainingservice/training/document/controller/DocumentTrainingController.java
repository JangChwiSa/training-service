package com.didgo.trainingservice.training.document.controller;

import com.didgo.trainingservice.common.response.ApiResponse;
import com.didgo.trainingservice.common.security.AuthenticatedUser;
import com.didgo.trainingservice.common.security.CurrentUser;
import com.didgo.trainingservice.training.document.dto.DocumentSessionDetailResponse;
import com.didgo.trainingservice.training.document.dto.StartDocumentSessionRequest;
import com.didgo.trainingservice.training.document.dto.StartDocumentSessionResponse;
import com.didgo.trainingservice.training.document.dto.SubmitDocumentAnswersRequest;
import com.didgo.trainingservice.training.document.dto.SubmitDocumentAnswersResponse;
import com.didgo.trainingservice.training.document.service.DocumentTrainingService;
import com.didgo.trainingservice.training.progress.dto.DocumentProgressResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DocumentTrainingController {

    private final DocumentTrainingService documentTrainingService;

    public DocumentTrainingController(DocumentTrainingService documentTrainingService) {
        this.documentTrainingService = documentTrainingService;
    }

    @Operation(
            summary = "臾몄꽌 ?댄빐 吏꾪뻾 ?곹깭 議고쉶",
            description = "?ъ슜?먯쓽 ?꾩옱 臾몄꽌 ?댄빐 ?덈젴 ?덈꺼, ?닿툑 ?덈꺼, 理쒓렐 ?섑뻾 寃곌낵瑜?議고쉶?⑸땲??"
    )
    @GetMapping("/api/trainings/document/progress")
    public ApiResponse<DocumentProgressResponse> getProgress(@AuthenticatedUser CurrentUser currentUser) {
        return ApiResponse.success(documentTrainingService.getProgress(currentUser.userId()));
    }

    @Operation(
            summary = "臾몄꽌 ?댄빐 ?덈젴 ?몄뀡 ?쒖옉",
            description = "臾몄꽌 ?댄빐 ?덈젴 ?몄뀡???앹꽦?섍퀬 ?쒖꽦 臾몄꽌 臾몄젣 紐⑸줉??諛섑솚?⑸땲??"
    )
    @PostMapping("/api/trainings/document/sessions")
    public ApiResponse<StartDocumentSessionResponse> startSession(
            @AuthenticatedUser CurrentUser currentUser,
            @Valid @org.springframework.web.bind.annotation.RequestBody StartDocumentSessionRequest request
    ) {
        return ApiResponse.success(documentTrainingService.startSession(currentUser, request));
    }

    @Operation(
            summary = "臾몄꽌 ?댄빐 ?덈젴 ?곸꽭 議고쉶",
            description = "?꾨즺??臾몄꽌 ?댄빐 ?덈젴???먯닔, 臾몄젣蹂??듬? 寃곌낵, ?뺣떟怨??댁꽕??議고쉶?⑸땲??"
    )
    @GetMapping("/api/trainings/document/sessions/{sessionId}/detail")
    public ApiResponse<DocumentSessionDetailResponse> getSessionDetail(
            @AuthenticatedUser CurrentUser currentUser,
            @Parameter(description = "議고쉶??臾몄꽌 ?댄빐 ?덈젴 ?몄뀡 ID?낅땲??", example = "50")
            @PathVariable long sessionId
    ) {
        return ApiResponse.success(documentTrainingService.getSessionDetail(currentUser, sessionId));
    }

    @Operation(
            summary = "臾몄꽌 ?댄빐 ?듬? ?쒖텧",
            description = "臾몄꽌 ?댄빐 ?덈젴 ?듬????쒖텧?섍퀬 梨꾩젏, 吏꾪뻾 ?붿빟 ??? ?몄뀡 ?꾨즺 泥섎━瑜??섑뻾?⑸땲??"
    )
    @PostMapping("/api/trainings/document/sessions/{sessionId}/answers")
    public ApiResponse<SubmitDocumentAnswersResponse> submitAnswers(
            @AuthenticatedUser CurrentUser currentUser,
            @Parameter(description = "?듬????쒖텧??臾몄꽌 ?댄빐 ?덈젴 ?몄뀡 ID?낅땲??", example = "50")
            @PathVariable long sessionId,
            @Valid @org.springframework.web.bind.annotation.RequestBody SubmitDocumentAnswersRequest request
    ) {
        return ApiResponse.success(documentTrainingService.submitAnswers(currentUser, sessionId, request));
    }
}
