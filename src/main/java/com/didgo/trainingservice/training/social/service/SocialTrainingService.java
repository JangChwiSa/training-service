package com.didgo.trainingservice.training.social.service;

import com.didgo.trainingservice.common.exception.ErrorCode;
import com.didgo.trainingservice.common.exception.TrainingServiceException;
import com.didgo.trainingservice.common.security.CurrentUser;
import com.didgo.trainingservice.external.openai.dto.TrainingEvaluationLog;
import com.didgo.trainingservice.external.openai.dto.TrainingEvaluationStorageModel;
import com.didgo.trainingservice.training.completion.TrainingCompletionCommand;
import com.didgo.trainingservice.training.completion.TrainingCompletionFeedback;
import com.didgo.trainingservice.training.completion.TrainingCompletionProgress;
import com.didgo.trainingservice.training.completion.TrainingCompletionResult;
import com.didgo.trainingservice.training.completion.TrainingCompletionScore;
import com.didgo.trainingservice.training.completion.TrainingCompletionService;
import com.didgo.trainingservice.training.completion.TrainingCompletionSummary;
import com.didgo.trainingservice.training.evaluation.TrainingEvaluationCommand;
import com.didgo.trainingservice.training.evaluation.TrainingEvaluationService;
import com.didgo.trainingservice.training.session.entity.TrainingSession;
import com.didgo.trainingservice.training.session.entity.TrainingType;
import com.didgo.trainingservice.training.session.service.CreateTrainingSessionCommand;
import com.didgo.trainingservice.training.session.service.SessionOwnershipValidator;
import com.didgo.trainingservice.training.session.service.TrainingSessionService;
import com.didgo.trainingservice.training.social.dto.CompleteSocialSessionRequest;
import com.didgo.trainingservice.training.social.dto.CompleteSocialSessionResponse;
import com.didgo.trainingservice.training.social.dto.SelectSocialJobTypeResponse;
import com.didgo.trainingservice.training.social.dto.SocialAdaptiveScenarioResponse;
import com.didgo.trainingservice.training.social.dto.SocialFeedbackResponse;
import com.didgo.trainingservice.training.social.dto.SocialScenarioDetailResponse;
import com.didgo.trainingservice.training.social.dto.SocialScenarioListItemResponse;
import com.didgo.trainingservice.training.social.dto.SocialSessionDetailResponse;
import com.didgo.trainingservice.training.social.dto.StartSocialSessionResponse;
import com.didgo.trainingservice.training.social.entity.SocialDialogSpeaker;
import com.didgo.trainingservice.training.social.entity.SocialJobType;
import com.didgo.trainingservice.training.social.repository.SocialTrainingRepository;
import com.didgo.trainingservice.training.social.repository.SocialTrainingRepository.SocialAdaptiveRecommendationRow;
import com.didgo.trainingservice.training.social.repository.SocialTrainingRepository.SocialScenarioSummaryRow;
import com.didgo.trainingservice.training.social.repository.SocialTrainingRepository.SocialScoreRow;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SocialTrainingService {

    private static final String NEXT_PAGE_SCENARIO_SELECTION = "SCENARIO_SELECTION";

    private final SocialTrainingRepository socialTrainingRepository;
    private final TrainingSessionService trainingSessionService;
    private final SessionOwnershipValidator sessionOwnershipValidator;
    private final TrainingEvaluationService trainingEvaluationService;
    private final TrainingCompletionService trainingCompletionService;
    private final SocialAdaptiveScenarioGenerator socialAdaptiveScenarioGenerator;
    private final SocialAdaptiveScenarioPreparationService socialAdaptiveScenarioPreparationService;

    @Autowired
    public SocialTrainingService(
            SocialTrainingRepository socialTrainingRepository,
            TrainingSessionService trainingSessionService,
            SessionOwnershipValidator sessionOwnershipValidator,
            TrainingEvaluationService trainingEvaluationService,
            TrainingCompletionService trainingCompletionService,
            SocialAdaptiveScenarioGenerator socialAdaptiveScenarioGenerator,
            SocialAdaptiveScenarioPreparationService socialAdaptiveScenarioPreparationService
    ) {
        this.socialTrainingRepository = socialTrainingRepository;
        this.trainingSessionService = trainingSessionService;
        this.sessionOwnershipValidator = sessionOwnershipValidator;
        this.trainingEvaluationService = trainingEvaluationService;
        this.trainingCompletionService = trainingCompletionService;
        this.socialAdaptiveScenarioGenerator = socialAdaptiveScenarioGenerator;
        this.socialAdaptiveScenarioPreparationService = socialAdaptiveScenarioPreparationService;
    }

    public SocialTrainingService(
            SocialTrainingRepository socialTrainingRepository,
            TrainingSessionService trainingSessionService,
            SessionOwnershipValidator sessionOwnershipValidator
    ) {
        this(socialTrainingRepository, trainingSessionService, sessionOwnershipValidator, null, null, null, null);
    }

    public SocialTrainingService(
            SocialTrainingRepository socialTrainingRepository,
            TrainingSessionService trainingSessionService,
            SessionOwnershipValidator sessionOwnershipValidator,
            TrainingEvaluationService trainingEvaluationService,
            TrainingCompletionService trainingCompletionService
    ) {
        this(
                socialTrainingRepository,
                trainingSessionService,
                sessionOwnershipValidator,
                trainingEvaluationService,
                trainingCompletionService,
                null,
                null
        );
    }

    public SelectSocialJobTypeResponse selectJobType(SocialJobType jobType) {
        return new SelectSocialJobTypeResponse(jobType, NEXT_PAGE_SCENARIO_SELECTION);
    }

    @Transactional(readOnly = true)
    public List<SocialScenarioListItemResponse> getScenarios(SocialJobType jobType) {
        return socialTrainingRepository.findActiveScenariosByJobType(jobType);
    }

    @Transactional(readOnly = true)
    public SocialScenarioDetailResponse getScenarioDetail(CurrentUser currentUser, long scenarioId) {
        return socialTrainingRepository.findAccessibleScenarioDetail(scenarioId, currentUser.userId())
                .orElseThrow(() -> new TrainingServiceException(ErrorCode.NOT_FOUND, "Social scenario was not found."));
    }

    @Transactional
    public SocialAdaptiveScenarioResponse generateAdaptiveScenario(CurrentUser currentUser, SocialJobType jobType) {
        ensureAdaptiveGenerationDependency();
        Optional<SocialAdaptiveRecommendationRow> readyRecommendation =
                socialTrainingRepository.findReadyAdaptiveRecommendation(currentUser.userId(), jobType);
        if (readyRecommendation.isPresent()) {
            SocialAdaptiveRecommendationRow recommendation = readyRecommendation.get();
            SocialScenarioDetailResponse scenario = socialTrainingRepository.findAccessibleScenarioDetail(
                    recommendation.scenarioId(),
                    currentUser.userId()
            ).orElseThrow(() -> new TrainingServiceException(ErrorCode.NOT_FOUND, "Social scenario was not found."));
            socialTrainingRepository.markAdaptiveRecommendationConsumed(recommendation.recommendationId());
            return new SocialAdaptiveScenarioResponse(
                    scenario.scenarioId(),
                    scenario.jobType(),
                    scenario.title(),
                    scenario.backgroundText(),
                    scenario.situationText(),
                    scenario.characterInfo(),
                    scenario.difficulty(),
                    recommendation.focusSummary()
            );
        }

        List<SocialTrainingRepository.SocialHistoryRow> historyRows =
                socialTrainingRepository.findRecentSocialHistory(currentUser.userId(), 8);
        SocialAdaptiveScenarioDraft draft = socialAdaptiveScenarioGenerator.generate(jobType, historyRows);
        long scenarioId = socialTrainingRepository.saveGeneratedScenario(currentUser.userId(), jobType, draft);
        socialTrainingRepository.saveAdaptiveRecommendation(currentUser.userId(), jobType, scenarioId);
        socialTrainingRepository.findReadyAdaptiveRecommendation(currentUser.userId(), jobType)
                .ifPresent(recommendation ->
                        socialTrainingRepository.markAdaptiveRecommendationConsumed(recommendation.recommendationId()));
        return new SocialAdaptiveScenarioResponse(
                scenarioId,
                jobType,
                draft.title(),
                draft.backgroundText(),
                draft.situationText(),
                draft.characterInfo(),
                parseDifficulty(draft.difficulty()),
                draft.focusSummary()
        );
    }

    @Transactional
    public StartSocialSessionResponse startSession(CurrentUser currentUser, SocialJobType jobType, long scenarioId) {
        if (!socialTrainingRepository.existsAccessibleScenario(scenarioId, jobType, currentUser.userId())) {
            throw new TrainingServiceException(ErrorCode.NOT_FOUND, "Social scenario was not found.");
        }

        TrainingSession session = trainingSessionService.createSession(
                currentUser,
                new CreateTrainingSessionCommand(currentUser.userId(), TrainingType.SOCIAL, jobType.name(), scenarioId)
        );

        return new StartSocialSessionResponse(session.sessionId(), scenarioId, session.status());
    }

    @Transactional(readOnly = true)
    public SocialSessionDetailResponse getSessionDetail(CurrentUser currentUser, long sessionId) {
        sessionOwnershipValidator.validateOwner(sessionId, currentUser);
        SocialScoreRow score = socialTrainingRepository.findScore(sessionId)
                .orElseThrow(() -> new TrainingServiceException(ErrorCode.NOT_FOUND, "Social training score was not found."));
        SocialFeedbackResponse feedback = socialTrainingRepository.findFeedback(sessionId)
                .orElseThrow(() -> new TrainingServiceException(ErrorCode.NOT_FOUND, "Social training feedback was not found."));

        return new SocialSessionDetailResponse(
                sessionId,
                score.score(),
                score.scoreType(),
                feedback,
                socialTrainingRepository.findDialogLogs(sessionId)
        );
    }

    public CompleteSocialSessionResponse completeSession(
            CurrentUser currentUser,
            long sessionId,
            CompleteSocialSessionRequest request
    ) {
        ensureCompletionDependencies();
        sessionOwnershipValidator.validateOwner(sessionId, currentUser);
        validateEvaluableDialogLogs(request);
        SocialScenarioSummaryRow scenario = socialTrainingRepository.findScenarioSummaryBySessionId(sessionId)
                .orElseThrow(() -> new TrainingServiceException(ErrorCode.NOT_FOUND, "Social scenario was not found."));
        TrainingEvaluationStorageModel evaluation = trainingEvaluationService.evaluate(new TrainingEvaluationCommand(
                TrainingType.SOCIAL,
                scenario.title(),
                request.dialogLogs().stream()
                        .map(log -> new TrainingEvaluationLog(log.speaker().name(), log.content()))
                        .toList(),
                Map.of("dialogLogCount", request.dialogLogs().size()),
                0,
                "AI_EVALUATION",
                "사회성 훈련 평가를 완료하지 못했습니다.",
                "일시적인 평가 오류로 점수를 산정하지 못했습니다. 대화 기록은 저장되었습니다.",
                true
        ));

        TrainingCompletionResult result = trainingCompletionService.complete(new TrainingCompletionCommand(
                currentUser.userId(),
                sessionId,
                TrainingType.SOCIAL,
                new TrainingCompletionScore(
                        evaluation.score().score(),
                        evaluation.score().scoreType(),
                        null,
                        null,
                        null,
                        null,
                        null,
                        evaluation.score().rawMetricsJson()
                ),
                new TrainingCompletionFeedback(
                        evaluation.feedback().feedbackType(),
                        evaluation.feedback().feedbackSource(),
                        evaluation.feedback().summary(),
                        evaluation.feedback().detailText()
                ),
                new TrainingCompletionSummary(
                        scenario.scenarioId(),
                        scenario.title(),
                        null,
                        scenario.title(),
                        "사회성 훈련 완료",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                ),
                TrainingCompletionProgress.none(),
                () -> socialTrainingRepository.saveDialogLogs(sessionId, request.dialogLogs())
        ));

        prepareNextAdaptiveScenario(currentUser, sessionId, scenario, result);
        return new CompleteSocialSessionResponse(result.sessionId(), result.score(), result.feedbackSummary(), true);
    }

    private void prepareNextAdaptiveScenario(
            CurrentUser currentUser,
            long sessionId,
            SocialScenarioSummaryRow scenario,
            TrainingCompletionResult result
    ) {
        if (socialAdaptiveScenarioPreparationService == null || scenario.jobType() == null) {
            return;
        }
        socialAdaptiveScenarioPreparationService.prepareNextScenario(
                currentUser.userId(),
                scenario.jobType(),
                sessionId,
                result.score(),
                result.feedbackSummary(),
                null
        );
    }

    private void validateEvaluableDialogLogs(CompleteSocialSessionRequest request) {
        boolean hasUserUtterance = request.dialogLogs().stream()
                .anyMatch(log -> log.speaker() == SocialDialogSpeaker.USER
                        && log.content() != null
                        && !log.content().isBlank());

        if (!hasUserUtterance) {
            throw new TrainingServiceException(ErrorCode.VALIDATION_ERROR, "사용자 대화가 없어 피드백을 생성할 수 없습니다.");
        }
    }

    private void ensureCompletionDependencies() {
        if (trainingEvaluationService == null || trainingCompletionService == null) {
            throw new TrainingServiceException(ErrorCode.INTERNAL_SERVER_ERROR, "Social completion is not configured.");
        }
    }

    private void ensureAdaptiveGenerationDependency() {
        if (socialAdaptiveScenarioGenerator == null) {
            throw new TrainingServiceException(ErrorCode.INTERNAL_SERVER_ERROR, "Social adaptive scenario generation is not configured.");
        }
    }

    private Integer parseDifficulty(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException exception) {
            return 2;
        }
    }
}
