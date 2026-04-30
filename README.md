# Training Service

Training Service는 훈련 콘텐츠 조회, 훈련 세션 생성, 진행 상태 관리, 점수/피드백 저장, 훈련 완료 요약 생성, `TrainingCompleted` 이벤트 발행을 담당하는 Spring Boot 기반 백엔드 서비스입니다.

이 서비스는 `training_db`만 소유하며, 인증/회원/리포트/음성 처리 로직은 포함하지 않습니다. 사용자 식별자는 API Gateway가 검증 후 전달하는 신뢰 헤더에서만 읽습니다.

## 실행 방법

### 요구 사항

- Java 21
- Maven 3.9 이상
- MySQL 8.4 이상
- OpenAI API Key
- Event Broker HTTP endpoint, 운영 이벤트 발행을 켜는 경우

### 주요 환경변수

| 변수 | 설명 |
| --- | --- |
| `SPRING_PROFILES_ACTIVE` | 운영 배포 프로필. 배포 환경에서 명시한다. |
| `SERVER_PORT` | 서비스 포트. 기본값 `8080`. |
| `SPRING_DATASOURCE_URL` | 운영 MySQL JDBC URL. 예: `jdbc:mysql://<host>:3306/training_db?useSSL=true&serverTimezone=UTC`. |
| `SPRING_DATASOURCE_USERNAME` | 운영 DB 사용자. |
| `SPRING_DATASOURCE_PASSWORD` | 운영 DB 비밀번호. |
| `SPRING_FLYWAY_LOCATIONS` | 운영에서는 `classpath:db/migration`. 로컬 seed migration을 포함하지 않는다. |
| `TRUSTED_USER_ID_HEADER` | Gateway가 전달하는 사용자 ID 헤더. 기본값 `X-User-Id`. |
| `OPENAI_ADAPTER` | 운영에서는 `openai`. |
| `OPENAI_API_KEY` | OpenAI API Key. 저장소에 커밋하지 않는다. |
| `OPENAI_MODEL` | 훈련 평가에 사용할 OpenAI 모델. |
| `OPENAI_BASE_URL` | 기본값 `https://api.openai.com/v1/responses`. |
| `OPENAI_TIMEOUT_MS` | OpenAI 요청 timeout. 기본값 `30000`. |
| `OUTBOX_PUBLISHER_ENABLED` | outbox 이벤트 발행 활성화 여부. 운영에서는 이벤트 브로커 준비 후 `true`. |
| `EVENT_BROKER_URL` | `TrainingCompleted` 이벤트를 전달할 브로커 URL. |
| `EVENT_BROKER_TIMEOUT_MS` | 이벤트 브로커 요청 timeout. 기본값 `5000`. |

### JAR 실행

```bash
mvn clean package
java -jar target/training-service-0.0.1-SNAPSHOT.jar
```

운영에서는 환경변수를 런타임 환경에서 주입한다. `.env`, API Key, DB 비밀번호, 운영 설정 파일은 저장소에 커밋하지 않는다.

### Docker 이미지

```bash
docker build -t training-service:prod .
docker run --rm -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e SPRING_DATASOURCE_URL='jdbc:mysql://<mysql-host>:3306/training_db?useSSL=true&serverTimezone=UTC' \
  -e SPRING_DATASOURCE_USERNAME=<db-user> \
  -e SPRING_DATASOURCE_PASSWORD=<db-password> \
  -e SPRING_FLYWAY_LOCATIONS=classpath:db/migration \
  -e OPENAI_ADAPTER=openai \
  -e OPENAI_API_KEY=<openai-api-key> \
  -e OPENAI_MODEL=<openai-model> \
  training-service:prod
```

### 상태 확인

```bash
curl http://localhost:8080/actuator/health
```

Swagger UI는 `/swagger-ui.html`에서 확인할 수 있다. 운영 노출 여부는 배포 환경 정책에 맞춰 제한한다.

## 디렉토리 구조

```text
src/main/java/com/jangchwisa/trainingservice
  common/        공통 응답, 예외, 로깅, 인증 사용자 헤더 처리
  config/        OpenAI, Swagger, Web MVC, 시간 설정
  event/         outbox 저장소, 이벤트 브로커 발행, 재시도/DLQ 처리
  external/      OpenAI 평가 어댑터
  support/       공통 지원 코드
  training/
    completion/  훈련 완료 공통 저장 흐름
    document/    문서 이해 훈련
    evaluation/  훈련 평가 오케스트레이션
    feedback/    훈련 피드백 저장
    focus/       집중력 훈련
    progress/    훈련 성취/진행 상태 조회
    safety/      안전 훈련
    score/       훈련 점수 저장
    session/     훈련 세션 저장
    social/      사회성 훈련
    summary/     훈련 기록 목록 및 내부 요약 조회

src/main/resources
  db/migration/  운영 스키마 Flyway migration
  db/seed/local/ 로컬 개발용 seed migration
```

## DB 구조

Training Service는 `training_db`를 소유한다. 운영 스키마는 `src/main/resources/db/migration`의 Flyway migration으로 관리한다.

### 핵심 세션/완료 테이블

| 테이블 | 역할 |
| --- | --- |
| `training_sessions` | 모든 훈련 세션의 공통 원장. 사용자, 훈련 유형, 세부 유형, 상태, 시작/완료 시각을 저장한다. |
| `training_scores` | 완료된 세션의 점수와 점수 유형, 원시 지표 JSON을 저장한다. |
| `training_feedbacks` | 완료된 세션의 피드백 요약/상세를 저장한다. |
| `training_session_summaries` | 목록/홈/현황 조회용 스냅샷. 원본 로그 대신 우선 조회한다. |
| `outbox_events` | `TrainingCompleted` 이벤트 발행을 위한 outbox. 재시도와 DLQ 상태를 관리한다. |

