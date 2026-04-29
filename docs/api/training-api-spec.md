# Training Service API Specification

> 이 문서는 전체 API 명세서에서 Training Service 관련 API만 발췌한 문서이다.
>
> 원본 문서:
>
> - `docs/api/api-spec.md`
>
> 원칙:
>
> - API 경로, 요청/응답 필드, 상태값, 오류 코드는 원문을 기준으로 한다.
> - 이 문서와 전체 API 명세가 충돌할 경우 전체 API 명세를 우선한다.
> - Training Service가 직접 소유하지 않는 API는 제외하되, Training Service 연동에 필요한 내부 API와 이벤트는 포함한다.

---

# 1. 설계 기준

본 API 명세서는 다음 3개 문서를 기준으로 재작성한다.

```text
1. 3.3 시스템 아키텍처 설계
2. 시퀀스다이어그램 작성
3. 데이터베이스 구조 설계
```

## 서비스 책임

```text
API Gateway
- 외부 요청 진입점
- 인증 토큰 검증
- 서비스 라우팅
- 내정보 화면처럼 여러 서비스 응답이 필요한 경우 간단한 응답 조합

Training Service
- 훈련 콘텐츠 조회
- 훈련 세션 생성
- 훈련 진행 로그 저장
- 훈련 점수/피드백 저장
- 사용자별 훈련 진행 요약 관리

Voice Service
- 사회성 훈련 음성 처리
- STT
- AI 응답 생성
- 필요 시 AI 피드백 생성 보조

Report Service
- TrainingCompleted 이벤트 기반 리포트 갱신
- 영역별 점수, 진행률, 직무 준비도, 종합 코멘트 조회
```

## DB 조회 기준

```text
training_db
- training_sessions
- social_scenarios
- social_dialog_logs
- user_social_progress
- safety_scenarios
- safety_scenes
- safety_choices
- safety_action_logs
- user_safety_progress
- focus_level_rules
- focus_commands
- focus_reaction_logs
- user_focus_progress
- document_questions
- document_answer_logs
- user_document_progress
- training_scores
- training_feedbacks

report_db
- report_summary
- report_snapshots
```

---

## 1.1 user_id 처리 원칙

```text
- 외부 API는 요청 바디나 쿼리 파라미터로 user_id를 직접 받지 않는다.
- 외부 클라이언트는 API Gateway에 Authorization: Bearer Access Token을 전달한다.
- API Gateway는 Access Token을 검증한 뒤 user_id를 추출한다.
- API Gateway는 Training Service로 요청을 전달할 때 trusted header인 X-User-Id에 인증된 user_id를 담아 전달한다.
- Training Service는 Authorization 토큰을 직접 검증하거나 디코딩하지 않고, Gateway가 전달한 X-User-Id를 현재 사용자 식별자로 사용한다.
- 내부 API는 서비스 간 호출 목적에 한해 경로에 userId를 포함할 수 있다.
- sessionId 기반 상세 조회 또는 완료 처리 시, 해당 sessionId가 현재 user_id의 세션인지 반드시 검증한다.
```

---

# 2. 공통 API 규칙

## 2.1 공통 Header

| Header | 필수 | 설명 |
| --- | --- | --- |
| Authorization | 외부 클라이언트 → API Gateway 인증 필요 API만 Y | Bearer Access Token |
| X-User-Id | API Gateway → Training Service Y | Gateway가 토큰 검증 후 전달하는 인증된 사용자 ID |
| Content-Type | Y | application/json |

Swagger에서 Training Service를 직접 호출하는 로컬 테스트는 API Gateway를 거치지 않으므로 `X-User-Id`를 직접 입력한다.

## 2.2 공통 응답 형식

Training Service의 컨트롤러는 성공/실패 응답을 모두 `ApiResponse<T>` 형식으로 반환한다.

각 API의 `Response` 예시는 실제 HTTP 응답의 `data` 필드에 들어가는 payload를 설명한다.
따라서 클라이언트가 실제 값을 읽을 때는 최상위 응답이 아니라 `data` 아래의 값을 사용한다.

예를 들어 API별 Response 예시가 다음과 같다면:

```json
{
  "sessionId": 10,
  "score": 85,
  "completed": true
}
```

실제 성공 응답 body는 다음 형식이다.

```json
{
  "success": true,
  "data": {
    "sessionId": 10,
    "score": 85,
    "completed": true
  },
  "error": null
}
```

