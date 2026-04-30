package com.jangchwisa.trainingservice.external.openai;

import com.jangchwisa.trainingservice.external.openai.dto.TrainingEvaluationRequest;
import com.jangchwisa.trainingservice.external.openai.dto.TrainingEvaluationResult;

public interface TrainingEvaluationAdapter {

    TrainingEvaluationResult evaluate(TrainingEvaluationRequest request);
}
