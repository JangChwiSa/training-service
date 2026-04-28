# Training Service Task Plan

## 1. Purpose

이 문서는 Training Service 구현 작업을 작은 단위의 task로 나누기 위한 기준 문서이다.

각 task는 별도 브랜치에서 작업할 수 있을 정도로 나누며, API, DB, 인프라, 코드 품질, 테스트가 함께 완성되는 순서로 진행한다.

## 2. Task Rules

```text
- main 브랜치에 직접 구현하지 않는다.
- 구현 작업은 feature/* 브랜치를 사용한다.
- 문서만 수정하는 작업은 docs/* 브랜치를 사용한다.
- 한 브랜치에는 하나의 목적만 담는다.
- API 또는 DB 계약 변경이 필요하면 구현보다 먼저 문서를 수정한다.
- 각 task는 테스트 또는 검증 기준을 포함해야 완료로 본다.
- task 시작 전 관련 API/DB/architecture 문서가 존재하고 서로 충돌하지 않는지 확인한다.
- 필수 문서가 없거나 outbox처럼 구현에 필요한 DB 계약이 누락되어 있으면 구현 task보다 문서 정합성 task를 먼저 수행한다.
```

## 2.1 Current Documentation Gaps

구현 시작 전 다음 문서 정합성 문제를 먼저 해결한다.

```text
- AGENTS.md에는 docs/product/project-plan.md가 필수 문서로 적혀 있지만 현재 저장소에는 해당 파일이 없다.
- event outbox 테이블은 task와 event-outbox 문서에는 필요하다고 되어 있지만 DB spec에는 테이블 컬럼과 제약 조건이 없다.
```

필요한 선행 작업:

- `docs/product/project-plan.md`를 추가하거나 AGENTS.md의 Required Documents 목록에서 제거한다.
- `docs/database/db-spec.md`와 `docs/database/training-db-spec.md`에 outbox 테이블 명세를 먼저 추가한다.

## 3. Phase 0 - Project Scaffold

### Task 0.1 Spring Boot 프로젝트 생성

```text
Branch: feature/project-scaffold
```

참고 문서:

- `docs/architecture/training-service-architecture.md`
- `docs/development/local-development.md`
- `docs/development/git-strategy.md`

작업:

- Spring Boot 4.0.6 프로젝트 생성
- Java 21 설정
- 기본 package를 `com.jangchwisa.trainingservice`로 설정
- Maven 또는 Gradle 빌드 파일 구성
- 기본 application entrypoint 생성
- `local`, `test` profile 분리

완료 기준:

- 애플리케이션이 로컬에서 기동된다.
- 기본 context load test가 통과한다.
- 빌드 명령이 성공한다.

### Task 0.2 기본 패키지 구조 생성

```text
Branch: feature/package-structure
```

참고 문서:

- `docs/architecture/training-service-architecture.md`
- `AGENTS.md`

작업:

- `common`, `config`, `training`, `event`, `external`, `support` 패키지 생성
- `training.session`, `training.social`, `training.safety`, `training.focus`, `training.document`, `training.progress`, `training.score`, `training.feedback`, `training.summary` 하위 구조 생성
- 빈 패키지만 두지 않고 최소 marker class 또는 실제 초기 구성 클래스로 유지

완료 기준:

- 문서의 패키지 구조와 실제 코드 구조가 일치한다.
- 불필요한 broad utility 패키지가 없다.

## 4. Phase 1 - Local Infrastructure

### Task 1.1 Docker 로컬 실행 환경 구성

```text
Branch: feature/local-docker
```

참고 문서:

- `docs/development/local-development.md`
- `docs/architecture/training-service-architecture.md`
- `docs/architecture/overall-architecture.md`

작업:

- `Dockerfile` 작성
- `docker-compose.yml` 작성
- Training Service 컨테이너와 MySQL 컨테이너 구성
- MySQL database를 `training_db`로 설정
- MySQL healthcheck 구성

완료 기준:

- `docker compose up --build`로 서비스와 MySQL이 기동된다.
- 애플리케이션이 MySQL 연결을 확인할 수 있다.
- Voice Service, User Service, Report Service 컨테이너를 포함하지 않는다.

### Task 1.2 환경 변수 예시 파일 작성

```text
Branch: chore/add-env-example
```

참고 문서:

- `docs/development/local-development.md`
- `docs/architecture/security-context.md`
- `docs/architecture/openai-integration.md`
- `docs/architecture/event-outbox.md`

작업:

- `.env.example` 작성
- DB 접속 정보, OpenAI 설정, trusted user header, outbox/event 설정 예시 추가
- 실제 secret 값은 포함하지 않음

