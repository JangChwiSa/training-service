package com.jangchwisa.trainingservice.training.evaluation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.jangchwisa.trainingservice.training.session.entity.TrainingType;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TrainingEvaluationCommandTest {

    @Test
    void rejectsUserIdentityInMetrics() {
        assertThatThrownBy(() -> new TrainingEvaluationCommand(
                TrainingType.SOCIAL,
                "scenario",
                List.of(),
                Map.of("user_id", 1L),
                70,
                "AI_EVALUATION",
                "시스템 피드백",
                "상세",
                false
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Training evaluation command must not include user identity.");
    }
}
