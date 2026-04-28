package com.jangchwisa.trainingservice.training.summary.dto;

import com.jangchwisa.trainingservice.training.session.entity.TrainingType;
import java.util.List;

public record TrainingSessionListResponse(
        TrainingType trainingType,
        int page,
        int size,
        long totalElements,
        List<TrainingSessionListItemResponse> sessions
) {
}
