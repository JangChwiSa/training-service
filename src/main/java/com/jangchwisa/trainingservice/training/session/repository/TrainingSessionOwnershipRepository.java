package com.jangchwisa.trainingservice.training.session.repository;

import java.util.OptionalLong;

public interface TrainingSessionOwnershipRepository {

    OptionalLong findUserIdBySessionId(long sessionId);
}
