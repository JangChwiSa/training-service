package com.jangchwisa.trainingservice.external.openai;

import static org.assertj.core.api.Assertions.assertThat;

import com.jangchwisa.trainingservice.external.openai.dto.TrainingEvaluationFeedback;
import com.jangchwisa.trainingservice.external.openai.dto.TrainingEvaluationResult;
import com.jangchwisa.trainingservice.external.openai.dto.TrainingEvaluationStorageModel;
import org.junit.jupiter.api.Test;

class TrainingEvaluationResultMapperTest {

    TrainingEvaluationResultMapper mapper = new TrainingEvaluationResultMapper();

    @Test
    void convertsEvaluationResultToScoreAndFeedbackStorageModel() {
        TrainingEvaluationResult result = new TrainingEvaluationResult(
                85,
                "AI_EVALUATION",
                new TrainingEvaluationFeedback("요약 피드백", "상세 피드백"),
                "{\"rubric\":\"social\"}"
        );

        TrainingEvaluationStorageModel storageModel = mapper.toStorageModel(result);

        assertThat(storageModel.score().score()).isEqualTo(85);
        assertThat(storageModel.score().scoreType()).isEqualTo("AI_EVALUATION");
        assertThat(storageModel.score().rawMetricsJson()).isEqualTo("{\"rubric\":\"social\"}");
        assertThat(storageModel.feedback().feedbackType()).isEqualTo("SUMMARY");
        assertThat(storageModel.feedback().feedbackSource()).isEqualTo("AI");
        assertThat(storageModel.feedback().summary()).isEqualTo("요약 피드백");
        assertThat(storageModel.feedback().detailText()).isEqualTo("상세 피드백");
    }
}
