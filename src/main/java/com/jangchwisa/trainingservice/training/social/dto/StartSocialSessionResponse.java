package com.jangchwisa.trainingservice.training.social.dto;

import com.jangchwisa.trainingservice.training.session.entity.TrainingSessionStatus;

public record StartSocialSessionResponse(
        long sessionId,
        long scenarioId,
        TrainingSessionStatus status
) {
}
