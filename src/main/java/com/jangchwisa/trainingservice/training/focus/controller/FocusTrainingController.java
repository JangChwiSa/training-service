package com.jangchwisa.trainingservice.training.focus.controller;

import com.jangchwisa.trainingservice.common.response.ApiResponse;
import com.jangchwisa.trainingservice.common.security.AuthenticatedUser;
import com.jangchwisa.trainingservice.common.security.CurrentUser;
import com.jangchwisa.trainingservice.training.focus.dto.FocusProgressResponse;
import com.jangchwisa.trainingservice.training.focus.dto.StartFocusSessionRequest;
import com.jangchwisa.trainingservice.training.focus.dto.StartFocusSessionResponse;
import com.jangchwisa.trainingservice.training.focus.service.FocusTrainingService;
import jakarta.validation.Valid;
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

    @GetMapping("/api/trainings/focus/progress")
    public ApiResponse<FocusProgressResponse> getProgress(@AuthenticatedUser CurrentUser currentUser) {
        return ApiResponse.success(focusTrainingService.getProgress(currentUser.userId()));
    }

    @PostMapping("/api/trainings/focus/sessions")
    public ApiResponse<StartFocusSessionResponse> startSession(
            @AuthenticatedUser CurrentUser currentUser,
            @Valid @RequestBody StartFocusSessionRequest request
    ) {
        return ApiResponse.success(focusTrainingService.startSession(currentUser, request.level()));
    }
}