## 2.3 공통 오류 응답

오류 응답은 `data`가 `null`이고, `error`에 코드와 메시지가 들어간다.

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "ERROR_CODE",
    "message": "오류 메시지"
  }
}
```

## 2.4 공통 오류 코드

| 코드 | 설명 |
| --- | --- |
| UNAUTHORIZED | 인증된 사용자 컨텍스트가 없거나 유효하지 않음 |
| FORBIDDEN | 접근 권한 없음 |
| VALIDATION_ERROR | 요청값 검증 실패 |
| NOT_FOUND | 요청한 리소스 없음 |
| CONFLICT | 중복 데이터 또는 상태 충돌 |
| INTERNAL_SERVER_ERROR | 서버 내부 오류 |

---

# 3. API Gateway

API Gateway는 클라이언트 요청을 각 서비스로 전달하는 외부 진입점이다.

## 3.1 라우팅 규칙

| 외부 경로 | 대상 서비스 | 설명 |
| --- | --- | --- |
| /api/my-page | User Service + Training Service | 내정보 통합 조회 |
| /api/trainings/ | Training Service | 훈련 현황, 훈련 진행, 훈련 결과 |
| /api/voice/ | Voice Service | 사회성 훈련 음성 대화 |
| /api/reports/ | Report Service | 리포트 조회 |

## 3.2 내정보 통합 조회

### GET /api/my-page

내정보 화면에 필요한 사용자 기본 정보와 훈련 요약 정보를 함께 조회한다.

### 처리 흐름

```text
1. API Gateway → User Service: 사용자 기본 정보 조회
   → User Service: GET /api/users/me
2. API Gateway → Training Service: 사용자 훈련 요약 조회
   → Training Service: GET /internal/trainings/users/{userId}/summary
3. API Gateway: 두 응답을 조합하여 클라이언트에 반환
```

### Training Service 조회 테이블

```text
사회성 최근 점수       → user_social_progress.recent_score
안전 정답 수/전체 수   → user_safety_progress.correct_count / total_count
문서이해 정답 수/전체 수 → user_document_progress.correct_count / total_count
집중력 현재 단계       → user_focus_progress.current_level
```

### Response

```json
{
  "user": {
    "userId": 1,
    "loginId": "user01",
    "name": "홍길동",
    "birthDate": "2000-01-01",
    "gender": "MALE",
    "email": "user@example.com",
    "disabilities": ["발달장애"],
    "desiredJob": "사무직"
  },
  "trainingSummary": {
    "socialRecentScore": 85,
    "safetyCorrectCount": 7,
    "safetyTotalCount": 10,
    "documentCorrectCount": 8,
    "documentTotalCount": 10,
    "focusCurrentLevel": 3
  }
}
```

---

# 4. Training Service API

Training Service는 training_db를 사용한다.

---

## 4.1 훈련 수준 조회

### GET /api/trainings/progress?type={trainingType}

Asia/Seoul 기준 이번 달 달력월의 완료 이력을 바탕으로 훈련 수준을 조회한다.
기존 경로와 `type` 파라미터를 유지하지만, 응답은 훈련 현황 요약이 아니라 공통 수준 응답이다.
기본 탭은 사회성 훈련이다.

### Query Parameter

| 이름 | 필수 | 설명 |
| --- | --- | --- |
| type | N | SOCIAL, SAFETY, DOCUMENT, FOCUS |

### DB 조회 기준

```text
training_session_summaries
training_sessions (DOCUMENT sub_type 조회용)