완료 기준:

- `.env.example`만으로 로컬 설정 항목을 파악할 수 있다.
- 실제 API key, 운영 password가 저장소에 포함되지 않는다.

## 5. Phase 2 - Database Migration

### Task 2.1 Migration 도구 설정

```text
Branch: feature/database-migration-setup
```

참고 문서:

- `docs/database/db-spec.md`
- `docs/database/training-db-spec.md`
- `docs/development/local-development.md`

작업:

- Flyway 또는 Liquibase 설정
- test profile에서 migration 실행되도록 구성

완료 기준:

- 빈 MySQL에서 migration이 순서대로 실행된다.
- migration 실패 시 테스트가 실패한다.

### Task 2.2 Integration test baseline for migration

```text
Branch: test/migration-baseline
```

참고 문서:

- `docs/database/db-spec.md`
- `docs/database/training-db-spec.md`
- `docs/development/local-development.md`

작업:

- Testcontainers MySQL 구성
- test profile에서 MySQL 기반 migration 검증이 가능하도록 설정
- 빈 DB에서 migration이 끝까지 실행되는 smoke test 작성

완료 기준:

- 로컬 테스트에서 MySQL 컨테이너 기반 migration 검증이 통과한다.
- migration 실패 시 테스트가 실패한다.
- 이후 schema task가 이 테스트 기반을 재사용할 수 있다.

### Task 2.3 Training DB core schema 작성

```text
Branch: feature/training-core-schema
```

참고 문서:

- `docs/database/db-spec.md`
- `docs/database/training-db-spec.md`
- `docs/architecture/event-outbox.md`
- `docs/api/training-api-spec.md`

작업:

- `training_sessions`
- `training_scores`
- `training_feedbacks`
- `training_session_summaries`
- event outbox 테이블

완료 기준:

- outbox 테이블은 DB spec에 컬럼, 상태값, retry/DLQ 관련 필드, 인덱스가 먼저 정의되어 있다.
- `training_session_summaries`는 목록 API 스냅샷 조회에 필요한 필드를 포함한다.
- `user_id`는 외부 사용자 ID 참조값으로 저장하고 `users.user_id` 물리 FK를 만들지 않는다.
- `session_id` 기반 FK와 unique 제약이 문서와 일치한다.

### Task 2.4 Training module schema 작성

```text
Branch: feature/training-module-schema
```

참고 문서:

- `docs/database/db-spec.md`
- `docs/database/training-db-spec.md`
- `docs/modules/social-training.md`
- `docs/modules/safety-training.md`
- `docs/modules/focus-training.md`
- `docs/modules/document-training.md`

작업:

- social tables
- safety tables
- focus tables
- document tables
- user progress tables

완료 기준:

- 모든 Training Service 소유 테이블이 migration에 포함된다.
- 콘텐츠 테이블에는 `user_id`를 저장하지 않는다.
- 사용자 수행 결과/progress/summary에는 `user_id`를 저장한다.

### Task 2.5 Local seed and fixture data 작성

```text
Branch: feature/local-seed-data
```

참고 문서:

- `docs/database/training-db-spec.md`
- `docs/api/training-api-spec.md`
- `docs/modules/social-training.md`
- `docs/modules/safety-training.md`
- `docs/modules/focus-training.md`
- `docs/modules/document-training.md`

작업:

- social scenario seed 데이터 작성
- safety scenario/scene/choice seed 데이터 작성
- focus level rule seed 데이터 작성
- document question seed 데이터 작성
- 테스트용 fixture와 local seed의 책임 분리

완료 기준:

- 로컬 환경에서 각 훈련 시작 API가 최소 1개 이상의 활성 콘텐츠로 동작할 수 있다.
- seed 데이터는 Training Service 소유 콘텐츠 테이블만 대상으로 한다.
- user progress, session, score, feedback 같은 사용자 수행 결과는 seed로 만들지 않는다.

## 6. Phase 3 - Common Foundation

### Task 3.1 공통 응답과 예외 처리

```text
Branch: feature/common-api-response
```

참고 문서:

- `docs/api/api-spec.md`
- `docs/api/training-api-spec.md`
- `docs/architecture/training-service-architecture.md`

작업:

- 공통 응답 포맷 구현
- 공통 오류 응답 구현
- global exception handler 구현
- validation error 응답 구현

완료 기준:

- API spec의 공통 응답/오류 구조와 일치한다.
- validation 실패 테스트가 있다.

### Task 3.2 Security context 구현

```text
Branch: feature/security-context
```

참고 문서:

- `docs/architecture/security-context.md`
- `docs/api/api-spec.md`
- `docs/api/training-api-spec.md`
- `AGENTS.md`

