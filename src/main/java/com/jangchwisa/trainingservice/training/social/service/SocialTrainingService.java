package com.jangchwisa.trainingservice.training.social.service;

import com.jangchwisa.trainingservice.common.exception.ErrorCode;
import com.jangchwisa.trainingservice.common.exception.TrainingServiceException;
import com.jangchwisa.trainingservice.common.security.CurrentUser;
import com.jangchwisa.trainingservice.training.session.entity.TrainingSession;
import com.jangchwisa.trainingservice.training.session.entity.TrainingType;
import com.jangchwisa.trainingservice.training.session.service.CreateTrainingSessionCommand;
import com.jangchwisa.trainingservice.training.session.service.SessionOwnershipValidator;
import com.jangchwisa.trainingservice.training.session.service.TrainingSessionService;
import com.jangchwisa.trainingservice.training.social.dto.SelectSocialJobTypeResponse;
import com.jangchwisa.trainingservice.training.social.dto.SocialFeedbackResponse;
import com.jangchwisa.trainingservice.training.social.dto.SocialScenarioDetailResponse;
import com.jangchwisa.trainingservice.training.social.dto.SocialScenarioListItemResponse;
import com.jangchwisa.trainingservice.training.social.dto.SocialSessionDetailResponse;
import com.jangchwisa.trainingservice.training.social.dto.StartSocialSessionResponse;
import com.jangchwisa.trainingservice.training.social.entity.SocialJobType;
import com.jangchwisa.trainingservice.training.social.repository.SocialTrainingRepository;
import com.jangchwisa.trainingservice.training.social.repository.SocialTrainingRepository.SocialScoreRow;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SocialTrainingService {

    private static final String NEXT_PAGE_SCENARIO_SELECTION = "SCENARIO_SELECTION";

    private final SocialTrainingRepository socialTrainingRepository;
    private final TrainingSessionService trainingSessionService;
    private final SessionOwnershipValidator sessionOwnershipValidator;

    public SocialTrainingService(
            SocialTrainingRepository socialTrainingRepository,
            TrainingSessionService trainingSessionService,
            SessionOwnershipValidator sessionOwnershipValidator
    ) {
        this.socialTrainingRepository = socialTrainingRepository;
        this.trainingSessionService = trainingSessionService;
        this.sessionOwnershipValidator = sessionOwnershipValidator;
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
}