WHERE training_session_summaries.user_id = 현재 사용자 ID
AND training_session_summaries.training_type = type
AND training_session_summaries.completed_at >= 이번 달 1일 00:00:00
AND training_session_summaries.completed_at < 다음 달 1일 00:00:00
```

### 기간 기준

```text
- timezone: Asia/Seoul
- periodStart: 이번 달 1일 00:00:00
- periodEnd: 다음 달 1일 00:00:00 (exclusive)
```

### 공통 Response

```json
{
  "trainingType": "SOCIAL",
  "level": 4,
  "periodStart": "2026-04-01T00:00:00",
  "periodEnd": "2026-05-01T00:00:00",
  "timezone": "Asia/Seoul",
  "completedCount": 3,
  "minRequiredCount": 3,
  "basis": "MONTHLY_COMPLETED_SUMMARIES",
  "reason": null,
  "metrics": {
    "averageScore": 80.0,
    "monthlyCompletedCount": 3
  }
}
```

### 공통 속성 설명

| 속성 | 설명 |
| --- | --- |
| `trainingType` | 응답 훈련 유형. SOCIAL, SAFETY, DOCUMENT, FOCUS 중 하나이다. |
| `level` | 이번 달 완료 이력으로 산정한 수준. 산정 불가 시 `null`이다. |
| `periodStart` | 조회 월 시작 시각. Asia/Seoul 기준 ISO-8601 LocalDateTime 형식이다. |
| `periodEnd` | 조회 월 종료 시각. exclusive이며 Asia/Seoul 기준 ISO-8601 LocalDateTime 형식이다. |
| `timezone` | 월간 범위를 계산한 타임존. 항상 `Asia/Seoul`이다. |
| `completedCount` | 이번 달 완료한 해당 훈련 수. |
| `minRequiredCount` | 수준 산정에 필요한 최소 완료 수. |
| `basis` | 산정 기준. `MONTHLY_COMPLETED_SUMMARIES`이다. |
| `reason` | `level`이 `null`인 사유. 예: `NO_MONTHLY_COMPLETION`, `INSUFFICIENT_COMPLETIONS`. 산정 성공 시 `null`이다. |
| `metrics` | 훈련 유형별 산정 보조 지표이다. |

### 점수 기반 레벨 구간

| 점수 | level |
| --- | --- |
| 0-39 | 1 |
| 40-59 | 2 |
| 60-74 | 3 |
| 75-89 | 4 |
| 90-100 | 5 |

### 유형별 산정 규칙

```text
SOCIAL
- 이번 달 완료한 사회성 훈련이 3회 이상이면 평균 score로 level 산정
- 3회 미만이면 level = null
- metrics: averageScore, monthlyCompletedCount

DOCUMENT
- 이번 달 완료한 문서 이해 훈련 중 training_sessions.sub_type = LEVEL_n의 최고 난이도를 level로 반환
- 완료 기록이 없으면 level = null
- metrics: highestCompletedLevel, monthlyCompletedCount

FOCUS
- 이번 달 완료한 집중력 훈련 중 training_session_summaries.played_level의 최고값을 level로 반환
- 완료 기록이 없으면 level = null
- metrics: highestPlayedLevel, monthlyCompletedCount

SAFETY
- 이번 달 안전 훈련 완료 3회 이상이면 평균 score로 기본 레벨 산정
- 커버한 안전 카테고리 수로 상한 적용: 1개 max 2, 2개 max 4, 3개 max 5
- 최종 level = min(scoreLevel, categoryCap)
- 3회 미만이면 level = null
- metrics: averageScore, coveredCategoryCount, coveredCategories, monthlyCompletedCount
```

### 산정 불가 Response 예시

```json
{
  "trainingType": "SAFETY",
  "level": null,
  "periodStart": "2026-04-01T00:00:00",
  "periodEnd": "2026-05-01T00:00:00",
  "timezone": "Asia/Seoul",
  "completedCount": 2,
  "minRequiredCount": 3,
  "basis": "MONTHLY_COMPLETED_SUMMARIES",
  "reason": "INSUFFICIENT_COMPLETIONS",
  "metrics": {
    "averageScore": 95.0,
    "coveredCategoryCount": 2,
    "coveredCategories": ["COMMUTE_SAFETY", "INFECTIOUS_DISEASE"],
    "monthlyCompletedCount": 2
  }
}
```

---

## 4.2 훈련 기록 목록 조회

### GET /api/trainings/sessions?type={trainingType}&page={page}&size={size}

사용자가 선택한 훈련 유형의 과거 훈련 기록 목록을 조회한다. 훈련 현황 페이지에서 특정 훈련 탭을 선택했을 때, 해당 훈련의 완료 이력을 카드 또는 리스트 형태로 표시하기 위한 API이다.

### Query Parameter

| 이름 | 필수 | 설명 |
| --- | --- | --- |
| type | Y | SOCIAL, SAFETY, DOCUMENT, FOCUS |
| page | N | 페이지 번호, 기본값 0 |
| size | N | 페이지 크기, 기본값 10 |

### DB 조회 기준

```text
공통:
- training_session_summaries
- type=SAFETY 이고 category 값이 있으면 training_session_summaries.category로 추가 필터링
- 목록 응답은 training_session_summaries에 저장된 스냅샷 필드만 사용한다.

