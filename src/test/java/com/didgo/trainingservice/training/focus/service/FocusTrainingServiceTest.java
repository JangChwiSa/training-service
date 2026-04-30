package com.didgo.trainingservice.training.focus.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.didgo.trainingservice.common.exception.ErrorCode;
import com.didgo.trainingservice.common.exception.TrainingServiceException;
import com.didgo.trainingservice.common.security.CurrentUser;
import com.didgo.trainingservice.event.outbox.OutboxEvent;
import com.didgo.trainingservice.event.outbox.OutboxEventRepository;
import com.didgo.trainingservice.training.completion.TrainingCompletionService;
import com.didgo.trainingservice.training.feedback.entity.TrainingFeedback;
import com.didgo.trainingservice.training.feedback.repository.TrainingFeedbackRepository;
import com.didgo.trainingservice.training.focus.dto.CompleteFocusSessionRequest;
import com.didgo.trainingservice.training.focus.dto.FocusCommandResponse;
import com.didgo.trainingservice.training.focus.dto.FocusProgressResponse;
import com.didgo.trainingservice.training.focus.dto.FocusReactionRequest;
import com.didgo.trainingservice.training.focus.dto.StartFocusSessionResponse;
import com.didgo.trainingservice.training.focus.repository.FocusTrainingRepository;
import com.didgo.trainingservice.training.progress.dto.DocumentProgressResponse;
import com.didgo.trainingservice.training.progress.dto.SafetyProgressResponse;
import com.didgo.trainingservice.training.progress.dto.SocialProgressResponse;
import com.didgo.trainingservice.training.progress.entity.TrainingProgressCompletion;
import com.didgo.trainingservice.training.progress.repository.TrainingProgressRepository;
import com.didgo.trainingservice.training.score.entity.TrainingScore;
import com.didgo.trainingservice.training.score.repository.TrainingScoreRepository;
import com.didgo.trainingservice.training.session.entity.TrainingSession;
import com.didgo.trainingservice.training.session.entity.TrainingType;
import com.didgo.trainingservice.training.session.repository.TrainingSessionOwnershipRepository;
import com.didgo.trainingservice.training.session.repository.TrainingSessionRepository;
import com.didgo.trainingservice.training.session.service.SessionOwnershipValidator;
import com.didgo.trainingservice.training.session.service.TrainingSessionService;
import com.didgo.trainingservice.training.summary.entity.TrainingSessionSummary;
import com.didgo.trainingservice.training.summary.repository.TrainingSessionSummaryRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import org.junit.jupiter.api.Test;

class FocusTrainingServiceTest {

    FakeFocusTrainingRepository focusRepository = new FakeFocusTrainingRepository();
    FakeOwnershipRepository ownershipRepository = new FakeOwnershipRepository();
    FakeTrainingSessionRepository sessionRepository = new FakeTrainingSessionRepository(ownershipRepository);
    FocusTrainingService service = new FocusTrainingService(
            focusRepository,
            new TrainingSessionService(
                    sessionRepository,
                    Clock.fixed(Instant.parse("2026-04-28T01:00:00Z"), ZoneId.of("Asia/Seoul"))
            ),
            new FocusCommandGenerator()
    );

    @Test
    void returnsDefaultProgressWhenProgressDoesNotExist() {
        FocusProgressResponse response = service.getProgress(1L);

        assertThat(response).isEqualTo(new FocusProgressResponse(1, 1, null, null, null));
    }

    @Test
    void startsFocusSessionAndStoresLevelAsSubType() {
        focusRepository.progress = Optional.of(new FocusProgressResponse(2, 2, 1, BigDecimal.valueOf(90), 700));
        focusRepository.rule = Optional.of(new FocusTrainingRepository.FocusLevelRuleRow(
                2,
                6,
                3000,
                "SIMPLE",
                BigDecimal.valueOf(80)
        ));

        StartFocusSessionResponse response = service.startSession(new CurrentUser(7L), 2);

        TrainingSession savedSession = sessionRepository.sessions.get(response.sessionId());
        assertThat(savedSession.trainingType()).isEqualTo(TrainingType.FOCUS);
        assertThat(savedSession.subType()).isEqualTo("2");
        assertThat(response.durationSeconds()).isEqualTo(6);
        assertThat(response.commands()).hasSize(2);
        assertThat(response.commands().getFirst().order()).isEqualTo(1);
        assertThat(response.commands().getFirst().displayAtMs()).isZero();
        assertThat(focusRepository.savedCommands).hasSize(2);
    }

