# Security Context

## 1. Purpose

이 문서는 Training Service에서 사용자 식별 정보를 다루는 기준을 정의한다.

Training Service는 인증 자체를 수행하지 않는다. 인증은 API Gateway 또는 User Service 책임이며, Training Service는 인증된 사용자 식별자만 신뢰한다.

## 2. User Identity Rule

외부 API는 `userId`를 request body 또는 query parameter로 받지 않는다.

```text
금지:
- POST /api/trainings/social/sessions { "userId": 1, ... }
- GET /api/trainings/sessions?userId=1

허용:
- API Gateway가 인증 후 trusted header 또는 인증 컨텍스트로 userId 전달
```

로컬 개발과 gateway 연동의 기본 trusted header 이름은 다음과 같다.

```http
X-User-Id: 1
```

운영 환경에서는 API Gateway가 토큰 검증 후 이 값을 전달한다.

## 3. External API Rule

`/api/trainings/**` API는 항상 현재 사용자 기준으로 동작한다.

```text
- 훈련 세션 생성: 현재 userId로 training_sessions.user_id 저장
- 훈련 현황 조회: 현재 userId의 progress만 조회
- 홈 화면 훈련 수준 요약 조회: 현재 userId의 summaries만 조회
- 문서 이해 해금 상태 조회: 현재 userId의 user_document_progress만 조회
- 훈련 기록 조회: 현재 userId의 summaries만 조회
- 훈련 상세 조회: sessionId가 현재 userId의 세션인지 검증
- 훈련 완료 처리: sessionId가 현재 userId의 세션인지 검증
```

클라이언트가 전달한 사용자 식별값은 신뢰하지 않는다.

## 4. Internal API Rule

`/internal/trainings/**` API는 서비스 간 호출 전용이다.

내부 API는 서비스 간 조회 목적에 한해 path variable로 `userId`를 포함할 수 있다.

```text
예:
GET /internal/trainings/users/{userId}/summary
GET /internal/trainings/users/{userId}/latest-results
```

내부 API는 외부 클라이언트에 공개하지 않는다.

내부 API 호출자는 API Gateway, Report Service 등 신뢰된 내부 구성요소로 제한한다.

## 5. Session Ownership Validation

`sessionId` 기반 API는 반드시 세션 소유권을 검증한다.

검증 기준은 다음과 같다.

```text
training_sessions.session_id = 요청 sessionId
training_sessions.user_id = 현재 userId
```

소유권 검증이 필요한 대표 API는 다음과 같다.

```text
GET /api/trainings/social/sessions/{sessionId}/detail
GET /api/trainings/safety/sessions/{sessionId}/detail
GET /api/trainings/document/sessions/{sessionId}/detail
POST /api/trainings/social/sessions/{sessionId}/complete
POST /api/trainings/safety/sessions/{sessionId}/next-scene
POST /api/trainings/safety/sessions/{sessionId}/complete
POST /api/trainings/focus/sessions/{sessionId}/complete
POST /api/trainings/document/sessions/{sessionId}/answers
```

소유권이 일치하지 않으면 다른 사용자의 훈련 로그, 점수, 피드백, 요약을 반환하지 않는다.

## 6. Boundary Rules

Training Service는 다음 기능을 구현하지 않는다.

```text
- 로그인
- 회원가입
- 사용자 프로필 관리
- 토큰 발급
- Refresh Token 저장
```

Training Service는 사용자 식별자를 저장할 수 있지만, 사용자 계정 원본 정보는 소유하지 않는다.