사회성:
- 대화 로그는 목록에서 조회하지 않는다.
- score, feedbackSummary는 training_session_summaries.score, feedback_summary를 사용한다.

안전:
- 선택 이력 전체는 목록에서 조회하지 않는다.
- correctCount, totalCount는 training_session_summaries.correct_count, total_count를 사용한다.

문서 이해:
- 문제별 답변 전체는 목록에서 조회하지 않는다.
- correctCount, totalCount는 training_session_summaries.correct_count, total_count를 사용한다.

집중력:
- 반응 로그 전체는 목록에서 조회하지 않는다.
- playedLevel, accuracyRate, wrongCount, averageReactionMs는 training_session_summaries의 스냅샷 필드를 사용한다.
```

### 정렬 기준

```text
training_session_summaries.completed_at DESC
```

### Response - SOCIAL

```json
{
  "trainingType": "SOCIAL",
  "page": 0,
  "size": 10,
  "totalElements": 3,
  "sessions": [
    {
      "sessionId": 10,
      "scenarioId": 1,
      "scenarioTitle": "동료에게 도움 요청하기",
      "score": 85,
      "feedbackSummary": "상황에 맞게 정중하게 대화했습니다.",
      "completedAt": "2026-04-27T10:00:00"
    }
  ]
}
```

### Response - SAFETY

```json
{
  "trainingType": "SAFETY",
  "page": 0,
  "size": 10,
  "totalElements": 2,
  "sessions": [
    {
      "sessionId": 20,
      "scenarioId": 1,
      "category": "COMMUTE_SAFETY",
      "scenarioTitle": "작업장 위험 상황 대처",
      "score": 70,
      "correctCount": 7,
      "totalCount": 10,
      "completedAt": "2026-04-27T10:20:00"
    }
  ]
}
```

### Response - DOCUMENT

```json
{
  "trainingType": "DOCUMENT",
  "page": 0,
  "size": 10,
  "totalElements": 4,
  "sessions": [
    {
      "sessionId": 30,
      "score": 80,
      "correctCount": 8,
      "totalCount": 10,
      "completedAt": "2026-04-27T10:40:00"
    }
  ]
}
```

### Response - FOCUS

```json
{
  "trainingType": "FOCUS",
  "page": 0,
  "size": 10,
  "totalElements": 5,
  "sessions": [
    {
      "sessionId": 40,
      "playedLevel": 2,
      "score": 92,
      "accuracyRate": 92.5,
      "wrongCount": 3,
      "averageReactionMs": 820,
      "completedAt": "2026-04-27T11:00:00"
    }
  ]
}
```

### 비고

```text
- 이 API는 훈련 목록/기록 카드 표시용이다.
- 사회성 대화 로그, 안전 선택 이력, 문서 문제별 답변, 집중력 반응 로그는 목록 API에서 내려주지 않는다.
- 상세 데이터는 각 훈련 상세 조회 API에서 sessionId 기반으로 조회한다.
- 단, 집중력 훈련은 별도 상세 조회 API를 제공하지 않는다.
- 훈련 기록 목록 API는 training_session_summaries만 조회한다.
- 안전 훈련은 training_session_summaries.category 조건으로 카테고리별 기록 조회가 가능하다.
- 훈련 완료 시 목록 화면에 필요한 scenario_id, scenario_title, category, feedback_summary를 training_session_summaries에 함께 저장한다.
- 훈련 완료 시 Training Service가 원본 로그/점수/피드백 저장 후 training_session_summaries를 생성한다.
- 상세 데이터는 각 훈련 상세 조회 API에서 원본 로그 테이블을 조회한다.
- 따라서 목록 화면과 상세 화면의 조회 책임을 분리한다.
```

---

## 4.3 훈련 상세 조회 - 사회성

### GET /api/trainings/social/sessions/{sessionId}/detail

사회성 훈련 상세보기에서 점수, 피드백, 대화 로그를 조회한다.

### DB 조회 기준

```text
training_sessions
training_scores
training_feedbacks
social_dialog_logs
```

### Response

```json
{
  "sessionId": 10,
  "score": 85,
  "scoreType": "AI_EVALUATION",
  "feedback": {
    "summary": "상황에 맞게 정중하게 대화했습니다.",
    "detailText": "도움 요청 표현이 자연스럽고 상대방의 반응에 적절히 답했습니다."
  },
  "dialogLogs": [
    { "turnNo": 1, "speaker": "USER", "content": "도와주실 수 있나요?" },
    { "turnNo": 1, "speaker": "AI", "content": "네, 어떤 부분이 어려우신가요?" }
  ]
}
```

## 4.4 훈련 상세 조회 - 안전

### GET /api/trainings/safety/sessions/{sessionId}/detail

안전 훈련 상세보기에서 점수, 선택 이력, 피드백을 조회한다.

### DB 조회 기준

```text
training_sessions
training_scores
training_feedbacks
safety_action_logs
safety_scenes
safety_choices
```

### Response

```json
{
  "sessionId": 20,
  "score": 70,
  "choiceSummary": {
    "correctCount": 7,
    "totalCount": 10
  },
  "actions": [
    {
      "sceneId": 1,
      "situationText": "작업장 바닥에 물이 흘러 있습니다.",
      "selectedChoice": "관리자에게 알린다",
      "correct": true
    }
  ],
  "feedback": {
    "summary": "대부분의 위험 상황을 올바르게 판단했습니다.",
    "detailText": "미끄러운 바닥을 발견했을 때 관리자에게 알린 선택은 적절합니다."
  }
}
```

## 4.5 훈련 상세 조회 - 문서 이해

### GET /api/trainings/document/sessions/{sessionId}/detail

문서 이해 훈련 상세보기에서 점수, 문제별 결과, 해설을 조회한다.

### DB 조회 기준

```text
training_sessions
training_scores
training_feedbacks
document_answer_logs
document_questions
```

### Response

```json
{
  "sessionId": 30,
  "score": 80,
  "answerSummary": {
    "correctCount": 8,
    "totalCount": 10
  },
  "answers": [
    {
      "questionId": 1,
      "questionText": "변경된 근무 시작 시간은 언제인가요?",
      "userAnswer": "오전 9시",
      "correctAnswer": "오전 9시",
      "correct": true,
      "explanation": "문서에 오전 9시로 변경된다고 명시되어 있습니다."
    }
  ]
}
```

---

# 5. 사회성 훈련 API

## 5.1 사회성 직무 유형 선택

### POST /api/trainings/social/job-type

사회성 훈련에서 사무직 또는 단순 노무를 선택한다.

### Request

```json
{
  "jobType": "OFFICE"
}
```

### Response

```json
{
  "jobType": "OFFICE",
  "nextPage": "SCENARIO_SELECTION"
}
```

### 처리 기준

```text
- 이 API는 사용자의 직무 유형 선택값을 DB에 저장하지 않는다.
- 실제 훈련 세션 저장은 POST /api/trainings/social/sessions 호출 시 training_sessions.sub_type = jobType으로 처리한다.
- 직무 유형 선택 후 시나리오 목록은 GET /api/trainings/social/scenarios?jobType={jobType}으로 조회한다.
```

## 5.2 사회성 시나리오 목록 조회

### GET /api/trainings/social/scenarios?jobType={jobType}

선택한 직무 유형에 맞는 사회성 훈련 시나리오 목록을 조회한다.

### DB 조회 기준

```text
social_scenarios
WHERE job_type = jobType
AND is_active = true
```

### Response

```json
[
  {
    "scenarioId": 1,
    "title": "동료에게 도움 요청하기",
    "difficulty": 1
  }
]
```

## 5.3 사회성 시나리오 상세 조회

### GET /api/trainings/social/scenarios/{scenarioId}

선택한 시나리오의 상황, 배경, 캐릭터 정보를 조회한다.

### DB 조회 기준

```text
social_scenarios
```

### Response

```json
{
  "scenarioId": 1,
  "jobType": "OFFICE",
  "title": "동료에게 도움 요청하기",
  "backgroundText": "사무실에서 업무 중 모르는 일이 생겼습니다.",
  "situationText": "동료에게 정중하게 도움을 요청해야 합니다.",
  "characterInfo": "동료 직원",
  "difficulty": 1
}
```

## 5.4 사회성 훈련 세션 시작

### POST /api/trainings/social/sessions

선택한 사회성 시나리오로 훈련 세션을 생성한다.

### Request

```json
{
  "jobType": "OFFICE",
  "scenarioId": 1
}
```

### DB 처리

```text
Gateway가 전달한 X-User-Id를 현재 user_id로 사용
training_sessions 생성
user_id = 현재 사용자 ID
training_type = SOCIAL
sub_type = jobType
scenario_id = scenarioId
status = IN_PROGRESS
```

### Response

```json
{
  "sessionId": 10,
  "scenarioId": 1,
  "status": "IN_PROGRESS"
}
```

## 5.5 사회성 훈련 종료

### POST /api/trainings/social/sessions/{sessionId}/complete

사회성 훈련 종료 후 전체 대화 로그, AI 평가 점수, 피드백을 저장한다.

### Request

```json
{
  "dialogLogs": [
    { "turnNo": 1, "speaker": "USER", "content": "도와주실 수 있나요?" },
    { "turnNo": 1, "speaker": "AI", "content": "네, 어떤 부분이 어려우신가요?" }
  ]
}
```

### DB 처리

```text
session_id가 현재 user_id의 세션인지 검증
social_dialog_logs 저장
training_scores 저장
  score_type = AI_EVALUATION
