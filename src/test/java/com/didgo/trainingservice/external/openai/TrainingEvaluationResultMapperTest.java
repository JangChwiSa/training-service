package com.didgo.trainingservice.external.openai;

import static org.assertj.core.api.Assertions.assertThat;

import com.didgo.trainingservice.external.openai.dto.TrainingEvaluationFeedback;
import com.didgo.trainingservice.external.openai.dto.TrainingEvaluationResult;
import com.didgo.trainingservice.external.openai.dto.TrainingEvaluationStorageModel;
import org.junit.jupiter.api.Test;

class TrainingEvaluationResultMapperTest {

    TrainingEvaluationResultMapper mapper = new TrainingEvaluationResultMapper();

    @Test
    void convertsEvaluationResultToScoreAndFeedbackStorageModel() {
        TrainingEvaluationResult result = new TrainingEvaluationResult(
                85,
                "AI_EVALUATION",
                new TrainingEvaluationFeedback("Summary feedback", "Detailed feedback"),
                "{\"rubric\":\"social\"}"
        );

        TrainingEvaluationStorageModel storageModel = mapper.toStorageModel(result);

        assertThat(storageModel.score().score()).isEqualTo(85);
        assertThat(storageModel.score().scoreType()).isEqualTo("AI_EVALUATION");
        assertThat(storageModel.score().rawMetricsJson()).isEqualTo("{\"rubric\":\"social\"}");
        assertThat(storageModel.feedback().feedbackType()).isEqualTo("SUMMARY");
        assertThat(storageModel.feedback().feedbackSource()).isEqualTo("AI");
        assertThat(storageModel.feedback().summary()).isEqualTo("Summary feedback");
        assertThat(storageModel.feedback().detailText()).isEqualTo("Detailed feedback");
    }
}
