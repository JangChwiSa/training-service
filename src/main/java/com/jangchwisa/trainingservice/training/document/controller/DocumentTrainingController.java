package com.jangchwisa.trainingservice.training.document.controller;

import com.jangchwisa.trainingservice.common.response.ApiResponse;
import com.jangchwisa.trainingservice.common.security.AuthenticatedUser;
import com.jangchwisa.trainingservice.common.security.CurrentUser;
import com.jangchwisa.trainingservice.training.document.dto.DocumentSessionDetailResponse;
import com.jangchwisa.trainingservice.training.document.dto.StartDocumentSessionResponse;
import com.jangchwisa.trainingservice.training.document.service.DocumentTrainingService;
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

    @PostMapping("/api/trainings/document/sessions")
    public ApiResponse<StartDocumentSessionResponse> startSession(@AuthenticatedUser CurrentUser currentUser) {
        return ApiResponse.success(documentTrainingService.startSession(currentUser));
    }

    @GetMapping("/api/trainings/document/sessions/{sessionId}/detail")
    public ApiResponse<DocumentSessionDetailResponse> getSessionDetail(
            @AuthenticatedUser CurrentUser currentUser,
            @PathVariable long sessionId
    ) {
        return ApiResponse.success(documentTrainingService.getSessionDetail(currentUser, sessionId));
    }
}