training_feedbacks 저장
user_social_progress 갱신
training_session_summaries 생성
training_sessions.status = COMPLETED
TrainingCompleted 이벤트 발행
```

### Response

```json
{
  "sessionId": 10,
  "score": 85,
  "feedbackSummary": "상황에 맞게 정중하게 대화했습니다.",
  "completed": true
}
```

---

# 6. 안전 훈련 API

## 6.1 안전 시나리오 목록 조회

### GET /api/trainings/safety/scenarios?category={category}

안전 훈련 시나리오 목록을 조회한다. 카테고리별 필터 조회를 지원한다.

### DB 조회 기준

```text
safety_scenarios
WHERE is_active = true
AND (category = 요청값 OR 요청값 없으면 전체)
```

### Response

```json
[
  {
    "scenarioId": 1,
    "category": "COMMUTE_SAFETY",
    "title": "출근길 횡단보도 안전 수칙",
    "description": "출퇴근 중 발생할 수 있는 위험 상황을 판단합니다."
  },
  {
    "scenarioId": 2,
    "category": "INFECTIOUS_DISEASE",
    "title": "감염병 예방 행동 요령",
    "description": "감염병 확산 상황에서 올바른 행동을 판단합니다."
  }
]
```

## 6.2 안전 훈련 세션 시작 및 첫 장면 조회

### POST /api/trainings/safety/sessions

선택한 안전 시나리오로 training_sessions를 생성하고 첫 장면을 반환한다.

### Request

```json
{
  "scenarioId": 1
}
```

### 비고

```text
선택한 scenarioId의 category에 따라 성 관련 교육 / 감염병 교육 / 출퇴근 안전 교육 흐름이 시작된다.
```

### DB 처리

```text
Gateway가 전달한 X-User-Id를 현재 user_id로 사용
training_sessions 생성
user_id = 현재 사용자 ID
training_type = SAFETY
scenario_id = scenarioId
status = IN_PROGRESS
safety_scenes에서 scene_order = 1 조회
safety_choices 조회
```

### Response

```json
{
  "sessionId": 20,
  "scene": {
    "sceneId": 1,
    "screenInfo": "공장 바닥에 물이 흘러 있는 화면",
    "situationText": "작업장 바닥에 물이 흘러 있습니다.",
    "questionText": "가장 먼저 해야 할 행동은 무엇인가요?",
    "choices": [
      { "choiceId": 1, "text": "그냥 지나간다" },
      { "choiceId": 2, "text": "관리자에게 알린다" }
    ],
    "endScene": false
  }
}
```

## 6.3 안전 훈련 다음 장면 조회

### POST /api/trainings/safety/sessions/{sessionId}/next-scene

사용자의 선택을 저장하고 다음 장면을 반환한다.

### Request

```json
{
  "sceneId": 1,
  "choiceId": 2
}
```

### DB 처리

```text
session_id가 현재 user_id의 세션인지 검증
safety_choices에서 choice_id 조회
safety_action_logs 저장
next_scene_id로 safety_scenes 조회
next_scene_id의 safety_choices 조회
training_sessions.current_step 갱신
```

### Response

```json
{
  "selectedResult": {
    "correct": true
  },
  "nextScene": {
    "sceneId": 2,
    "screenInfo": "관리자가 안전 표지판을 설치하는 화면",
    "situationText": "관리자가 위험 구역을 표시했습니다.",
    "questionText": "다음 행동은 무엇인가요?",
    "choices": [
      { "choiceId": 3, "text": "주의해서 이동한다" }
    ],
    "endScene": false
  }
}
```

## 6.4 안전 훈련 완료

### POST /api/trainings/safety/sessions/{sessionId}/complete

안전 훈련을 완료 처리하고 점수와 진행 요약을 갱신한다.

### DB 처리

```text
session_id가 현재 user_id의 세션인지 검증
safety_action_logs 집계
training_scores 저장
  score_type = CHOICE_RESULT
