package com.didgo.trainingservice.training.safety.service;

import com.didgo.trainingservice.common.exception.ErrorCode;
import com.didgo.trainingservice.common.exception.TrainingServiceException;
import com.didgo.trainingservice.common.security.CurrentUser;
import com.didgo.trainingservice.training.completion.TrainingCompletionCommand;
import com.didgo.trainingservice.training.completion.TrainingCompletionFeedback;
import com.didgo.trainingservice.training.completion.TrainingCompletionProgress;
import com.didgo.trainingservice.training.completion.TrainingCompletionResult;
import com.didgo.trainingservice.training.completion.TrainingCompletionScore;
import com.didgo.trainingservice.training.completion.TrainingCompletionService;
import com.didgo.trainingservice.training.completion.TrainingCompletionSummary;
import com.didgo.trainingservice.training.safety.dto.CompleteSafetySessionResponse;
import com.didgo.trainingservice.training.safety.dto.NextSafetySceneResponse;
import com.didgo.trainingservice.training.safety.dto.SafetyScenarioListItemResponse;
import com.didgo.trainingservice.training.safety.dto.SafetySceneResponse;
import com.didgo.trainingservice.training.safety.dto.SafetySelectedResultResponse;
import com.didgo.trainingservice.training.safety.dto.SafetyChoiceSummaryResponse;
import com.didgo.trainingservice.training.safety.dto.SafetyFeedbackResponse;
import com.didgo.trainingservice.training.safety.dto.SafetySessionDetailResponse;
import com.didgo.trainingservice.training.safety.dto.StartSafetySessionResponse;
import com.didgo.trainingservice.training.safety.entity.SafetyCategory;
import com.didgo.trainingservice.training.safety.repository.SafetyTrainingRepository;
import com.didgo.trainingservice.training.safety.repository.SafetyTrainingRepository.SafetyActionSummaryRow;
import com.didgo.trainingservice.training.safety.repository.SafetyTrainingRepository.SafetyChoiceRow;
import com.didgo.trainingservice.training.safety.repository.SafetyTrainingRepository.SafetyScenarioSummaryRow;
import com.didgo.trainingservice.training.safety.repository.SafetyTrainingRepository.SafetyScoreRow;
import com.didgo.trainingservice.training.session.entity.TrainingSession;
import com.didgo.trainingservice.training.session.entity.TrainingType;
import com.didgo.trainingservice.training.session.service.CreateTrainingSessionCommand;
import com.didgo.trainingservice.training.session.service.SessionOwnershipValidator;
import com.didgo.trainingservice.training.session.service.TrainingSessionService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SafetyTrainingService {

    private final SafetyTrainingRepository safetyTrainingRepository;
    private final TrainingSessionService trainingSessionService;
    private final SessionOwnershipValidator sessionOwnershipValidator;
    private final TrainingCompletionService trainingCompletionService;

    @Autowired
    public SafetyTrainingService(
            SafetyTrainingRepository safetyTrainingRepository,
            TrainingSessionService trainingSessionService,
            SessionOwnershipValidator sessionOwnershipValidator,
            TrainingCompletionService trainingCompletionService
    ) {
        this.safetyTrainingRepository = safetyTrainingRepository;
        this.trainingSessionService = trainingSessionService;
        this.sessionOwnershipValidator = sessionOwnershipValidator;
        this.trainingCompletionService = trainingCompletionService;
    }

    public SafetyTrainingService(
            SafetyTrainingRepository safetyTrainingRepository,
            TrainingSessionService trainingSessionService,
            SessionOwnershipValidator sessionOwnershipValidator
    ) {
        this(safetyTrainingRepository, trainingSessionService, sessionOwnershipValidator, null);
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
        SafetySceneResponse nextScene = null;
        if (choice.nextSceneId() != null) {
            nextScene = safetyTrainingRepository.findScene(choice.nextSceneId())
                    .orElseThrow(() -> new TrainingServiceException(ErrorCode.NOT_FOUND, "Safety scene was not found."));
        }
        safetyTrainingRepository.saveActionLog(sessionId, sceneId, choiceId, choice.correct());
        trainingSessionService.advanceCurrentStep(sessionId, nextScene == null ? (int) sceneId : (int) nextScene.sceneId());

        boolean completed = nextScene == null || nextScene.endScene();
        return new NextSafetySceneResponse(
                completed,
                completed ? null : nextScene,
                completed
                        ? new SafetySelectedResultResponse(
                        choice.correct(),
                        choice.resultText(),
                        choice.effectText(),
                        choice.feedbackImageUrl(),
                        choice.feedbackImageAlt()
                )
                        : null
        );
    }

    public NextSafetySceneResponse advanceScene(CurrentUser currentUser, long sessionId, long sceneId) {
        sessionOwnershipValidator.validateOwner(sessionId, currentUser);

        TrainingSession session = trainingSessionService.getSession(sessionId);
        SafetySceneResponse nextScene = safetyTrainingRepository.findNextScene(sceneId, session.scenarioId())
                .orElseThrow(() -> new TrainingServiceException(ErrorCode.NOT_FOUND, "Next safety scene was not found."));
        trainingSessionService.advanceCurrentStep(sessionId, (int) nextScene.sceneId());

        return new NextSafetySceneResponse(false, nextScene, null);
    }

    @Transactional(readOnly = true)
    public SafetySessionDetailResponse getSessionDetail(CurrentUser currentUser, long sessionId) {
        sessionOwnershipValidator.validateOwner(sessionId, currentUser);
        SafetyScoreRow score = safetyTrainingRepository.findScore(sessionId)
                .orElseThrow(() -> new TrainingServiceException(ErrorCode.NOT_FOUND, "Safety training score was not found."));
        SafetyFeedbackResponse feedback = safetyTrainingRepository.findFeedback(sessionId)
                .orElseThrow(() -> new TrainingServiceException(ErrorCode.NOT_FOUND, "Safety training feedback was not found."));

        return new SafetySessionDetailResponse(
                sessionId,
                score.score(),
                new SafetyChoiceSummaryResponse(score.correctCount(), score.totalCount()),
                safetyTrainingRepository.findActionDetails(sessionId),
                feedback
        );
    }

    public CompleteSafetySessionResponse completeSession(CurrentUser currentUser, long sessionId) {
        ensureCompletionDependency();
        sessionOwnershipValidator.validateOwner(sessionId, currentUser);
        SafetyScenarioSummaryRow scenario = safetyTrainingRepository.findScenarioSummaryBySessionId(sessionId)
                .orElseThrow(() -> new TrainingServiceException(ErrorCode.NOT_FOUND, "Safety scenario was not found."));
        SafetyActionSummaryRow actionSummary = safetyTrainingRepository.summarizeActions(sessionId);
        if (actionSummary.totalCount() == 0) {
            throw new TrainingServiceException(ErrorCode.VALIDATION_ERROR, "Safety completion requires action logs.");
        }

        int score = (int) Math.round((actionSummary.correctCount() * 100.0) / actionSummary.totalCount());
        TrainingCompletionResult result = trainingCompletionService.complete(new TrainingCompletionCommand(
                currentUser.userId(),
                sessionId,
                TrainingType.SAFETY,
                new TrainingCompletionScore(
                        score,
                        "CHOICE_RESULT",
                        actionSummary.correctCount(),
                        actionSummary.totalCount(),
                        java.math.BigDecimal.valueOf(score),
                        actionSummary.totalCount() - actionSummary.correctCount(),
                        null,
                        null
                ),
                new TrainingCompletionFeedback(
                        "SUMMARY",
                        "SYSTEM",
                        "?덉쟾 ?덈젴???꾨즺?덉뒿?덈떎.",
                        "?좏깮 寃곌낵瑜?湲곗??쇰줈 ?덉쟾 ?먮떒 ?먯닔瑜?怨꾩궛?덉뒿?덈떎."
                ),
                new TrainingCompletionSummary(
                        scenario.scenarioId(),
                        scenario.title(),
                        scenario.category().name(),
                        scenario.title(),
                        "?덉쟾 ?덈젴 ?꾨즺",
                        actionSummary.correctCount(),
                        actionSummary.totalCount(),
                        java.math.BigDecimal.valueOf(score),
                        actionSummary.totalCount() - actionSummary.correctCount(),
                        null,
                        null
                ),
                TrainingCompletionProgress.none(),
                null
        ));

        return new CompleteSafetySessionResponse(
                result.sessionId(),
                result.score(),
                actionSummary.correctCount(),
                actionSummary.totalCount(),
                true
        );
    }

    private void ensureCompletionDependency() {
        if (trainingCompletionService == null) {
            throw new TrainingServiceException(ErrorCode.INTERNAL_SERVER_ERROR, "Safety completion is not configured.");
        }
    }
}
