package com.didgo.trainingservice.training.session.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.didgo.trainingservice.common.exception.ErrorCode;
import com.didgo.trainingservice.common.exception.TrainingServiceException;
import com.didgo.trainingservice.common.security.CurrentUser;
import com.didgo.trainingservice.training.session.repository.TrainingSessionOwnershipRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalLong;
import org.junit.jupiter.api.Test;

class SessionOwnershipValidatorTest {

    FakeTrainingSessionOwnershipRepository repository = new FakeTrainingSessionOwnershipRepository();
    SessionOwnershipValidator validator = new SessionOwnershipValidator(repository);

    @Test
    void passesWhenSessionBelongsToCurrentUser() {
        repository.save(10L, 1L);

        assertThatCode(() -> validator.validateOwner(10L, new CurrentUser(1L)))
                .doesNotThrowAnyException();
    }

    @Test
    void throwsNotFoundWhenSessionDoesNotExist() {
        assertThatThrownBy(() -> validator.validateOwner(999L, new CurrentUser(1L)))
                .isInstanceOfSatisfying(TrainingServiceException.class, exception -> {
                    assertThatErrorCode(exception, ErrorCode.NOT_FOUND);
                    assertThatMessage(exception, "Training session was not found.");
                });
    }

    @Test
    void throwsForbiddenWhenSessionBelongsToAnotherUser() {
        repository.save(10L, 2L);

        assertThatThrownBy(() -> validator.validateOwner(10L, new CurrentUser(1L)))
                .isInstanceOfSatisfying(TrainingServiceException.class, exception -> {
                    assertThatErrorCode(exception, ErrorCode.FORBIDDEN);
                    assertThatMessage(exception, "Training session belongs to another user.");
                });
    }

    private void assertThatErrorCode(TrainingServiceException exception, ErrorCode errorCode) {
        org.assertj.core.api.Assertions.assertThat(exception.errorCode()).isEqualTo(errorCode);
    }

    private void assertThatMessage(TrainingServiceException exception, String message) {
        org.assertj.core.api.Assertions.assertThat(exception.getMessage()).isEqualTo(message);
    }

    static class FakeTrainingSessionOwnershipRepository implements TrainingSessionOwnershipRepository {

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
}
