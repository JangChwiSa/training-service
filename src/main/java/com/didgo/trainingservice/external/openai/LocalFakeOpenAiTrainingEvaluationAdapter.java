package com.didgo.trainingservice.external.openai;

import com.didgo.trainingservice.external.openai.dto.TrainingEvaluationFeedback;
import com.didgo.trainingservice.external.openai.dto.TrainingEvaluationLog;
import com.didgo.trainingservice.external.openai.dto.TrainingEvaluationRequest;
import com.didgo.trainingservice.external.openai.dto.TrainingEvaluationResult;
import com.didgo.trainingservice.training.session.entity.TrainingType;
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
        int score = score(request);
        String summary = summary(request, score);
        String detail = detail(request, score);

        return new TrainingEvaluationResult(
                score,
                "AI_EVALUATION",
                new TrainingEvaluationFeedback(summary, detail),
                "{\"adapter\":\"local-fake\",\"fallback\":false}"
        );
    }

    private int score(TrainingEvaluationRequest request) {
        if (request.trainingType() == TrainingType.SOCIAL) {
            return socialScore(request);
        }
        return Math.min(100, 60 + request.logs().size() * 5 + request.metrics().size() * 3);
    }

    private int socialScore(TrainingEvaluationRequest request) {
        boolean hasUserRefusal = request.logs().stream()
                .filter(log -> "USER".equalsIgnoreCase(log.role()))
                .map(TrainingEvaluationLog::content)
                .anyMatch(this::isRefusal);
        boolean hasConstructiveUserResponse = request.logs().stream()
                .filter(log -> "USER".equalsIgnoreCase(log.role()))
                .map(TrainingEvaluationLog::content)
                .anyMatch(this::isConstructiveSocialResponse);

        if (hasUserRefusal && !hasConstructiveUserResponse) {
            return 35;
        }
        if (hasUserRefusal) {
            return 55;
        }
        return Math.min(100, 62 + request.logs().size() * 4);
    }

    private boolean isRefusal(String content) {
        if (content == null) {
            return false;
        }
        String normalized = content.replace(" ", "");
        return normalized.contains("하기싫")
                || normalized.contains("안할")
                || normalized.contains("못하겠")
                || normalized.contains("싫어요")
                || normalized.contains("싫다");
    }

    private boolean isConstructiveSocialResponse(String content) {
        if (content == null) {
            return false;
        }
        String normalized = content.replace(" ", "");
        return normalized.contains("몇장")
                || normalized.contains("확인")
                || normalized.contains("알려")
                || normalized.contains("어려운")
                || normalized.contains("도와");
    }

    private String summary(TrainingEvaluationRequest request, int score) {
        if (request.trainingType() == TrainingType.SOCIAL) {
            return score < 60
                    ? "업무 상황에 맞는 사회적 대응이 부족했습니다."
                    : "상황을 이해하고 대화를 이어가려는 시도가 있었습니다.";
        }
        return request.trainingType().name() + " 훈련 평가가 완료되었습니다.";
    }

    private String detail(TrainingEvaluationRequest request, int score) {
        if (request.trainingType() == TrainingType.SOCIAL && score < 60) {
            return "사수의 업무 요청에 대해 단순히 거절하는 답변이 반복되었습니다. 사회성 훈련에서는 하기 싫다는 표현보다, 필요한 정보가 무엇인지 묻거나 어려운 점을 차분히 설명하는 응답이 필요합니다.";
        }
        return "훈련 로그와 응답 흐름을 기준으로 평가했습니다. 제한 시간은 "
                + properties.timeoutMs()
                + "ms입니다.";
    }
}
