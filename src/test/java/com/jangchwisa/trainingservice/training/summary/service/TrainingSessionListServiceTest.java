package com.jangchwisa.trainingservice.training.summary.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.jangchwisa.trainingservice.training.safety.entity.SafetyCategory;
import com.jangchwisa.trainingservice.training.session.entity.TrainingType;
import com.jangchwisa.trainingservice.training.summary.dto.TrainingSessionListItemResponse;
import com.jangchwisa.trainingservice.training.summary.dto.TrainingSessionListResponse;
import com.jangchwisa.trainingservice.training.summary.repository.TrainingSessionSummaryRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class TrainingSessionListServiceTest {

    FakeTrainingSessionSummaryRepository repository = new FakeTrainingSessionSummaryRepository();
    TrainingSessionListService service = new TrainingSessionListService(repository);

    @Test
    void returnsPagedSessionSummaryList() {
        repository.totalElements = 2L;
        repository.sessions = List.of(new TrainingSessionListItemResponse(
                10L,
                1L,
                "동료에게 도움 요청하기",
                null,
                85,
                "좋은 대화 흐름입니다.",
                null,
                null,
                null,
                null,
                null,
                null,
                LocalDateTime.of(2026, 4, 27, 10, 0)
        ));

        TrainingSessionListResponse response = service.getSessions(1L, TrainingType.SOCIAL, null, 0, 10);

        assertThat(response.trainingType()).isEqualTo(TrainingType.SOCIAL);
        assertThat(response.page()).isZero();
        assertThat(response.size()).isEqualTo(10);
        assertThat(response.totalElements()).isEqualTo(2L);
        assertThat(response.sessions()).hasSize(1);
    }

    @Test
    void appliesCategoryOnlyForSafetyTraining() {
        service.getSessions(1L, TrainingType.SOCIAL, SafetyCategory.COMMUTE_SAFETY, 0, 10);

        assertThat(repository.category).isNull();

        service.getSessions(1L, TrainingType.SAFETY, SafetyCategory.COMMUTE_SAFETY, 0, 10);

        assertThat(repository.category).isEqualTo(SafetyCategory.COMMUTE_SAFETY);
    }

    static class FakeTrainingSessionSummaryRepository implements TrainingSessionSummaryRepository {

        long totalElements;
        List<TrainingSessionListItemResponse> sessions = List.of();
        SafetyCategory category;

        @Override
        public long countByUserIdAndTrainingType(long userId, TrainingType trainingType, SafetyCategory category) {
            this.category = category;
            return totalElements;
        }

        @Override
        public List<TrainingSessionListItemResponse> findByUserIdAndTrainingType(
                long userId,
                TrainingType trainingType,
                SafetyCategory category,
                int page,
                int size
        ) {
            this.category = category;
            return sessions;
        }
    }
}