작업:

- trusted header `X-User-Id` 기반 current user resolver 구현
- 외부 API에서 request body/query `userId`를 사용하지 않도록 controller contract 구성
- 내부 API의 path `userId` 사용 경계 분리

완료 기준:

- 외부 API는 current user context에서만 userId를 얻는다.
- `X-User-Id` 누락/잘못된 값 테스트가 있다.
- 로그인, 회원가입, 토큰 발급 로직이 없다.

### Task 3.3 Session ownership validator 구현

```text
Branch: feature/session-ownership-validation
```

참고 문서:

- `docs/architecture/security-context.md`
- `docs/api/training-api-spec.md`
- `docs/database/training-db-spec.md`
- `AGENTS.md`

작업:

- `sessionId`와 current `userId` 소유권 검증 공통 서비스 구현
- 상세 조회, 다음 장면, 완료 처리에서 재사용 가능하게 구성

완료 기준:

- 다른 사용자의 session 접근은 실패한다.
- not found와 forbidden 정책이 테스트로 고정된다.

## 7. Phase 4 - Session and Query Core

### Task 4.1 Training session core 구현

```text
Branch: feature/training-session-core
```

참고 문서:

- `docs/database/training-db-spec.md`
- `docs/api/training-api-spec.md`
- `docs/architecture/training-service-architecture.md`

작업:

- `TrainingSession` entity/repository/service 구현
- training type, status, current step, started/ended time 처리
- session 생성 공통 로직 구현

완료 기준:

- SOCIAL, SAFETY, FOCUS, DOCUMENT 공통 세션 생성에 재사용 가능하다.
- status 전이 테스트가 있다.

### Task 4.2 Progress summary API 구현

```text
Branch: feature/training-progress-api
```

참고 문서:

- `docs/api/api-spec.md`
- `docs/api/training-api-spec.md`
- `docs/database/training-db-spec.md`
- `docs/architecture/security-context.md`

작업:

- `GET /api/trainings/progress?type={trainingType}`
- social/safety/document/focus progress 조회

완료 기준:

- current user 기준으로만 조회한다.
- 데이터 없음 기본 응답 정책이 테스트로 고정된다.

### Task 4.3 Training session list API 구현

```text
Branch: feature/training-session-list-api
```

참고 문서:

- `docs/api/api-spec.md`
- `docs/api/training-api-spec.md`
- `docs/database/db-spec.md`
- `docs/database/training-db-spec.md`

작업:

- `GET /api/trainings/sessions`
- `training_session_summaries`만 조회
- SAFETY category 필터
- `completed_at DESC` 정렬
- paging 처리

완료 기준:

- 목록 API에서 원본 로그, score, feedback 테이블을 조회하지 않는다.
- SOCIAL, SAFETY, DOCUMENT, FOCUS 응답 테스트가 있다.

### Task 4.4 Internal training query API 구현

```text
Branch: feature/internal-training-query-api
```

참고 문서:

- `docs/api/api-spec.md`
- `docs/api/training-api-spec.md`
- `docs/architecture/security-context.md`
- `docs/database/training-db-spec.md`

작업:

- `GET /internal/trainings/users/{userId}/summary`
- `GET /internal/trainings/users/{userId}/latest-results`
- 내부 API 호출자 경계와 외부 API 노출 방지 설정

완료 기준:

- summary API는 progress 테이블만 조회한다.
- latest-results API는 completed session, score, feedback 기준으로 응답한다.
- 외부 `/api/trainings/**` API와 달리 내부 API에서만 path `userId`를 허용한다.
- Report Service DB에는 직접 접근하지 않는다.

## 8. Phase 5 - Training Modules

### Task 5.1 Social training API 구현

```text
Branch: feature/social-training-api
```

참고 문서:

- `docs/api/api-spec.md`
- `docs/api/training-api-spec.md`
- `docs/database/training-db-spec.md`
- `docs/modules/social-training.md`
- `docs/architecture/sequence-diagrams.md`

작업:

- job type 선택 API
- social scenario 목록/상세 조회
- social session 시작
- social detail 조회

완료 기준:

- `training_sessions.sub_type = jobType` 저장 기준을 따른다.
- scenario 조회는 active content 기준으로 동작한다.
- detail 조회는 session ownership을 검증한다.

### Task 5.2 Safety training API 구현

```text
Branch: feature/safety-training-api
```

참고 문서:

- `docs/api/api-spec.md`
- `docs/api/training-api-spec.md`
- `docs/database/training-db-spec.md`
- `docs/modules/safety-training.md`
- `docs/architecture/sequence-diagrams.md`

