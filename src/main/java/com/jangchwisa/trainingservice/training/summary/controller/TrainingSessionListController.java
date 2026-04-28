package com.jangchwisa.trainingservice.training.summary.controller;

import com.jangchwisa.trainingservice.common.exception.ErrorCode;
import com.jangchwisa.trainingservice.common.exception.TrainingServiceException;
import com.jangchwisa.trainingservice.common.response.ApiResponse;
import com.jangchwisa.trainingservice.common.security.AuthenticatedUser;
import com.jangchwisa.trainingservice.common.security.CurrentUser;
import com.jangchwisa.trainingservice.training.safety.entity.SafetyCategory;
import com.jangchwisa.trainingservice.training.session.entity.TrainingType;
import com.jangchwisa.trainingservice.training.summary.dto.TrainingSessionListResponse;
import com.jangchwisa.trainingservice.training.summary.service.TrainingSessionListService;
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

    @GetMapping("/api/trainings/sessions")
    public ApiResponse<TrainingSessionListResponse> getSessions(
            @AuthenticatedUser CurrentUser currentUser,
            @RequestParam String type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        TrainingType trainingType = parseTrainingType(type);
        SafetyCategory safetyCategory = parseSafetyCategory(category);
        int pageNumber = normalizePage(page);
        int pageSize = normalizeSize(size);

        TrainingSessionListResponse response = trainingSessionListService.getSessions(
                currentUser.userId(),
                trainingType,
                safetyCategory,
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

    private SafetyCategory parseSafetyCategory(String category) {
        if (category == null || category.isBlank()) {
            return null;
        }

        try {
            return SafetyCategory.valueOf(category.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new TrainingServiceException(ErrorCode.VALIDATION_ERROR, "Safety category is invalid.");
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
