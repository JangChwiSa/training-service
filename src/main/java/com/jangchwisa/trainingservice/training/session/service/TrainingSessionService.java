package com.jangchwisa.trainingservice.training.session.service;

import com.jangchwisa.trainingservice.common.exception.ErrorCode;
import com.jangchwisa.trainingservice.common.exception.TrainingServiceException;
import com.jangchwisa.trainingservice.common.security.CurrentUser;
import com.jangchwisa.trainingservice.training.session.entity.TrainingSession;
import com.jangchwisa.trainingservice.training.session.repository.TrainingSessionRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TrainingSessionService {

    private final TrainingSessionRepository trainingSessionRepository;
    private final Clock clock;

    public TrainingSessionService(TrainingSessionRepository trainingSessionRepository, Clock clock) {
        this.trainingSessionRepository = trainingSessionRepository;
        this.clock = clock;
    }

    @Transactional
    public TrainingSession createSession(CurrentUser currentUser, CreateTrainingSessionCommand command) {
        if (currentUser.userId() != command.userId()) {
            throw new TrainingServiceException(ErrorCode.FORBIDDEN, "Current user does not match session command user.");
        }
        return createSession(command);
    }

    @Transactional
    public TrainingSession createSession(CreateTrainingSessionCommand command) {
        TrainingSession session = TrainingSession.start(
                command.userId(),
                command.trainingType(),
                command.subType(),
                command.scenarioId(),
                now()
        );
        return trainingSessionRepository.save(session);
    }

    @Transactional
    public TrainingSession advanceCurrentStep(long sessionId, int nextStep) {
        TrainingSession session = findSession(sessionId);
        TrainingSession updatedSession = session.advanceToStep(nextStep);
        trainingSessionRepository.update(updatedSession);
        return updatedSession;
    }

    @Transactional
    public TrainingSession completeSession(long sessionId) {
        TrainingSession session = findSession(sessionId);
        TrainingSession completedSession = session.complete(now());
        trainingSessionRepository.update(completedSession);
        return completedSession;
    }

    @Transactional
    public TrainingSession failSession(long sessionId) {
        TrainingSession session = findSession(sessionId);
        TrainingSession failedSession = session.fail(now());
        trainingSessionRepository.update(failedSession);
        return failedSession;
    }

    @Transactional(readOnly = true)
    public TrainingSession getSession(long sessionId) {
        return findSession(sessionId);
    }

    private TrainingSession findSession(long sessionId) {
        return trainingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new TrainingServiceException(ErrorCode.NOT_FOUND, "Training session was not found."));
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }
}
