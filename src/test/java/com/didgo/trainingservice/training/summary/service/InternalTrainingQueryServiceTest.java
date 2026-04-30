package com.didgo.trainingservice.training.summary.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.didgo.trainingservice.training.session.entity.TrainingType;
import com.didgo.trainingservice.training.summary.dto.InternalTrainingSummaryResponse;
import com.didgo.trainingservice.training.summary.dto.LatestTrainingResultResponse;
import com.didgo.trainingservice.training.summary.dto.LatestTrainingResultsResponse;
import com.didgo.trainingservice.training.summary.repository.InternalTrainingQueryRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class InternalTrainingQueryServiceTest {

    FakeInternalTrainingQueryRepository repository = new FakeInternalTrainingQueryRepository();
    InternalTrainingQueryService service = new InternalTrainingQueryService(repository);

    @Test
    void returnsSummaryFromProgressTablesRepository() {
        repository.summary = new InternalTrainingSummaryResponse(85, 7, 10, 8, 10, 3);

        InternalTrainingSummaryResponse response = service.getSummary(1L);

        assertThat(response).isEqualTo(repository.summary);
    }

    @Test
    void wrapsLatestResultsWithUserId() {
        repository.latestResults = List.of(new LatestTrainingResultResponse(
                10L,
                TrainingType.SOCIAL,
                85,
                "AI_EVALUATION",
                LocalDateTime.of(2026, 4, 27, 10, 30)
        ));

        LatestTrainingResultsResponse response = service.getLatestResults(1L);

        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.results()).isEqualTo(repository.latestResults);
    }

    static class FakeInternalTrainingQueryRepository implements InternalTrainingQueryRepository {

        InternalTrainingSummaryResponse summary = new InternalTrainingSummaryResponse(null, 0, 0, 0, 0, 1);
        List<LatestTrainingResultResponse> latestResults = List.of();

        @Override
        public InternalTrainingSummaryResponse findTrainingSummary(long userId) {
            return summary;
        }

        @Override
        public List<LatestTrainingResultResponse> findLatestTrainingResults(long userId) {
            return latestResults;
        }
    }
}
