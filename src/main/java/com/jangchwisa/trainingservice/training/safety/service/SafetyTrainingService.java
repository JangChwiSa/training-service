package com.jangchwisa.trainingservice.training.safety.service;

import com.jangchwisa.trainingservice.common.exception.ErrorCode;
import com.jangchwisa.trainingservice.common.exception.TrainingServiceException;
import com.jangchwisa.trainingservice.common.security.CurrentUser;
import com.jangchwisa.trainingservice.training.safety.dto.NextSafetySceneResponse;
import com.jangchwisa.trainingservice.training.safety.dto.SafetyScenarioListItemResponse;
import com.jangchwisa.trainingservice.training.safety.dto.SafetySceneResponse;
import com.jangchwisa.trainingservice.training.safety.dto.SafetySelectedResultResponse;
import com.jangchwisa.trainingservice.training.safety.dto.SafetySessionDetailResponse;
import com.jangchwisa.trainingservice.training.safety.dto.StartSafetySessionResponse;
import com.jangchwisa.trainingservice.training.safety.entity.SafetyCategory;
import com.jangchwisa.trainingservice.training.safety.repository.SafetyTrainingRepository;
import com.jangchwisa.trainingservice.training.safety.repository.SafetyTrainingRepository.SafetyChoiceRow;
import com.jangchwisa.trainingservice.training.session.entity.TrainingSession;
import com.jangchwisa.trainingservice.training.session.entity.TrainingType;
import com.jangchwisa.trainingservice.training.session.service.CreateTrainingSessionCommand;
import com.jangchwisa.trainingservice.training.session.service.SessionOwnershipValidator;
import com.jangchwisa.trainingservice.training.session.service.TrainingSessionService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SafetyTrainingService {

    private final SafetyTrainingRepository safetyTrainingRepository;
    private final TrainingSessionService trainingSessionService;
    private final SessionOwnershipValidator sessionOwnershipValidator;

    public SafetyTrainingService(
            SafetyTrainingRepository safetyTrainingRepository,
            TrainingSessionService trainingSessionService,
            SessionOwnershipValidator sessionOwnershipValidator
    ) {
        this.safetyTrainingRepository = safetyTrainingRepository;
        this.trainingSessionService = trainingSessionService;
        this.sessionOwnershipValidator = sessionOwnershipValidator;
    }

    @Transactional(readOnly = true)
    public List<SafetyScenarioListItemResponse> getScenarios(SafetyCategory category) {
        return safetyTrainingRepository.findActiveScenarios(category);
    }

    @Transactional
    public StartSafetySessionResponse startSession(CurrentUser currentUser, long scenarioId) {
        if (!safetyTrainingRepository.existsActiveScenario(scenarioId)) {
            throw new TrainingServiceException(ErrorCode.NOT_FOUND, "Safety scenario was not found.");
        }

        TrainingSession session = trainingSessionService.createSession(
                currentUser,
                new CreateTrainingSessionCommand(currentUser.userId(), TrainingType.SAFETY, null, scenarioId)
        );
        SafetySceneResponse firstScene = safetyTrainingRepository.findFirstScene(scenarioId)
                .orElseThrow(() -> new TrainingServiceException(ErrorCode.NOT_FOUND, "Safety scene was not found."));

        return new StartSafetySessionResponse(session.sessionId(), firstScene);
    }

    @Transactional
    public NextSafetySceneResponse nextScene(CurrentUser currentUser, long sessionId, long sceneId, long choiceId) {
        sessionOwnershipValidator.validateOwner(sessionId, currentUser);

        SafetyChoiceRow choice = safetyTrainingRepository.findChoice(sceneId, choiceId)
                .orElseThrow(() -> new TrainingServiceException(ErrorCode.NOT_FOUND, "Safety choice was not found."));
        safetyTrainingRepository.saveActionLog(sessionId, sceneId, choiceId, choice.correct());
        trainingSessionService.advanceCurrentStep(sessionId, (int) sceneId);

        SafetySceneResponse nextScene = null;
        if (choice.nextSceneId() != null) {
            nextScene = safetyTrainingRepository.findScene(choice.nextSceneId())
                    .orElseThrow(() -> new TrainingServiceException(ErrorCode.NOT_FOUND, "Safety scene was not found."));
        }

        return new NextSafetySceneResponse(new SafetySelectedResultResponse(choice.correct()), nextScene);
    }

    @Transactional(readOnly = true)
    public SafetySessionDetailResponse getSessionDetail(CurrentUser currentUser, long sessionId) {
        sessionOwnershipValidator.validateOwner(sessionId, currentUser);
        return new SafetySessionDetailResponse(sessionId, safetyTrainingRepository.findActionLogs(sessionId));
    }
}