training_feedbacks 저장
user_safety_progress 갱신
training_session_summaries 생성
training_sessions.status = COMPLETED
TrainingCompleted 이벤트 발행
```

### Response

```json
{
  "sessionId": 20,
  "score": 70,
  "correctCount": 7,
  "totalCount": 10,
  "completed": true
}
```

---

# 7. 집중력 훈련 API

## 7.1 집중력 진행 상태 조회

### GET /api/trainings/focus/progress

사용자의 현재 집중력 훈련 단계와 해금 단계를 조회한다.

### DB 조회 기준

```text
user_focus_progress
```

### Response

```json
{
  "currentLevel": 3,
  "highestUnlockedLevel": 3,
  "lastPlayedLevel": 2,
  "lastAccuracyRate": 92.5,
  "lastAverageReactionMs": 820
}
```

## 7.2 집중력 훈련 시작

### POST /api/trainings/focus/sessions

사용자가 선택한 단계로 청기백기 훈련 세션을 생성하고 3분치 지시 목록을 반환한다.

### Request

```json
{
  "level": 2
}
```

### DB 처리

```text
Gateway가 전달한 X-User-Id를 현재 user_id로 사용
user_focus_progress.highest_unlocked_level로 선택 가능 여부 검증
training_sessions 생성
user_id = 현재 사용자 ID
training_type = FOCUS
sub_type = level
focus_commands 생성 및 저장
```

### Response

```json
{
  "sessionId": 40,
  "level": 2,
  "durationSeconds": 180,
  "commands": [
    {
      "commandId": 1001,
      "order": 1,
      "commandText": "청기 들어",
      "expectedAction": "BLUE_UP",
      "displayAtMs": 0
    }
  ]
}
```

## 7.3 집중력 훈련 종료 및 반응 로그 제출

### POST /api/trainings/focus/sessions/{sessionId}/complete

프론트가 3분 동안 기록한 전체 반응 로그를 제출하고 서버가 일괄 채점한다.

### Request

```json
{
  "reactions": [
    {
      "commandId": 1001,
      "userInput": "BLUE_UP",
      "reactionMs": 720
    }
  ]
}
```

### DB 처리

```text
session_id가 현재 user_id의 세션인지 검증
focus_commands 조회
focus_reaction_logs 저장
정확도/평균 반응속도 계산
training_scores 저장
  score_type = REACTION_PERFORMANCE
