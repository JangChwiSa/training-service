package com.jangchwisa.trainingservice.training.progress.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.jangchwisa.trainingservice.training.progress.dto.DocumentProgressResponse;
import com.jangchwisa.trainingservice.training.progress.dto.FocusProgressResponse;
import com.jangchwisa.trainingservice.training.progress.dto.SafetyProgressResponse;
import com.jangchwisa.trainingservice.training.progress.dto.SocialProgressResponse;
import com.jangchwisa.trainingservice.training.progress.dto.TrainingProgressResponse;
import com.jangchwisa.trainingservice.training.progress.repository.TrainingProgressRepository;
import com.jangchwisa.trainingservice.training.session.entity.TrainingType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class TrainingProgressServiceTest {

    FakeTrainingProgressRepository repository = new FakeTrainingProgressRepository();
    TrainingProgressService service = new TrainingProgressService(repository);

    @Test
    void returnsSocialProgress() {
        repository.socialProgress = Optional.of(new SocialProgressResponse(
                TrainingType.SOCIAL,
                10L,
                85,
                "좋은 대화 흐름입니다.",
                3,
                LocalDateTime.of(2026, 4, 27, 10, 0)
        ));

        TrainingProgressResponse response = service.getProgress(1L, TrainingType.SOCIAL);

        assertThat(response).isEqualTo(repository.socialProgress.orElseThrow());
    }

    @Test
    void returnsDefaultSocialProgressWhenDataDoesNotExist() {
        TrainingProgressResponse response = service.getProgress(1L, TrainingType.SOCIAL);

        assertThat(response).isEqualTo(new SocialProgressResponse(TrainingType.SOCIAL, null, null, null, 0, null));
    }

    @Test
    void returnsDefaultSafetyProgressWhenDataDoesNotExist() {
        TrainingProgressResponse response = service.getProgress(1L, TrainingType.SAFETY);

        assertThat(response).isEqualTo(new SafetyProgressResponse(TrainingType.SAFETY, null, 0, 0, 0, null));
    }

    @Test
    void returnsDefaultDocumentProgressWhenDataDoesNotExist() {
        TrainingProgressResponse response = service.getProgress(1L, TrainingType.DOCUMENT);

        assertThat(response).isEqualTo(new DocumentProgressResponse(TrainingType.DOCUMENT, null, 0, 0, null, 0, null));
    }

    @Test
    void returnsDefaultFocusProgressWhenDataDoesNotExist() {
        TrainingProgressResponse response = service.getProgress(1L, TrainingType.FOCUS);

        assertThat(response).isEqualTo(new FocusProgressResponse(TrainingType.FOCUS, 1, 1, null, null, null, null));
    }

    @Test
    void returnsFocusProgress() {
        repository.focusProgress = Optional.of(new FocusProgressResponse(
                TrainingType.FOCUS,
                3,
                4,
                2,
                BigDecimal.valueOf(92.5),
                820,
                LocalDateTime.of(2026, 4, 27, 11, 0)
        ));

        TrainingProgressResponse response = service.getProgress(1L, TrainingType.FOCUS);

        assertThat(response).isEqualTo(repository.focusProgress.orElseThrow());
    }

    static class FakeTrainingProgressRepository implements TrainingProgressRepository {

        Optional<SocialProgressResponse> socialProgress = Optional.empty();
        Optional<SafetyProgressResponse> safetyProgress = Optional.empty();
        Optional<DocumentProgressResponse> documentProgress = Optional.empty();
        Optional<FocusProgressResponse> focusProgress = Optional.empty();

        @Override
        public Optional<SocialProgressResponse> findSocialProgress(long userId) {
            return socialProgress;
        }

        @Override
        public Optional<SafetyProgressResponse> findSafetyProgress(long userId) {
            return safetyProgress;
        }

        @Override
        public Optional<DocumentProgressResponse> findDocumentProgress(long userId) {
            return documentProgress;
        }

        @Override
        public Optional<FocusProgressResponse> findFocusProgress(long userId) {
            return focusProgress;
        }
    }
}
