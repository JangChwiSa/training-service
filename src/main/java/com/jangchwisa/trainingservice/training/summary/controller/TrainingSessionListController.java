package com.jangchwisa.trainingservice.training.summary.controller;

import com.jangchwisa.trainingservice.common.exception.ErrorCode;
import com.jangchwisa.trainingservice.common.exception.TrainingServiceException;
import com.jangchwisa.trainingservice.common.response.ApiResponse;
import com.jangchwisa.trainingservice.common.security.AuthenticatedUser;
import com.jangchwisa.trainingservice.common.security.CurrentUser;
import com.jangchwisa.trainingservice.training.session.entity.TrainingType;
import com.jangchwisa.trainingservice.training.summary.dto.TrainingSessionListResponse;
import com.jangchwisa.trainingservice.training.summary.service.TrainingSessionListService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TrainingSessionListController {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 100;

    private final TrainingSessionListService trainingSessionListService;

    public TrainingSessionListController(TrainingSessionListService trainingSessionListService) {
        this.trainingSessionListService = trainingSessionListService;
    }

    @Operation(
            summary = "훈련 기록 목록 조회",
            description = "선택한 훈련 유형의 완료 이력을 최신순으로 조회합니다."
    )
    @GetMapping("/api/trainings/sessions")
    public ApiResponse<TrainingSessionListResponse> getSessions(
            @AuthenticatedUser CurrentUser currentUser,
            @Parameter(description = "조회할 훈련 유형입니다. SOCIAL, SAFETY, DOCUMENT, FOCUS 중 하나를 입력합니다.", example = "SOCIAL")
            @RequestParam String type,
            @Parameter(description = "페이지 번호입니다. 0부터 시작합니다.", example = "0")
            @RequestParam(required = false) Integer page,
            @Parameter(description = "페이지당 항목 수입니다. 1부터 100까지 입력할 수 있습니다.", example = "10")
            @RequestParam(required = false) Integer size
    ) {
        TrainingType trainingType = parseTrainingType(type);
        int pageNumber = normalizePage(page);
        int pageSize = normalizeSize(size);

        TrainingSessionListResponse response = trainingSessionListService.getSessions(
                currentUser.userId(),
                trainingType,
                pageNumber,
                pageSize
        );
        return ApiResponse.success(response);
    }

    private TrainingType parseTrainingType(String type) {
        if (type == null || type.isBlank()) {
            throw new TrainingServiceException(ErrorCode.VALIDATION_ERROR, "Training type is required.");
        }

        try {
            return TrainingType.valueOf(type.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new TrainingServiceException(ErrorCode.VALIDATION_ERROR, "Training type is invalid.");
        }
    }

    private int normalizePage(Integer page) {
        if (page == null) {
            return DEFAULT_PAGE;
        }
        if (page < 0) {
            throw new TrainingServiceException(ErrorCode.VALIDATION_ERROR, "Page must not be negative.");
        }
        return page;
    }

    private int normalizeSize(Integer size) {
        if (size == null) {
            return DEFAULT_SIZE;
        }
        if (size < 1 || size > MAX_SIZE) {
            throw new TrainingServiceException(ErrorCode.VALIDATION_ERROR, "Size must be between 1 and 100.");
        }
        return size;
    }
}