    @Test
    void rejectsLockedLevel() {
        focusRepository.progress = Optional.of(new FocusProgressResponse(1, 1, null, null, null));
        focusRepository.rule = Optional.of(new FocusTrainingRepository.FocusLevelRuleRow(
                2,
                180,
                3000,
                "SIMPLE",
                BigDecimal.valueOf(80)
        ));

        assertThatThrownBy(() -> service.startSession(new CurrentUser(7L), 2))
                .isInstanceOfSatisfying(TrainingServiceException.class, exception -> {
                    assertThat(exception.errorCode()).isEqualTo(ErrorCode.FORBIDDEN);
                    assertThat(exception.getMessage()).isEqualTo("Focus level is locked.");
                });
    }

    @Test
    void rejectsUnknownLevelRule() {
        assertThatThrownBy(() -> service.startSession(new CurrentUser(7L), 99))
                .isInstanceOfSatisfying(TrainingServiceException.class, exception -> {
                    assertThat(exception.errorCode()).isEqualTo(ErrorCode.NOT_FOUND);
                    assertThat(exception.getMessage()).isEqualTo("Focus level rule was not found.");
                });
    }

    @Test
    void rejectsAnotherUsersCompletionRequest() {
        ownershipRepository.save(10L, 2L);
        focusRepository.sessionLevel = Optional.of(2);
        focusRepository.rule = Optional.of(new FocusTrainingRepository.FocusLevelRuleRow(
                2,
                6,
                3000,
                "SIMPLE",
                BigDecimal.valueOf(80)
        ));
        focusRepository.commands = List.of(new FocusTrainingRepository.FocusCommandRow(101L, 1, "Tap red", "RED"));
        FocusTrainingService completionEnabledService = new FocusTrainingService(
                focusRepository,
                new TrainingSessionService(
                        sessionRepository,
                        Clock.fixed(Instant.parse("2026-04-28T01:00:00Z"), ZoneId.of("Asia/Seoul"))
                ),
                new FocusCommandGenerator(),
                new SessionOwnershipValidator(ownershipRepository),
                new TrainingCompletionService(
                        sessionRepository,
                        new CapturingTrainingScoreRepository(),
                        new NoOpTrainingFeedbackRepository(),
                        new NoOpTrainingProgressRepository(),
                        new NoOpTrainingSessionSummaryRepository(),
                        new CapturingOutboxEventRepository(),
                        Clock.fixed(Instant.parse("2026-04-28T01:30:00Z"), ZoneId.of("Asia/Seoul"))
                )
        );
        sessionRepository.sessions.put(10L, TrainingSession.start(
                2L,
                TrainingType.FOCUS,
                "2",
                null,
                LocalDateTime.of(2026, 4, 28, 10, 0)
        ).withSessionId(10L));

        assertThatThrownBy(() -> completionEnabledService.completeSession(
                new CurrentUser(1L),
                10L,
                new CompleteFocusSessionRequest(List.of(new FocusReactionRequest(101L, "RED", 500)))
        ))
                .isInstanceOfSatisfying(TrainingServiceException.class, exception -> {
                    assertThat(exception.errorCode()).isEqualTo(ErrorCode.FORBIDDEN);
                    assertThat(exception.getMessage()).isEqualTo("Training session belongs to another user.");
                });

        assertThat(focusRepository.savedReactions).isEmpty();
    }

    static class FakeFocusTrainingRepository implements FocusTrainingRepository {

        Optional<FocusProgressResponse> progress = Optional.empty();
        Optional<FocusLevelRuleRow> rule = Optional.empty();
        List<NewFocusCommand> savedCommands = new ArrayList<>();
        Optional<Integer> sessionLevel = Optional.empty();
        List<FocusCommandRow> commands = List.of();
        List<ScoredFocusReaction> savedReactions = List.of();