작업:

- safety scenario 목록 조회
- safety session 시작 및 첫 장면 조회
- next scene 처리
- safety detail 조회

완료 기준:

- 선택 이력 저장과 current step 갱신이 동작한다.
- session ownership 검증 테스트가 있다.
- category 필터가 동작한다.

### Task 5.3 Focus training API 구현

```text
Branch: feature/focus-training-api
```

참고 문서:

- `docs/api/api-spec.md`
- `docs/api/training-api-spec.md`
- `docs/database/training-db-spec.md`
- `docs/modules/focus-training.md`
- `docs/architecture/sequence-diagrams.md`

작업:

- focus progress 조회
- focus session 시작
- `POST /api/trainings/focus/sessions` 안에서 focus commands 생성 후 응답으로 반환
- focus reaction logs는 완료 API에서 일괄 제출되도록 DTO/검증 기준 준비

완료 기준:

- level은 `training_sessions.sub_type`에 저장한다.
- focus detail API는 만들지 않는다.
- 별도 focus command 조회 API를 만들지 않는다.
- command/reaction 기본 제약 테스트가 있다.

### Task 5.4 Document training API 구현

```text
Branch: feature/document-training-api
```

참고 문서:

- `docs/api/api-spec.md`
- `docs/api/training-api-spec.md`
- `docs/database/training-db-spec.md`
- `docs/modules/document-training.md`
- `docs/architecture/sequence-diagrams.md`

작업:

- document session 시작
- question 제공
- `POST /api/trainings/document/sessions/{sessionId}/answers` 요청/응답 DTO와 검증 기준 준비
- document detail 조회

완료 기준:

- question별 중복 답변 정책이 테스트로 고정된다.
- detail 조회는 session ownership을 검증한다.
- 실제 답변 저장, 채점, 완료 처리는 Module completion task에서 구현한다.

## 9. Phase 6 - OpenAI Evaluation Boundary

### Task 6.1 OpenAI adapter boundary 구현

```text
Branch: feature/openai-adapter
```

참고 문서:

- `docs/architecture/openai-integration.md`
- `docs/architecture/training-service-architecture.md`
- `docs/api/training-api-spec.md`
- `AGENTS.md`

작업:

- OpenAI adapter interface
- local fake adapter
- timeout 설정
- 요청/응답 DTO
- evaluation result를 score/feedback 저장 모델로 변환하는 내부 DTO

완료 기준:

- 테스트에서 실제 OpenAI API를 호출하지 않는다.
- OpenAI 원문 응답을 외부 API로 그대로 노출하지 않는다.
- completion flow는 adapter interface만 의존할 수 있다.

### Task 6.2 AI evaluation integration 구현

```text
Branch: feature/openai-training-evaluation
```

참고 문서:

- `docs/architecture/openai-integration.md`
- `docs/api/training-api-spec.md`
- `docs/database/training-db-spec.md`
- `docs/modules/social-training.md`

작업:

- social dialogue evaluation
- training score generation
- feedback generation
- raw metrics 저장
- timeout/retry/fallback 정책 구현

완료 기준:

- 실패/timeout/fallback 정책이 테스트로 고정된다.
- 개인정보와 불필요한 user profile을 OpenAI 요청에 포함하지 않는다.
- fallback을 사용한 경우 feedback_source 또는 raw_metrics_json에 근거를 남긴다.

## 10. Phase 7 - Completion Flow

### Task 7.1 Completion transaction core 구현

```text
Branch: feature/training-completion-core
```

참고 문서:

- `docs/architecture/event-outbox.md`
- `docs/architecture/openai-integration.md`
- `docs/api/training-api-spec.md`
- `docs/database/training-db-spec.md`
- `AGENTS.md`

작업:

- 완료 처리 공통 트랜잭션 구성
- 원본 로그/결과 저장
- score 저장
- feedback 저장
- progress 갱신
- summary 생성
- session completed 처리
- 같은 트랜잭션 안에서 outbox event 저장

완료 기준:

- 완료 처리 순서가 event-outbox 문서와 일치한다.
- 완료 데이터와 outbox event가 같은 트랜잭션에서 저장된다.
- 중간 실패 시 트랜잭션 rollback 테스트가 있다.
- duplicate completion 테스트가 있다.
- 이미 완료된 세션은 score, feedback, summary, event를 중복 생성하지 않는다.

### Task 7.2 Module completion 구현

```text
Branch: feature/training-module-completion
```

참고 문서:

- `docs/api/api-spec.md`
- `docs/api/training-api-spec.md`
- `docs/database/training-db-spec.md`
- `docs/architecture/event-outbox.md`
- `docs/architecture/sequence-diagrams.md`

