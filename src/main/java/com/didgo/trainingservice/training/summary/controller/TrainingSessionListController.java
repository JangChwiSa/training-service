package com.didgo.trainingservice.training.summary.controller;

import com.didgo.trainingservice.common.exception.ErrorCode;
import com.didgo.trainingservice.common.exception.TrainingServiceException;
import com.didgo.trainingservice.common.response.ApiResponse;
import com.didgo.trainingservice.common.security.AuthenticatedUser;
import com.didgo.trainingservice.common.security.CurrentUser;
import com.didgo.trainingservice.training.session.entity.TrainingType;
import com.didgo.trainingservice.training.summary.dto.TrainingSessionListResponse;
import com.didgo.trainingservice.training.summary.service.TrainingSessionListService;
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
            summary = "?덈젴 湲곕줉 紐⑸줉 議고쉶",
            description = "?좏깮???덈젴 ?좏삎???꾨즺 ?대젰??理쒖떊?쒖쑝濡?議고쉶?⑸땲??"
    )
    @GetMapping("/api/trainings/sessions")
    public ApiResponse<TrainingSessionListResponse> getSessions(
            @AuthenticatedUser CurrentUser currentUser,
            @Parameter(description = "議고쉶???덈젴 ?좏삎?낅땲?? SOCIAL, SAFETY, DOCUMENT, FOCUS 以??섎굹瑜??낅젰?⑸땲??", example = "SOCIAL")
            @RequestParam String type,
            @Parameter(description = "?섏씠吏 踰덊샇?낅땲?? 0遺???쒖옉?⑸땲??", example = "0")
            @RequestParam(required = false) Integer page,
            @Parameter(description = "?섏씠吏????ぉ ?섏엯?덈떎. 1遺??100源뚯? ?낅젰?????덉뒿?덈떎.", example = "10")
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
