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
                "동료에게 지원 요청하기",
                List.of(
                        new TrainingEvaluationLog("USER", "제가 도와드릴 부분이 있을까요?"),
                        new TrainingEvaluationLog("AI", "어떤 부분이 어려운지 물어봐줘서 좋아요.")
                ),
                Map.of("turnCount", 2)
        ));

        assertThat(result.score()).isEqualTo(70);
        assertThat(result.scoreType()).isEqualTo("AI_EVALUATION");
        assertThat(result.feedback().summary()).contains("상황");
        assertThat(result.rawMetricsJson()).contains("local-fake");
    }

    @Test
    void socialRefusalWithoutConstructiveResponseGetsLowKoreanFeedback() {
        OpenAiProperties properties = new OpenAiProperties(null, 30000, "local-fake", null, null);
        TrainingEvaluationAdapter adapter = new LocalFakeOpenAiTrainingEvaluationAdapter(properties);

        TrainingEvaluationResult result = adapter.evaluate(new TrainingEvaluationRequest(
                TrainingType.SOCIAL,
                "복사 지시 확인",
                List.of(
                        new TrainingEvaluationLog("AI", "이거 넉넉히 복사해서 회의실에 갖다 둬요."),
                        new TrainingEvaluationLog("USER", "제가 하기 싫어요."),
                        new TrainingEvaluationLog("AI", "지금 꼭 해야 하는 거라서 당장 복사해서 갖다 놔야 해요. 몇 장이 필요한지 파악하는 게 어렵나요?"),
                        new TrainingEvaluationLog("USER", "하기 싫다니까요.")
                ),
                Map.of("dialogLogCount", 4)
        ));

        assertThat(result.score()).isEqualTo(35);
        assertThat(result.feedback().summary()).contains("부족");
        assertThat(result.feedback().detailText()).contains("사수의 업무 요청");
    }
}
