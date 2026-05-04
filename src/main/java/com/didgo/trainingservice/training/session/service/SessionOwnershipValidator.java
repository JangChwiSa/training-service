package com.didgo.trainingservice.training.session.service;

import com.didgo.trainingservice.common.exception.ErrorCode;
import com.didgo.trainingservice.common.exception.TrainingServiceException;
import com.didgo.trainingservice.common.security.CurrentUser;
import com.didgo.trainingservice.training.session.repository.TrainingSessionOwnershipRepository;
import java.util.OptionalLong;
import org.springframework.stereotype.Service;

@Service
public class SessionOwnershipValidator {

    private final TrainingSessionOwnershipRepository trainingSessionOwnershipRepository;

    public SessionOwnershipValidator(TrainingSessionOwnershipRepository trainingSessionOwnershipRepository) {
        this.trainingSessionOwnershipRepository = trainingSessionOwnershipRepository;
    }

    public void validateOwner(long sessionId, CurrentUser currentUser) {
        validateOwner(sessionId, currentUser.userId());
    }

    public void validateOwner(long sessionId, long currentUserId) {
        OptionalLong ownerUserId = trainingSessionOwnershipRepository.findUserIdBySessionId(sessionId);

        if (ownerUserId.isEmpty()) {
            throw new TrainingServiceException(ErrorCode.NOT_FOUND, "Training session was not found.");
        }

        if (ownerUserId.getAsLong() != currentUserId) {
            throw new TrainingServiceException(ErrorCode.FORBIDDEN, "Training session belongs to another user.");
        }
    }
}