작업:

- social complete
- safety complete
- focus complete
- document answers submit and complete

완료 기준:

- 각 완료 API가 score, feedback, progress, summary, outbox event를 생성한다.
- 이미 완료된 세션에 대한 중복 요청 정책이 테스트로 고정된다.

## 11. Phase 8 - Event Outbox Publisher

### Task 8.1 Event publisher 구현

```text
Branch: feature/training-completed-publisher
```

참고 문서:

- `docs/architecture/event-outbox.md`
- `docs/api/training-api-spec.md`
- `docs/database/training-db-spec.md`
- `docs/architecture/overall-architecture.md`

작업:

- outbox publisher
- publish success 상태 변경
- retry 대상 상태 유지
- DLQ 전환 기준 구현 준비

완료 기준:

- completion flow가 저장한 outbox event를 발행 대상으로 조회한다.
- publish 성공/실패 테스트가 있다.
- eventId가 payload에 포함된다.
- Report Service DB에 직접 접근하지 않는다.

## 12. Phase 9 - Infra Hardening

### Task 9.1 Health check and actuator

```text
Branch: feature/health-check
```

참고 문서:

- `docs/development/local-development.md`
- `docs/architecture/training-service-architecture.md`

작업:

- actuator health 설정
- DB health 확인
- local docker healthcheck와 연결

완료 기준:

- `/actuator/health`가 DB 상태를 포함한다.
- docker compose 환경에서 healthcheck가 통과한다.

### Task 9.2 Logging and tracing baseline

```text
Branch: feature/logging-tracing-baseline
```

참고 문서:

- `docs/architecture/overall-architecture.md`
- `docs/architecture/security-context.md`
- `docs/architecture/openai-integration.md`
- `docs/architecture/event-outbox.md`

작업:

- request logging 기준 설정
- error logging 기준 설정
- event publish log 기준 설정
- trace id 또는 correlation id 수용 준비

완료 기준:

- 민감 정보와 OpenAI API key를 로그에 남기지 않는다.
- sessionId/userId 기반 운영 추적이 가능하다.

## 13. Phase 10 - Test and Quality Gate

### Task 10.1 Repository and service integration tests

```text
Branch: test/integration-baseline
```

참고 문서:

- `docs/database/training-db-spec.md`
- `docs/api/training-api-spec.md`
- `docs/architecture/event-outbox.md`
- `AGENTS.md`

작업:

- Phase 2의 Testcontainers MySQL 기반 재사용
- repository integration test
- 주요 service transaction integration test

완료 기준:

- CI 또는 로컬 verify에서 MySQL 기반 repository/service 테스트가 통과한다.
- migration 검증은 Phase 2 baseline과 중복 구현하지 않는다.

### Task 10.2 API contract tests

```text
Branch: test/api-contract
```

참고 문서:

- `docs/api/api-spec.md`
- `docs/api/training-api-spec.md`
- `docs/architecture/security-context.md`
- `AGENTS.md`

작업:

- progress API contract test
- session list API contract test
- detail API contract test
- completion API contract test

완료 기준:

- API spec의 request/response와 테스트가 일치한다.
- userId body/query 사용이 없음을 테스트한다.

### Task 10.3 Boundary and regression tests

```text
Branch: test/boundary-regression
```

참고 문서:

- `docs/architecture/security-context.md`
- `docs/architecture/openai-integration.md`
- `docs/architecture/event-outbox.md`
- `docs/api/training-api-spec.md`
- `AGENTS.md`

작업:

- session ownership regression test
- duplicate completion regression test
- invalid session access test
- OpenAI failure boundary test
- outbox retry boundary test

완료 기준:

- AGENTS.md Testing Guidelines의 핵심 항목을 모두 커버한다.

## 14. Implementation Order

권장 구현 순서는 다음과 같다.

```text
0. Documentation gaps 정리
1. Phase 0 - Project Scaffold
2. Phase 1 - Local Infrastructure
3. Phase 2 - Database Migration, migration test baseline, seed data
4. Phase 3 - Common Foundation
5. Phase 4 - Session and Query Core, internal query API
6. Phase 5 - Training Modules
7. Phase 6 - OpenAI Evaluation Boundary
8. Phase 7 - Completion Flow with outbox storage
9. Phase 8 - Event Outbox Publisher
10. Phase 9 - Infra Hardening
11. Phase 10 - Test and Quality Gate
```

Phase 6 이후부터는 OpenAI integration, completion flow, event outbox가 서로 영향을 주므로 API/DB 문서와 테스트를 함께 확인한다.
