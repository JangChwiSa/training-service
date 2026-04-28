package com.jangchwisa.trainingservice.external.openai;

import static org.assertj.core.api.Assertions.assertThat;

import com.jangchwisa.trainingservice.external.openai.dto.TrainingEvaluationLog;
import com.jangchwisa.trainingservice.external.openai.dto.TrainingEvaluationRequest;
import com.jangchwisa.trainingservice.external.openai.dto.TrainingEvaluationResult;
import com.jangchwisa.trainingservice.training.session.entity.TrainingType;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class LocalFakeOpenAiTrainingEvaluationAdapterTest {

    @Test
    void evaluatesWithoutCallingRealOpenAiApi() {
        OpenAiProperties properties = new OpenAiProperties(null, 30000, "local-fake");
        TrainingEvaluationAdapter adapter = new LocalFakeOpenAiTrainingEvaluationAdapter(properties);

        TrainingEvaluationResult result = adapter.evaluate(new TrainingEvaluationRequest(
                TrainingType.SOCIAL,
                "동료에게 도움 요청하기",
                List.of(
                        new TrainingEvaluationLog("USER", "도와주실 수 있나요?"),
                        new TrainingEvaluationLog("AI", "어떤 부분이 어려우신가요?")
                ),
                Map.of("turnCount", 2)
        ));

        assertThat(result.score()).isEqualTo(73);
        assertThat(result.scoreType()).isEqualTo("AI_EVALUATION");
        assertThat(result.feedback().summary()).contains("SOCIAL");
        assertThat(result.rawMetricsJson()).contains("local-fake");
    }
}
