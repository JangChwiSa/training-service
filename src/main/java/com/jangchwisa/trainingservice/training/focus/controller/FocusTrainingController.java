package com.jangchwisa.trainingservice.training.focus.controller;

import com.jangchwisa.trainingservice.common.response.ApiResponse;
import com.jangchwisa.trainingservice.common.security.AuthenticatedUser;
import com.jangchwisa.trainingservice.common.security.CurrentUser;
import com.jangchwisa.trainingservice.training.focus.dto.CompleteFocusSessionRequest;
import com.jangchwisa.trainingservice.training.focus.dto.CompleteFocusSessionResponse;
import com.jangchwisa.trainingservice.training.focus.dto.FocusProgressResponse;
import com.jangchwisa.trainingservice.training.focus.dto.StartFocusSessionRequest;
import com.jangchwisa.trainingservice.training.focus.dto.StartFocusSessionResponse;
import com.jangchwisa.trainingservice.training.focus.service.FocusTrainingService;
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
            summary = "집중력 진행 상태 조회",
            description = "사용자의 현재 집중력 훈련 단계, 해금 단계, 최근 수행 결과를 조회합니다."
    )
    @GetMapping("/api/trainings/focus/progress")
    public ApiResponse<FocusProgressResponse> getProgress(@AuthenticatedUser CurrentUser currentUser) {
        return ApiResponse.success(focusTrainingService.getProgress(currentUser.userId()));
    }

    @Operation(
            summary = "집중력 훈련 세션 시작",
            description = "선택한 단계로 집중력 훈련 세션을 생성하고 수행할 지시 목록을 반환합니다."
    )
    @PostMapping("/api/trainings/focus/sessions")
    public ApiResponse<StartFocusSessionResponse> startSession(
            @AuthenticatedUser CurrentUser currentUser,
            @Valid @RequestBody StartFocusSessionRequest request
    ) {
        return ApiResponse.success(focusTrainingService.startSession(currentUser, request.level()));
    }

    @Operation(
            summary = "집중력 훈련 완료",
            description = "사용자의 반응 로그를 제출하고 정확도, 평균 반응속도, 점수와 해금 상태를 계산합니다."
    )
    @PostMapping("/api/trainings/focus/sessions/{sessionId}/complete")
    public ApiResponse<CompleteFocusSessionResponse> completeSession(
            @AuthenticatedUser CurrentUser currentUser,
            @Parameter(description = "완료 처리할 집중력 훈련 세션 ID입니다.", example = "40")
            @PathVariable long sessionId,
            @Valid @RequestBody CompleteFocusSessionRequest request
    ) {
        return ApiResponse.success(focusTrainingService.completeSession(currentUser, sessionId, request));
    }
}
