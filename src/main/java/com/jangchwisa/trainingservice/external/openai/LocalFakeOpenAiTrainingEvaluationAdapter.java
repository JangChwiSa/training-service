package com.jangchwisa.trainingservice.external.openai;

import com.jangchwisa.trainingservice.external.openai.dto.TrainingEvaluationFeedback;
import com.jangchwisa.trainingservice.external.openai.dto.TrainingEvaluationRequest;
import com.jangchwisa.trainingservice.external.openai.dto.TrainingEvaluationResult;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "training.openai", name = "adapter", havingValue = "local-fake", matchIfMissing = true)
public class LocalFakeOpenAiTrainingEvaluationAdapter implements TrainingEvaluationAdapter {

    private final OpenAiProperties properties;

    public LocalFakeOpenAiTrainingEvaluationAdapter(OpenAiProperties properties) {
        this.properties = properties;
    }

    @Override
    public TrainingEvaluationResult evaluate(TrainingEvaluationRequest request) {
        int score = Math.min(100, 60 + request.logs().size() * 5 + request.metrics().size() * 3);
        String summary = "Local fake evaluation generated for " + request.trainingType().name() + " training.";
        String detail = "This is a deterministic local adapter response. Timeout is "
                + properties.timeoutMs()
                + " ms.";

        return new TrainingEvaluationResult(
                score,
                "AI_EVALUATION",
                new TrainingEvaluationFeedback(summary, detail),
                "{\"adapter\":\"local-fake\",\"fallback\":false}"
        );
    }
}
