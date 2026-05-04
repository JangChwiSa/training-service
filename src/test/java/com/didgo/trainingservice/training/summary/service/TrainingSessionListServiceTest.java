package com.didgo.trainingservice.training.summary.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.didgo.trainingservice.training.session.entity.TrainingType;
import com.didgo.trainingservice.training.summary.dto.TrainingSessionListItemResponse;
import com.didgo.trainingservice.training.summary.dto.TrainingSessionListResponse;
import com.didgo.trainingservice.training.summary.repository.TrainingSessionSummaryRepository;
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
                "?숇즺?먭쾶 ?꾩? ?붿껌?섍린",
                null,
                85,
                "醫뗭? ????먮쫫?낅땲??",
                null,
                null,
                null,
                null,
                null,
                null,
                LocalDateTime.of(2026, 4, 27, 10, 0)
        ));

        TrainingSessionListResponse response = service.getSessions(1L, TrainingType.SOCIAL, 0, 10);

        assertThat(response.trainingType()).isEqualTo(TrainingType.SOCIAL);
        assertThat(response.page()).isZero();
        assertThat(response.size()).isEqualTo(10);
        assertThat(response.totalElements()).isEqualTo(2L);
        assertThat(response.sessions()).hasSize(1);
    }

    @Test
    void queriesByTrainingTypeWithoutCategoryFilter() {
        service.getSessions(1L, TrainingType.SAFETY, 0, 10);

        assertThat(repository.trainingType).isEqualTo(TrainingType.SAFETY);
    }

    static class FakeTrainingSessionSummaryRepository implements TrainingSessionSummaryRepository {

        long totalElements;
        List<TrainingSessionListItemResponse> sessions = List.of();
        TrainingType trainingType;

        @Override
        public long countByUserIdAndTrainingType(long userId, TrainingType trainingType) {
            this.trainingType = trainingType;
            return totalElements;
        }

        @Override
        public List<TrainingSessionListItemResponse> findByUserIdAndTrainingType(
                long userId,
                TrainingType trainingType,
                int page,
                int size
        ) {
            this.trainingType = trainingType;
            return sessions;
        }
    }
}
