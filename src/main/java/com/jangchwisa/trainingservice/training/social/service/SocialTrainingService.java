package com.jangchwisa.trainingservice.training.social.service;

import com.jangchwisa.trainingservice.external.openai.dto.TrainingEvaluationLog;
import com.jangchwisa.trainingservice.external.openai.dto.TrainingEvaluationStorageModel;
import com.jangchwisa.trainingservice.common.exception.ErrorCode;
import com.jangchwisa.trainingservice.common.exception.TrainingServiceException;
import com.jangchwisa.trainingservice.common.security.CurrentUser;
import com.jangchwisa.trainingservice.training.completion.TrainingCompletionCommand;
import com.jangchwisa.trainingservice.training.completion.TrainingCompletionFeedback;
import com.jangchwisa.trainingservice.training.completion.TrainingCompletionProgress;
import com.jangchwisa.trainingservice.training.completion.TrainingCompletionResult;
import com.jangchwisa.trainingservice.training.completion.TrainingCompletionScore;
import com.jangchwisa.trainingservice.training.completion.TrainingCompletionService;
import com.jangchwisa.trainingservice.training.completion.TrainingCompletionSummary;
import com.jangchwisa.trainingservice.training.evaluation.TrainingEvaluationCommand;
import com.jangchwisa.trainingservice.training.evaluation.TrainingEvaluationService;
import com.jangchwisa.trainingservice.training.session.entity.TrainingSession;
import com.jangchwisa.trainingservice.training.session.entity.TrainingType;
import com.jangchwisa.trainingservice.training.session.service.CreateTrainingSessionCommand;
import com.jangchwisa.trainingservice.training.session.service.SessionOwnershipValidator;
import com.jangchwisa.trainingservice.training.session.service.TrainingSessionService;
import com.jangchwisa.trainingservice.training.social.dto.CompleteSocialSessionRequest;
import com.jangchwisa.trainingservice.training.social.dto.CompleteSocialSessionResponse;
import com.jangchwisa.trainingservice.training.social.dto.SelectSocialJobTypeResponse;
import com.jangchwisa.trainingservice.training.social.dto.SocialFeedbackResponse;
import com.jangchwisa.trainingservice.training.social.dto.SocialScenarioDetailResponse;
import com.jangchwisa.trainingservice.training.social.dto.SocialScenarioListItemResponse;
import com.jangchwisa.trainingservice.training.social.dto.SocialSessionDetailResponse;
import com.jangchwisa.trainingservice.training.social.dto.StartSocialSessionResponse;
import com.jangchwisa.trainingservice.training.social.entity.SocialJobType;
import com.jangchwisa.trainingservice.training.social.repository.SocialTrainingRepository;
import com.jangchwisa.trainingservice.training.social.repository.SocialTrainingRepository.SocialScenarioSummaryRow;
import com.jangchwisa.trainingservice.training.social.repository.SocialTrainingRepository.SocialScoreRow;
import java.util.List;
import java.util.Map;
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

    @Autowired
    public SocialTrainingService(
            SocialTrainingRepository socialTrainingRepository,
            TrainingSessionService trainingSessionService,
            SessionOwnershipValidator sessionOwnershipValidator,
            TrainingEvaluationService trainingEvaluationService,
            TrainingCompletionService trainingCompletionService
    ) {
        this.socialTrainingRepository = socialTrainingRepository;
        this.trainingSessionService = trainingSessionService;
        this.sessionOwnershipValidator = sessionOwnershipValidator;
        this.trainingEvaluationService = trainingEvaluationService;
        this.trainingCompletionService = trainingCompletionService;
    }

    public SocialTrainingService(
            SocialTrainingRepository socialTrainingRepository,
            TrainingSessionService trainingSessionService,
            SessionOwnershipValidator sessionOwnershipValidator
    ) {
        this(socialTrainingRepository, trainingSessionService, sessionOwnershipValidator, null, null);
    }

    public SelectSocialJobTypeResponse selectJobType(SocialJobType jobType) {
        return new SelectSocialJobTypeResponse(jobType, NEXT_PAGE_SCENARIO_SELECTION);
    }

    @Transactional(readOnly = true)
    public List<SocialScenarioListItemResponse> getScenarios(SocialJobType jobType) {
        return socialTrainingRepository.findActiveScenariosByJobType(jobType);
    }

    @Transactional(readOnly = true)
    public SocialScenarioDetailResponse getScenarioDetail(long scenarioId) {
        return socialTrainingRepository.findActiveScenarioDetail(scenarioId)
                .orElseThrow(() -> new TrainingServiceException(ErrorCode.NOT_FOUND, "Social scenario was not found."));
    }

    @Transactional
    public StartSocialSessionResponse startSession(CurrentUser currentUser, SocialJobType jobType, long scenarioId) {
        if (!socialTrainingRepository.existsActiveScenario(scenarioId, jobType)) {
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
                "사회성 훈련을 완료했습니다.",
                "사회성 훈련 대화 로그가 저장되었습니다.",
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

        return new CompleteSocialSessionResponse(result.sessionId(), result.score(), result.feedbackSummary(), true);
    }

    private void ensureCompletionDependencies() {
        if (trainingEvaluationService == null || trainingCompletionService == null) {
            throw new TrainingServiceException(ErrorCode.INTERNAL_SERVER_ERROR, "Social completion is not configured.");
        }
    }
}
