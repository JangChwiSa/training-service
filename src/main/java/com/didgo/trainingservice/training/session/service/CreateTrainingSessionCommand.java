package com.didgo.trainingservice.training.session.service;

import com.didgo.trainingservice.training.session.entity.TrainingType;

public record CreateTrainingSessionCommand(
        long userId,
        TrainingType trainingType,
        String subType,
        Long scenarioId
) {
}
