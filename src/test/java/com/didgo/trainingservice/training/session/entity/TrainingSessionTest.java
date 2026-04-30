package com.didgo.trainingservice.training.session.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.didgo.trainingservice.common.exception.ErrorCode;
import com.didgo.trainingservice.common.exception.TrainingServiceException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class TrainingSessionTest {

    LocalDateTime startedAt = LocalDateTime.of(2026, 4, 28, 10, 0);

    @Test
    void startsSessionInProgress() {
        TrainingSession session = TrainingSession.start(1L, TrainingType.SOCIAL, "OFFICE", 10L, startedAt);

        assertThat(session.sessionId()).isNull();
        assertThat(session.userId()).isEqualTo(1L);
        assertThat(session.trainingType()).isEqualTo(TrainingType.SOCIAL);
        assertThat(session.subType()).isEqualTo("OFFICE");
        assertThat(session.scenarioId()).isEqualTo(10L);
        assertThat(session.status()).isEqualTo(TrainingSessionStatus.IN_PROGRESS);
        assertThat(session.currentStep()).isZero();
        assertThat(session.startedAt()).isEqualTo(startedAt);
        assertThat(session.endedAt()).isNull();
    }

    @Test
    void requiresSubTypeForFocusSession() {
        assertThatThrownBy(() -> TrainingSession.start(1L, TrainingType.FOCUS, null, null, startedAt))
                .isInstanceOfSatisfying(TrainingServiceException.class, exception -> {
                    assertThat(exception.errorCode()).isEqualTo(ErrorCode.VALIDATION_ERROR);
                    assertThat(exception.getMessage()).isEqualTo("Focus training session requires sub type.");
                });
    }

    @Test
    void completesInProgressSession() {
        LocalDateTime completedAt = startedAt.plusMinutes(5);
        TrainingSession session = TrainingSession.start(1L, TrainingType.SAFETY, null, 20L, startedAt)
                .withSessionId(100L);

        TrainingSession completed = session.complete(completedAt);

        assertThat(completed.status()).isEqualTo(TrainingSessionStatus.COMPLETED);
        assertThat(completed.endedAt()).isEqualTo(completedAt);
    }

    @Test
    void failsInProgressSession() {
        LocalDateTime failedAt = startedAt.plusMinutes(1);
        TrainingSession session = TrainingSession.start(1L, TrainingType.DOCUMENT, null, null, startedAt)
                .withSessionId(100L);

        TrainingSession failed = session.fail(failedAt);

        assertThat(failed.status()).isEqualTo(TrainingSessionStatus.FAILED);
        assertThat(failed.endedAt()).isEqualTo(failedAt);
    }

    @Test
    void rejectsDuplicateCompletionTransition() {
        TrainingSession completed = TrainingSession.start(1L, TrainingType.SOCIAL, "OFFICE", 10L, startedAt)
                .withSessionId(100L)
                .complete(startedAt.plusMinutes(5));

        assertThatThrownBy(() -> completed.complete(startedAt.plusMinutes(6)))
                .isInstanceOfSatisfying(TrainingServiceException.class, exception -> {
                    assertThat(exception.errorCode()).isEqualTo(ErrorCode.CONFLICT);
                    assertThat(exception.getMessage()).isEqualTo("Only in-progress training session can be completed.");
                });
    }

    @Test
    void advancesCurrentStepWithoutMovingBackward() {
        TrainingSession session = TrainingSession.start(1L, TrainingType.SAFETY, null, 20L, startedAt)
                .withSessionId(100L);

        TrainingSession advanced = session.advanceToStep(2);

        assertThat(advanced.currentStep()).isEqualTo(2);
        assertThatThrownBy(() -> advanced.advanceToStep(1))
                .isInstanceOfSatisfying(TrainingServiceException.class, exception -> {
                    assertThat(exception.errorCode()).isEqualTo(ErrorCode.CONFLICT);
                    assertThat(exception.getMessage()).isEqualTo("Current step cannot move backward.");
                });
    }
}