user_focus_progress 갱신
training_session_summaries 생성
training_sessions.status = COMPLETED
TrainingCompleted 이벤트 발행
```

### Response

```json
{
  "sessionId": 40,
  "score": 92,
  "accuracyRate": 92.5,
  "wrongCount": 3,
  "averageReactionMs": 820,
  "unlockedNextLevel": true,
  "currentLevel": 3,
  "highestUnlockedLevel": 3
}
```

---

# 8. 문서 이해 훈련 API

## 8.1 문서 이해 훈련 시작

### POST /api/trainings/document/sessions

문서 이해 훈련 세션을 생성하고 요청한 레벨의 활성 문서 이해 문제 중 랜덤 배정된 5문제를 반환한다.

### Request

```json
{
  "level": 1
}
```

### Request Body

| 이름 | 필수 | 설명 |
| --- | --- | --- |
| level | Y | 문서 이해 훈련 레벨. 1~5만 허용 |

### DB 처리

```text
Gateway가 전달한 X-User-Id를 현재 user_id로 사용
level을 LEVEL_1~LEVEL_5 difficulty 값으로 변환
document_questions에서 difficulty와 is_active 기준으로 랜덤 5문제 조회
5문제 미만이면 CONFLICT 반환
training_sessions 생성
user_id = 현재 사용자 ID
training_type = DOCUMENT
sub_type = LEVEL_n
status = IN_PROGRESS
document_session_questions에 배정 문제 5개와 display_order 저장
```

### Response

```json
{
  "sessionId": 50,
  "questions": [
    {
      "questionId": 1,
      "title": "근무 시간 변경 안내",
      "documentText": "오늘부터 근무 시간이 오전 9시로 변경됩니다.",
      "questionText": "변경된 근무 시작 시간은 언제인가요?",
      "questionType": "SHORT_ANSWER"
    }
  ]
}
```

## 8.2 문서 이해 답변 제출 및 완료

### POST /api/trainings/document/sessions/{sessionId}/answers

문서 이해 훈련 답변을 제출하고 세션에 배정된 5문제와 정확히 일치하는지 검증한 뒤 채점 및 완료 처리를 수행한다.

### Request

```json
{
  "answers": [
    {
      "questionId": 1,
      "userAnswer": "오전 9시"
    }
  ]
}
```

### DB 처리

```text
session_id가 현재 user_id의 세션인지 검증
document_session_questions에서 배정 문제 조회
제출 questionId 목록이 배정된 5문제와 정확히 일치하는지 검증
배정된 문제 기준으로 document_questions 정답 조회
document_answer_logs 저장
training_scores 저장
  score_type = ACCURACY_RATE
