package com.didgo.trainingservice.training.social.voice;

import com.didgo.trainingservice.common.exception.ErrorCode;
import com.didgo.trainingservice.common.exception.TrainingServiceException;
import com.didgo.trainingservice.common.security.CurrentUser;
import com.didgo.trainingservice.training.session.entity.TrainingSession;
import com.didgo.trainingservice.training.session.entity.TrainingSessionStatus;
import com.didgo.trainingservice.training.session.entity.TrainingType;
import com.didgo.trainingservice.training.session.service.SessionOwnershipValidator;
import com.didgo.trainingservice.training.session.service.TrainingSessionService;
import com.didgo.trainingservice.training.social.dto.SocialScenarioDetailResponse;
import com.didgo.trainingservice.training.social.repository.SocialTrainingRepository;
import com.didgo.trainingservice.training.social.voice.realtime.OpenAiRealtimeProperties;
import com.didgo.trainingservice.training.social.voice.dto.ConversationSettingsResponse;
import com.didgo.trainingservice.training.social.voice.dto.OpeningVoiceResponse;
import com.didgo.trainingservice.training.social.voice.dto.RealtimeConnectionResponse;
import com.didgo.trainingservice.training.social.voice.dto.SocialVoiceSessionPrepareResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SocialVoiceSessionService {

    private static final String CONNECTION_MODE = "SERVER_RELAY";
    private static final String WS_URL = "/ws/trainings/social/voice";
    private static final String PROTOCOL = "json";
    private static final String OPENING_AUDIO_STATUS_READY = "READY";
    private static final String DEFAULT_INSTRUCTIONS_VERSION = "v1";

    private final TrainingSessionService trainingSessionService;
    private final SessionOwnershipValidator sessionOwnershipValidator;
    private final SocialTrainingRepository socialTrainingRepository;
    private final SocialVoiceSessionTokenService tokenService;
    private final OpenAiRealtimeProperties realtimeProperties;

    public SocialVoiceSessionService(
            TrainingSessionService trainingSessionService,
            SessionOwnershipValidator sessionOwnershipValidator,
            SocialTrainingRepository socialTrainingRepository,
            SocialVoiceSessionTokenService tokenService,
            OpenAiRealtimeProperties realtimeProperties
    ) {
        this.trainingSessionService = trainingSessionService;
        this.sessionOwnershipValidator = sessionOwnershipValidator;
        this.socialTrainingRepository = socialTrainingRepository;
        this.tokenService = tokenService;
        this.realtimeProperties = realtimeProperties;
    }

    @Transactional(readOnly = true)
    public SocialVoiceSessionPrepareResponse prepare(CurrentUser currentUser, long sessionId) {
        sessionOwnershipValidator.validateOwner(sessionId, currentUser);
        TrainingSession session = trainingSessionService.getSession(sessionId);
        validateSession(session);

        long scenarioId = session.scenarioId();
        SocialScenarioDetailResponse scenario = socialTrainingRepository.findActiveScenarioDetail(scenarioId)
                .orElseThrow(() -> new TrainingServiceException(ErrorCode.NOT_FOUND, "Social scenario was not found."));
        String scenarioContext = scenarioContext(scenario);
        String openingScript = openingScript(scenario);
        String openingAudioUrl = openingAudioUrl(scenarioId, openingScript);
        SocialVoiceSessionToken token = tokenService.issue(
                sessionId,
                currentUser.userId(),
                scenarioId,
                scenarioContext,
                openingScript,
                openingAudioUrl
        );

        return new SocialVoiceSessionPrepareResponse(
                sessionId,
                scenarioId,
                CONNECTION_MODE,
                new RealtimeConnectionResponse(
                        WS_URL,
                        PROTOCOL,
                        token.token(),
                        SocialVoiceSessionTokenService.EXPIRES_IN_SECONDS
                ),
                new OpeningVoiceResponse(openingScript, openingAudioUrl, OPENING_AUDIO_STATUS_READY),
                new ConversationSettingsResponse(
                        realtimeProperties.voice(),
                        realtimeProperties.model(),
                        DEFAULT_INSTRUCTIONS_VERSION
                )
        );
    }

    private void validateSession(TrainingSession session) {
        if (session.trainingType() != TrainingType.SOCIAL) {
            throw new TrainingServiceException(ErrorCode.CONFLICT, "Only social training sessions can prepare voice relay.");
        }
        if (session.status() != TrainingSessionStatus.IN_PROGRESS) {
            throw new TrainingServiceException(ErrorCode.CONFLICT, "Only in-progress social training sessions can prepare voice relay.");
        }
        if (session.scenarioId() == null) {
            throw new TrainingServiceException(ErrorCode.CONFLICT, "Social training session does not have a scenario.");
        }
    }

    private String scenarioContext(SocialScenarioDetailResponse scenario) {
        String context = scenario.situationText();
        if (context == null || context.isBlank()) {
            context = scenario.title();
        }
        return context == null ? "" : context.trim();
    }

    private String openingScript(SocialScenarioDetailResponse scenario) {
        return "안녕하세요. " + scenarioContext(scenario) + " 상황을 함께 연습해볼게요.";
    }

    private String openingAudioUrl(long scenarioId, String openingScript) {
        return "/static/opening/social/%d/%08x.pcm".formatted(scenarioId, openingScript.hashCode());
    }
}
