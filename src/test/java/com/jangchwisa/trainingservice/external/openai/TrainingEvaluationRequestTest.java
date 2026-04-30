package com.jangchwisa.trainingservice.external.openai;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.jangchwisa.trainingservice.external.openai.dto.TrainingEvaluationRequest;
import com.jangchwisa.trainingservice.training.session.entity.TrainingType;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TrainingEvaluationRequestTest {

    @Test
    void rejectsUserIdentityInMetrics() {
        assertThatThrownBy(() -> new TrainingEvaluationRequest(
                TrainingType.SOCIAL,
                "scenario",
                List.of(),
                Map.of("userId", 1L)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("OpenAI evaluation request must not include user identity.");
    }
}
