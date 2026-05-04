package com.didgo.trainingservice.external.openai;

import com.didgo.trainingservice.external.openai.dto.TrainingEvaluationFeedbackModel;
import com.didgo.trainingservice.external.openai.dto.TrainingEvaluationResult;
import com.didgo.trainingservice.external.openai.dto.TrainingEvaluationScoreModel;
import com.didgo.trainingservice.external.openai.dto.TrainingEvaluationStorageModel;
import org.springframework.stereotype.Component;

@Component
public class TrainingEvaluationResultMapper {

    private static final String FEEDBACK_TYPE_SUMMARY = "SUMMARY";
    private static final String FEEDBACK_SOURCE_AI = "AI";

    public TrainingEvaluationStorageModel toStorageModel(TrainingEvaluationResult result) {
        return new TrainingEvaluationStorageModel(
                new TrainingEvaluationScoreModel(
                        result.score(),
                        result.scoreType(),
                        result.rawMetricsJson()
                ),
                new TrainingEvaluationFeedbackModel(
                        FEEDBACK_TYPE_SUMMARY,
                        FEEDBACK_SOURCE_AI,
                        result.feedback().summary(),
                        result.feedback().detailText()
                )
        );
    }
}
