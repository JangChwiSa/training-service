package com.didgo.trainingservice.training.social.voice;

import static org.assertj.core.api.Assertions.assertThat;

import com.didgo.trainingservice.common.security.CurrentUser;
import com.didgo.trainingservice.training.session.entity.TrainingSession;
import com.didgo.trainingservice.training.session.entity.TrainingType;
import com.didgo.trainingservice.training.session.repository.TrainingSessionOwnershipRepository;
import com.didgo.trainingservice.training.session.repository.TrainingSessionRepository;
import com.didgo.trainingservice.training.session.service.SessionOwnershipValidator;
import com.didgo.trainingservice.training.session.service.TrainingSessionService;
import com.didgo.trainingservice.training.social.dto.SocialFeedbackResponse;
import com.didgo.trainingservice.training.social.dto.SocialScenarioDetailResponse;
import com.didgo.trainingservice.training.social.dto.SocialScenarioListItemResponse;
import com.didgo.trainingservice.training.social.entity.SocialJobType;
import com.didgo.trainingservice.training.social.repository.SocialTrainingRepository;
import com.didgo.trainingservice.training.social.voice.dto.SocialVoiceSessionPrepareResponse;
import com.didgo.trainingservice.training.social.voice.realtime.OpenAiRealtimeProperties;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import org.junit.jupiter.api.Test;

class SocialVoiceSessionServiceTest {

    FakeOwnershipRepository ownershipRepository = new FakeOwnershipRepository();
    FakeTrainingSessionRepository sessionRepository = new FakeTrainingSessionRepository();
    FakeSocialTrainingRepository socialRepository = new FakeSocialTrainingRepository();
    Clock clock = Clock.fixed(Instant.parse("2026-04-30T01:00:00Z"), ZoneId.of("Asia/Seoul"));
    SocialVoiceSessionTokenService tokenService = new SocialVoiceSessionTokenService(clock);
    SocialVoiceSessionService service = new SocialVoiceSessionService(
            new TrainingSessionService(sessionRepository, clock),
            new SessionOwnershipValidator(ownershipRepository),
            socialRepository,
            tokenService,
            new OpenAiRealtimeProperties(null, "gpt-realtime-mini", "marin", "audio/pcm", 24000, "audio/pcm")
    );

    @Test
    void preparesVoiceSessionWithShortLivedConnectionToken() {
        ownershipRepository.save(10L, 7L);
        sessionRepository.sessions.put(10L, TrainingSession.start(
                7L,
                TrainingType.SOCIAL,
                "OFFICE",
                1L,
                LocalDateTime.of(2026, 4, 30, 10, 0)
        ).withSessionId(10L));

        SocialVoiceSessionPrepareResponse response = service.prepare(new CurrentUser(7L), 10L);

        assertThat(response.sessionId()).isEqualTo(10L);
        assertThat(response.scenarioId()).isEqualTo(1L);
        assertThat(response.connectionMode()).isEqualTo("SERVER_RELAY");
        assertThat(response.realtime().wsUrl()).isEqualTo("/ws/trainings/social/voice");
        assertThat(response.realtime().connectionToken()).isNotBlank();
        assertThat(response.realtime().expiresInSeconds()).isEqualTo(300);
        assertThat(response.opening().script()).contains("A coworker looks upset.");
        assertThat(response.conversation().model()).isEqualTo("gpt-realtime-mini");
        assertThat(response.conversation().voice()).isEqualTo("marin");
    }

    static class FakeSocialTrainingRepository implements SocialTrainingRepository {

        @Override
        public List<SocialScenarioListItemResponse> findActiveScenariosByJobType(SocialJobType jobType) {
            return List.of();
        }

        @Override
        public Optional<SocialScenarioDetailResponse> findActiveScenarioDetail(long scenarioId) {
            return Optional.of(new SocialScenarioDetailResponse(
                    scenarioId,
                    SocialJobType.OFFICE,
                    "Offer help to a coworker",
                    "Office break room",
                    "A coworker looks upset.",
                    "How would you respond?",
                    1
            ));
        }

        @Override
        public boolean existsActiveScenario(long scenarioId, SocialJobType jobType) {
            return true;
        }

        @Override
        public Optional<SocialScoreRow> findScore(long sessionId) {
            return Optional.empty();
        }

        @Override
        public Optional<SocialFeedbackResponse> findFeedback(long sessionId) {
            return Optional.empty();
        }

        @Override
        public List<com.didgo.trainingservice.training.social.dto.SocialDialogLogResponse> findDialogLogs(long sessionId) {
            return List.of();
        }
    }

    static class FakeTrainingSessionRepository implements TrainingSessionRepository {

        Map<Long, TrainingSession> sessions = new HashMap<>();

        @Override
        public TrainingSession save(TrainingSession trainingSession) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<TrainingSession> findById(long sessionId) {
            return Optional.ofNullable(sessions.get(sessionId));
        }

        @Override
        public void update(TrainingSession trainingSession) {
            sessions.put(trainingSession.sessionId(), trainingSession);
        }
    }

    static class FakeOwnershipRepository implements TrainingSessionOwnershipRepository {

        Map<Long, Long> owners = new HashMap<>();

        void save(long sessionId, long userId) {
            owners.put(sessionId, userId);
        }

        @Override
        public OptionalLong findUserIdBySessionId(long sessionId) {
            Long userId = owners.get(sessionId);
            return userId == null ? OptionalLong.empty() : OptionalLong.of(userId);
        }
    }
}