training_feedbacks 저장
user_document_progress 갱신
training_session_summaries 생성
training_sessions.status = COMPLETED
TrainingCompleted 이벤트 발행
```

### Error Cases

| 조건 | 코드 |
| --- | --- |
| level이 1~5 범위를 벗어남 | VALIDATION_ERROR |
| 요청 레벨의 활성 문제가 5개 미만 | CONFLICT |
| 제출 questionId가 배정된 5문제와 정확히 일치하지 않음 | VALIDATION_ERROR |

### Response

```json
{
  "sessionId": 50,
  "score": 100,
  "correctCount": 1,
  "totalCount": 1,
  "results": [
    {
      "questionId": 1,
      "correct": true,
      "correctAnswer": "오전 9시",
      "explanation": "문서에 오전 9시로 변경된다고 명시되어 있습니다."
    }
  ],
  "completed": true
}
```

# 9. 내부 API

외부 클라이언트에는 공개하지 않는 서비스 간 API이다.

## 9.1 사용자 훈련 요약 조회

### GET /internal/trainings/users/{userId}/summary

API Gateway가 내정보 통합 조회 시 호출한다.

### DB 조회 기준

```text
user_social_progress
user_safety_progress
user_document_progress
user_focus_progress
```

### Response

```json
{
  "socialRecentScore": 85,
  "safetyCorrectCount": 7,
  "safetyTotalCount": 10,
  "documentCorrectCount": 8,
  "documentTotalCount": 10,
  "focusCurrentLevel": 3
}
```

## 9.2 최신 훈련 결과 재조회

### GET /internal/trainings/users/{userId}/latest-results

Report Service가 리포트 데이터가 없거나 최신 훈련 결과와 불일치할 때 호출한다.

### DB 조회 기준

```text
training_sessions
training_scores
training_feedbacks
```

### Response

```json
{
  "userId": 1,
  "results": [
    {
      "sessionId": 10,
      "trainingType": "SOCIAL",
      "score": 85,
      "scoreType": "AI_EVALUATION",
      "completedAt": "2026-04-27T10:30:00"
    }
  ]
}
```

---

# 10. 이벤트 명세

Event Broker를 통해 서비스 간 비동기 처리를 수행한다.

## 10.1 TrainingCompleted

훈련 완료 후 Training Service가 발행한다.

### Producer

```text
Training Service
```

### Consumer

```text
Report Service
```

### Payload

```json
{
  "eventId": "evt-001",
  "eventType": "TrainingCompleted",
  "userId": 1,
  "sessionId": 10,
  "trainingType": "SOCIAL",
  "score": 85,
  "scoreType": "AI_EVALUATION",
  "completedAt": "2026-04-27T10:30:00"
}
```

### 처리 내용

```text
Report Service는 이벤트 수신 후 report_summary를 갱신한다.
필요하면 report_snapshots에 현재 리포트 상태를 저장한다.
```

## 10.2 이벤트 처리 공통 규칙

```text
- eventId 기준으로 멱등 처리한다.
- 이벤트 발행은 Outbox Pattern을 사용한다.
- 소비 실패 시 Retry 후 DLQ로 이동한다.
- 리포트 조회 시 report_db가 비어 있거나 최신 결과와 불일치하면 Training Service 최신 결과 재조회 API로 복구한다.
```
