package com.didgo.trainingservice.training.session.repository;

import com.didgo.trainingservice.training.session.entity.TrainingSession;
import java.util.Optional;

public interface TrainingSessionRepository {

    TrainingSession save(TrainingSession trainingSession);

    Optional<TrainingSession> findById(long sessionId);

    void update(TrainingSession trainingSession);
}
