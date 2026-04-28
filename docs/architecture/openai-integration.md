# OpenAI Integration

## 1. Purpose

이 문서는 Training Service가 OpenAI API를 사용하는 범위와 경계를 정의한다.

Training Service는 훈련 평가, 점수 생성, 피드백 생성, 완료된 훈련 로그 분석을 위해 OpenAI API를 사용할 수 있다.

## 2. Responsibility Boundary

Training Service가 OpenAI API를 사용하는 범위는 다음과 같다.

```text
- Training evaluation
- Score generation
- Feedback generation
- Analysis of completed training logs
- Adaptive training support when required
```

Training Service는 다음 기능을 OpenAI 연동 범위에 포함하지 않는다.

```text
- STT
- TTS
- Real-time voice dialogue
- Voice-based AI response generation
```

## 3. Input Data

OpenAI 평가 입력은 완료된 훈련 결과를 기준으로 한다.

훈련 유형별 입력 개념은 다음과 같다.

```text
Social training
- Completed dialogue logs
- Scenario information
- User utterances

Safety training
- Selected choices
- Correct/incorrect results
- Scenario and scene information

Focus training
- Focus reaction results
- Accuracy rate
- Average reaction time
- Wrong count

Document training
- Document answers
- Correct answers
- Explanation and scoring result
```

개인정보와 불필요한 사용자 프로필 정보는 OpenAI 요청에 포함하지 않는다.

## 4. Output Data

OpenAI 응답은 Training Service의 결과 저장 형식으로 변환한다.

저장 대상은 다음과 같다.

```text
training_scores
- score
- score_type
- raw_metrics_json

training_feedbacks
- feedback_type
- feedback_source = AI
- summary
- detail_text

training_session_summaries
- score
- feedback_summary
- summary_text
```

OpenAI 원문 응답을 그대로 외부 API에 노출하지 않는다.

Training Service가 필요한 결과 필드로 정제한 뒤 저장하고 응답한다.

## 5. Failure Policy

OpenAI 호출 실패는 훈련 완료 흐름에 영향을 줄 수 있으므로 명시적으로 처리한다.

기본 정책은 다음과 같다.

```text
Timeout
- 요청 제한 시간을 둔다.
- 기본 local 문서 기준값은 OPENAI_TIMEOUT_MS=30000이다.

Retry
- 일시적 네트워크 오류 또는 5xx 오류는 제한된 횟수만 retry한다.
- validation 오류나 quota 오류는 무한 retry하지 않는다.

Fallback
- AI 피드백 생성 실패 시 시스템 피드백 또는 재시도 가능 상태를 사용할 수 있다.
- fallback을 사용한 경우 feedback_source 또는 raw_metrics_json에 근거를 남긴다.
```

## 6. Configuration

OpenAI 연동 설정은 환경 변수로 주입한다.

```text
OPENAI_API_KEY=
OPENAI_TIMEOUT_MS=30000
```

API key는 저장소에 커밋하지 않는다.

운영 환경의 model, timeout, retry 정책은 설정값으로 관리한다.
