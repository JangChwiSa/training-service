package com.didgo.trainingservice.external.openai;

import static org.assertj.core.api.Assertions.assertThat;

import com.didgo.trainingservice.external.openai.dto.TrainingEvaluationLog;
import com.didgo.trainingservice.external.openai.dto.TrainingEvaluationRequest;
import com.didgo.trainingservice.external.openai.dto.TrainingEvaluationResult;
import com.didgo.trainingservice.training.session.entity.TrainingType;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class LocalFakeOpenAiTrainingEvaluationAdapterTest {

    @Test
    void evaluatesWithoutCallingRealOpenAiApi() {
        OpenAiProperties properties = new OpenAiProperties(null, 30000, "local-fake", null, null);
        TrainingEvaluationAdapter adapter = new LocalFakeOpenAiTrainingEvaluationAdapter(properties);

        TrainingEvaluationResult result = adapter.evaluate(new TrainingEvaluationRequest(
                TrainingType.SOCIAL,
                "?숇즺?먭쾶 ?꾩? ?붿껌?섍린",
                List.of(
                        new TrainingEvaluationLog("USER", "?꾩?二쇱떎 ???덈굹??"),
                        new TrainingEvaluationLog("AI", "?대뼡 遺遺꾩씠 ?대젮?곗떊媛??")
                ),
                Map.of("turnCount", 2)
        ));

        assertThat(result.score()).isEqualTo(73);
        assertThat(result.scoreType()).isEqualTo("AI_EVALUATION");
        assertThat(result.feedback().summary()).contains("SOCIAL");
        assertThat(result.rawMetricsJson()).contains("local-fake");
    }
}