        @Override
        public Optional<FocusProgressResponse> findProgress(long userId) {
            return progress;
        }

        @Override
        public Optional<FocusLevelRuleRow> findActiveLevelRule(int level) {
            return rule;
        }

        @Override
        public List<FocusCommandResponse> saveCommands(long sessionId, List<NewFocusCommand> commands) {
            savedCommands.addAll(commands);
            List<FocusCommandResponse> responses = new ArrayList<>();
            long commandId = 1000L;
            for (NewFocusCommand command : commands) {
                responses.add(new FocusCommandResponse(
                        commandId++,
                        command.order(),
                        command.commandText(),
                        command.expectedAction(),
                        command.displayAtMs()
                ));
            }
            return responses;
        }

        @Override
        public Optional<Integer> findSessionLevel(long sessionId) {
            return sessionLevel;
        }

        @Override
        public List<FocusCommandRow> findCommands(long sessionId) {
            return commands;
        }

        @Override
        public void saveReactionLogs(long sessionId, List<ScoredFocusReaction> reactions) {
            savedReactions = List.copyOf(reactions);
        }
    }

    static class FakeTrainingSessionRepository implements TrainingSessionRepository {

        private final FakeOwnershipRepository ownershipRepository;
        private final Map<Long, TrainingSession> sessions = new HashMap<>();
        private long sequence = 1L;

        FakeTrainingSessionRepository(FakeOwnershipRepository ownershipRepository) {
            this.ownershipRepository = ownershipRepository;
        }

        @Override
        public TrainingSession save(TrainingSession trainingSession) {
            TrainingSession savedSession = trainingSession.withSessionId(sequence++);
            sessions.put(savedSession.sessionId(), savedSession);
            ownershipRepository.save(savedSession.sessionId(), savedSession.userId());
            return savedSession;
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

        private final Map<Long, Long> ownerUserIds = new HashMap<>();

        void save(long sessionId, long userId) {
            ownerUserIds.put(sessionId, userId);
        }

        @Override
        public OptionalLong findUserIdBySessionId(long sessionId) {
            Long userId = ownerUserIds.get(sessionId);
            if (userId == null) {
                return OptionalLong.empty();
            }
            return OptionalLong.of(userId);
        }
    }

    private static class CapturingTrainingScoreRepository implements TrainingScoreRepository {

        private TrainingScore saved;

        @Override
        public void save(TrainingScore trainingScore) {
            this.saved = trainingScore;
        }
    }

    private static class CapturingOutboxEventRepository implements OutboxEventRepository {

        private OutboxEvent saved;

        @Override
        public void save(OutboxEvent outboxEvent) {
            this.saved = outboxEvent;
        }
    }

    private static class NoOpTrainingFeedbackRepository implements TrainingFeedbackRepository {

        @Override
        public void save(TrainingFeedback trainingFeedback) {
        }
    }

    private static class NoOpTrainingProgressRepository implements TrainingProgressRepository {

        @Override
        public void applyCompletion(TrainingProgressCompletion completion) {
        }

        @Override
        public Optional<SocialProgressResponse> findSocialProgress(long userId) {
            return Optional.empty();
        }

        @Override
        public Optional<SafetyProgressResponse> findSafetyProgress(long userId) {
            return Optional.empty();
        }

        @Override
        public Optional<DocumentProgressResponse> findDocumentProgress(long userId) {
            return Optional.empty();
        }

        @Override
        public Optional<com.didgo.trainingservice.training.progress.dto.FocusProgressResponse> findFocusProgress(
                long userId
        ) {
            return Optional.empty();
        }
    }

    private static class NoOpTrainingSessionSummaryRepository implements TrainingSessionSummaryRepository {

        @Override
        public void save(TrainingSessionSummary summary) {
        }

        @Override
        public long countByUserIdAndTrainingType(
                long userId,
                TrainingType trainingType
        ) {
            return 0;
        }

        @Override
        public List<com.didgo.trainingservice.training.summary.dto.TrainingSessionListItemResponse> findByUserIdAndTrainingType(
                long userId,
                TrainingType trainingType,
                int page,
                int size
        ) {
            return List.of();
        }
    }
}
