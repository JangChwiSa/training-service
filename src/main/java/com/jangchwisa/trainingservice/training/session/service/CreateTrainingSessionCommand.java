package com.jangchwisa.trainingservice.training.session.service;

import com.jangchwisa.trainingservice.training.session.entity.TrainingType;

public record CreateTrainingSessionCommand(
        long userId,
        TrainingType trainingType,
        String subType,
        Long scenarioId
) {
}