### 훈련별 테이블

| 영역 | 테이블 |
| --- | --- |
| 사회성 | `social_scenarios`, `social_dialog_logs`, `user_social_progress` |
| 안전 | `safety_scenarios`, `safety_scenes`, `safety_choices`, `safety_action_logs`, `user_safety_progress` |
| 집중력 | `focus_level_rules`, `focus_commands`, `focus_reaction_logs`, `user_focus_progress` |
| 문서 이해 | `document_questions`, `document_question_choices`, `document_session_questions`, `document_answer_logs`, `user_document_progress` |

### 진행 상태 기준

- `user_focus_progress.highest_unlocked_level` 이하의 집중력 레벨만 시작할 수 있다.
- `user_document_progress.highest_unlocked_level` 이하의 문서 이해 레벨만 시작할 수 있다.
- 훈련 완료 시 원본 로그/답변, 점수, 피드백, 사용자 진행 상태, `training_session_summaries`, outbox 이벤트가 같은 완료 흐름에서 저장된다.
- `sessionId` 기반 조회/완료 요청은 반드시 현재 사용자 소유 세션인지 검증한다.

## API

모든 외부 API는 Gateway가 전달한 사용자 ID 헤더를 사용한다. 클라이언트 request body/query의 `userId`는 신뢰하지 않는다.

### 공통

| Method | Path | 설명 |
| --- | --- | --- |
| `GET` | `/actuator/health` | 서비스 상태 확인 |
| `GET` | `/swagger-ui.html` | OpenAPI UI |

### 훈련 현황/기록

| Method | Path | 설명 |
| --- | --- | --- |
| `GET` | `/api/trainings/progress?type={trainingType}` | 훈련 유형별 이번 달 성취 레벨 조회 |
| `GET` | `/api/trainings/progress/summary` | 홈 화면용 전체 훈련 성취 요약 조회 |
| `GET` | `/api/trainings/sessions?type={trainingType}&page={page}&size={size}` | 훈련 기록 목록 조회 |

### 사회성 훈련

| Method | Path | 설명 |
| --- | --- | --- |
| `POST` | `/api/trainings/social/job-type` | 사회성 직무 유형 선택 |
| `GET` | `/api/trainings/social/scenarios?jobType={jobType}` | 직무 유형별 시나리오 목록 조회 |
| `GET` | `/api/trainings/social/scenarios/{scenarioId}` | 시나리오 상세 조회 |
| `POST` | `/api/trainings/social/sessions` | 사회성 훈련 세션 시작 |
| `GET` | `/api/trainings/social/sessions/{sessionId}/detail` | 사회성 훈련 상세 조회 |
| `POST` | `/api/trainings/social/sessions/{sessionId}/complete` | 대화 로그 제출 및 완료 |

### 안전 훈련

| Method | Path | 설명 |
| --- | --- | --- |
| `GET` | `/api/trainings/safety/scenarios?category={category}` | 안전 시나리오 목록 조회 |
| `POST` | `/api/trainings/safety/sessions` | 안전 훈련 세션 시작 및 첫 장면 조회 |
| `POST` | `/api/trainings/safety/sessions/{sessionId}/next-scene` | 선택지 제출 및 다음 장면 조회 |
| `GET` | `/api/trainings/safety/sessions/{sessionId}/detail` | 안전 훈련 상세 조회 |
| `POST` | `/api/trainings/safety/sessions/{sessionId}/complete` | 안전 훈련 완료 |

### 집중력 훈련

| Method | Path | 설명 |
| --- | --- | --- |
| `GET` | `/api/trainings/focus/progress` | 현재/해금 집중력 레벨 조회 |
| `POST` | `/api/trainings/focus/sessions` | 집중력 훈련 세션 시작 |
| `POST` | `/api/trainings/focus/sessions/{sessionId}/complete` | 반응 로그 제출 및 완료 |

### 문서 이해 훈련

| Method | Path | 설명 |
| --- | --- | --- |
| `GET` | `/api/trainings/document/progress` | 현재/해금 문서 이해 레벨 조회 |
| `POST` | `/api/trainings/document/sessions` | 문서 이해 훈련 세션 시작 |
| `GET` | `/api/trainings/document/sessions/{sessionId}/detail` | 문서 이해 세션 문제/답변 상세 조회 |
| `POST` | `/api/trainings/document/sessions/{sessionId}/answers` | 답변 제출 및 완료 |

### 내부 API

내부 API는 서비스 간 통신용이다. 외부 클라이언트에 직접 노출하지 않는다.

| Method | Path | 설명 |
| --- | --- | --- |
| `GET` | `/internal/trainings/users/{userId}/summary` | 사용자 훈련 요약 조회 |
| `GET` | `/internal/trainings/users/{userId}/latest-results` | 최신 훈련 결과 재조회 |

## 이벤트

훈련 완료 성공 후 Training Service는 `TrainingCompleted` 이벤트를 발행한다.

처리 순서:

1. 원본 로그 또는 결과 저장
2. 점수 저장
3. 피드백 저장
4. 사용자 진행 상태 갱신
5. `training_session_summaries` 생성
6. 세션 완료 처리
7. outbox 저장 및 이벤트 발행

이벤트 발행 실패 시 outbox 재시도 정책을 따른다.

## 참고 문서

- `docs/api/training-api-spec.md`
- `docs/database/training-db-spec.md`
- `docs/architecture/overall-architecture.md`
- `docs/architecture/sequence-diagrams.md`
