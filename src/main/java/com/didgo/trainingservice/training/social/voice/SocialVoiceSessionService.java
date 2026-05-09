package com.didgo.trainingservice.training.social.voice;

import com.didgo.trainingservice.common.exception.ErrorCode;
import com.didgo.trainingservice.common.exception.TrainingServiceException;
import com.didgo.trainingservice.common.security.CurrentUser;
import com.didgo.trainingservice.external.openai.OpenAiAdapterException;
import com.didgo.trainingservice.training.session.entity.TrainingSession;
import com.didgo.trainingservice.training.session.entity.TrainingSessionStatus;
import com.didgo.trainingservice.training.session.entity.TrainingType;
import com.didgo.trainingservice.training.session.service.SessionOwnershipValidator;
import com.didgo.trainingservice.training.session.service.TrainingSessionService;
import com.didgo.trainingservice.training.social.dto.SocialScenarioDetailResponse;
import com.didgo.trainingservice.training.social.repository.SocialTrainingRepository;
import com.didgo.trainingservice.training.social.voice.dto.ConversationSettingsResponse;
import com.didgo.trainingservice.training.social.voice.dto.OpeningVoiceResponse;
import com.didgo.trainingservice.training.social.voice.dto.RealtimeConnectionResponse;
import com.didgo.trainingservice.training.social.voice.dto.SocialVoiceSessionPrepareResponse;
import com.didgo.trainingservice.training.social.voice.realtime.OpenAiRealtimeProperties;
import com.didgo.trainingservice.training.social.voice.tts.OpenAiSpeechClient;
import com.didgo.trainingservice.training.social.voice.tts.OpenAiSpeechProperties;
import com.didgo.trainingservice.training.social.voice.tts.SocialOpeningAudioAsset;
import com.didgo.trainingservice.training.social.voice.tts.SocialOpeningAudioAssetRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SocialVoiceSessionService {

    private static final String CONNECTION_MODE = "SERVER_RELAY";
    private static final String WS_URL = "/ws/trainings/social/voice";
    private static final String PROTOCOL = "json";
    private static final String OPENING_AUDIO_STATUS_READY = "READY";
    private static final String OPENING_AUDIO_STATUS_UNAVAILABLE = "UNAVAILABLE";
    private static final String DEFAULT_INSTRUCTIONS_VERSION = "v1";

    private final TrainingSessionService trainingSessionService;
    private final SessionOwnershipValidator sessionOwnershipValidator;
    private final SocialTrainingRepository socialTrainingRepository;
    private final SocialVoiceSessionTokenService tokenService;
    private final OpenAiRealtimeProperties realtimeProperties;
    private final OpenAiSpeechProperties speechProperties;
    private final OpenAiSpeechClient speechClient;
    private final SocialOpeningAudioAssetRepository audioAssetRepository;

    public SocialVoiceSessionService(
            TrainingSessionService trainingSessionService,
            SessionOwnershipValidator sessionOwnershipValidator,
            SocialTrainingRepository socialTrainingRepository,
            SocialVoiceSessionTokenService tokenService,
            OpenAiRealtimeProperties realtimeProperties,
            OpenAiSpeechProperties speechProperties,
            OpenAiSpeechClient speechClient,
            SocialOpeningAudioAssetRepository audioAssetRepository
    ) {
        this.trainingSessionService = trainingSessionService;
        this.sessionOwnershipValidator = sessionOwnershipValidator;
        this.socialTrainingRepository = socialTrainingRepository;
        this.tokenService = tokenService;
        this.realtimeProperties = realtimeProperties;
        this.speechProperties = speechProperties;
        this.speechClient = speechClient;
        this.audioAssetRepository = audioAssetRepository;
    }

    @Transactional
    public SocialVoiceSessionPrepareResponse prepare(CurrentUser currentUser, long sessionId) {
        sessionOwnershipValidator.validateOwner(sessionId, currentUser);
        TrainingSession session = trainingSessionService.getSession(sessionId);
        validateSession(session);

        long scenarioId = session.scenarioId();
        SocialScenarioDetailResponse scenario = socialTrainingRepository.findActiveScenarioDetail(scenarioId)
                .orElseThrow(() -> new TrainingServiceException(ErrorCode.NOT_FOUND, "Social scenario was not found."));
        String scenarioContext = scenarioContext(scenario);
        String openingScript = openingScript(scenario);
        OpeningVoiceResponse opening = openingVoice(scenarioId, openingScript);
        SocialVoiceSessionToken token = tokenService.issue(
                sessionId,
                currentUser.userId(),
                scenarioId,
                scenarioContext,
                openingScript,
                opening.audioUrl() == null ? "" : opening.audioUrl()
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
                opening,
                new ConversationSettingsResponse(
                        realtimeProperties.voice(),
                        realtimeProperties.model(),
                        DEFAULT_INSTRUCTIONS_VERSION
                )
        );
    }

    @Transactional
    public OpeningVoiceResponse openingVoice(long scenarioId) {
        SocialScenarioDetailResponse scenario = socialTrainingRepository.findActiveScenarioDetail(scenarioId)
                .orElseThrow(() -> new TrainingServiceException(ErrorCode.NOT_FOUND, "Social scenario was not found."));
        return openingVoice(scenarioId, openingScript(scenario));
    }

    @Transactional
    public SocialOpeningAudioAsset openingAudioAsset(String cacheKey) {
        return audioAssetRepository.findByCacheKey(cacheKey)
                .orElseThrow(() -> new TrainingServiceException(ErrorCode.NOT_FOUND, "Opening audio asset was not found."));
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
        String request = extractCounterpartRequest(scenario.situationText());
        if (request.isBlank()) {
            request = scenarioContext(scenario);
        }
        return request;
    }

    private OpeningVoiceResponse openingVoice(long scenarioId, String openingScript) {
        String cacheKey = cacheKey(openingScript);
        String audioUrl = "/api/trainings/social/opening-audio/%s.%s".formatted(
                cacheKey,
                speechProperties.responseFormat()
        );
        if (audioAssetRepository.findByCacheKey(cacheKey).isPresent()) {
            return new OpeningVoiceResponse(openingScript, audioUrl, OPENING_AUDIO_STATUS_READY);
        }

        try {
            byte[] audioData = speechClient.createSpeech(openingScript);
            audioAssetRepository.save(new SocialOpeningAudioAsset(
                    cacheKey,
                    scenarioId,
                    openingScript,
                    speechProperties.model(),
                    speechProperties.voice(),
                    speechProperties.responseFormat(),
                    speechProperties.contentType(),
                    audioData
            ));
            return new OpeningVoiceResponse(openingScript, audioUrl, OPENING_AUDIO_STATUS_READY);
        } catch (OpenAiAdapterException exception) {
            return new OpeningVoiceResponse(openingScript, null, OPENING_AUDIO_STATUS_UNAVAILABLE);
        }
    }

    private String cacheKey(String openingScript) {
        String source = "%s|%s|%s|%s".formatted(
                speechProperties.model(),
                speechProperties.voice(),
                speechProperties.responseFormat(),
                openingScript
        );
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(source.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(hash.length * 2);
            for (byte value : hash) {
                builder.append("%02x".formatted(value));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available.", exception);
        }
    }

    private String extractCounterpartRequest(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        String value = text.trim();
        String[] quotePairs = {"\"", "\"", "“", "”", "‘", "’", "'", "'"};
        String extracted = "";
        for (int index = 0; index < quotePairs.length; index += 2) {
            String open = quotePairs[index];
            String close = quotePairs[index + 1];
            int start = value.indexOf(open);
            while (start >= 0) {
                int end = value.indexOf(close, start + open.length());
                if (end < 0) {
                    break;
                }
                String candidate = value.substring(start + open.length(), end).trim();
                if (!candidate.isBlank()) {
                    extracted = candidate;
                }
                start = value.indexOf(open, end + close.length());
            }
        }
        return extracted;
    }
}
