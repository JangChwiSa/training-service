package com.jangchwisa.trainingservice.training.document.controller;

import com.jangchwisa.trainingservice.common.response.ApiResponse;
import com.jangchwisa.trainingservice.common.security.AuthenticatedUser;
import com.jangchwisa.trainingservice.common.security.CurrentUser;
import com.jangchwisa.trainingservice.training.document.dto.DocumentSessionDetailResponse;
import com.jangchwisa.trainingservice.training.document.dto.StartDocumentSessionRequest;
import com.jangchwisa.trainingservice.training.document.dto.StartDocumentSessionResponse;
import com.jangchwisa.trainingservice.training.document.dto.SubmitDocumentAnswersRequest;
import com.jangchwisa.trainingservice.training.document.dto.SubmitDocumentAnswersResponse;
import com.jangchwisa.trainingservice.training.document.service.DocumentTrainingService;
import com.jangchwisa.trainingservice.training.progress.dto.DocumentProgressResponse;
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
            summary = "문서 이해 진행 상태 조회",
            description = "사용자의 현재 문서 이해 훈련 레벨, 해금 레벨, 최근 수행 결과를 조회합니다."
    )
    @GetMapping("/api/trainings/document/progress")
    public ApiResponse<DocumentProgressResponse> getProgress(@AuthenticatedUser CurrentUser currentUser) {
        return ApiResponse.success(documentTrainingService.getProgress(currentUser.userId()));
    }

    @Operation(
            summary = "문서 이해 훈련 세션 시작",
            description = "문서 이해 훈련 세션을 생성하고 활성 문서 문제 목록을 반환합니다."
    )
    @PostMapping("/api/trainings/document/sessions")
    public ApiResponse<StartDocumentSessionResponse> startSession(
            @AuthenticatedUser CurrentUser currentUser,
            @Valid @org.springframework.web.bind.annotation.RequestBody StartDocumentSessionRequest request
    ) {
        return ApiResponse.success(documentTrainingService.startSession(currentUser, request));
    }

    @Operation(
            summary = "문서 이해 훈련 상세 조회",
            description = "완료된 문서 이해 훈련의 점수, 문제별 답변 결과, 정답과 해설을 조회합니다."
    )
    @GetMapping("/api/trainings/document/sessions/{sessionId}/detail")
    public ApiResponse<DocumentSessionDetailResponse> getSessionDetail(
            @AuthenticatedUser CurrentUser currentUser,
            @Parameter(description = "조회할 문서 이해 훈련 세션 ID입니다.", example = "50")
            @PathVariable long sessionId
    ) {
        return ApiResponse.success(documentTrainingService.getSessionDetail(currentUser, sessionId));
    }

    @Operation(
            summary = "문서 이해 답변 제출",
            description = "문서 이해 훈련 답변을 제출하고 채점, 진행 요약 저장, 세션 완료 처리를 수행합니다."
    )
    @PostMapping("/api/trainings/document/sessions/{sessionId}/answers")
    public ApiResponse<SubmitDocumentAnswersResponse> submitAnswers(
            @AuthenticatedUser CurrentUser currentUser,
            @Parameter(description = "답변을 제출할 문서 이해 훈련 세션 ID입니다.", example = "50")
            @PathVariable long sessionId,
            @Valid @org.springframework.web.bind.annotation.RequestBody SubmitDocumentAnswersRequest request
    ) {
        return ApiResponse.success(documentTrainingService.submitAnswers(currentUser, sessionId, request));
    }
}
