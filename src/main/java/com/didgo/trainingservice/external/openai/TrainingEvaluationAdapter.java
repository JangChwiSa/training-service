package com.didgo.trainingservice.external.openai;

import com.didgo.trainingservice.external.openai.dto.TrainingEvaluationRequest;
import com.didgo.trainingservice.external.openai.dto.TrainingEvaluationResult;

public interface TrainingEvaluationAdapter {

    TrainingEvaluationResult evaluate(TrainingEvaluationRequest request);
}
