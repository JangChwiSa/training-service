# Local Development

## 1. Purpose

이 문서는 Training Service를 로컬에서 개발하고 실행하기 위한 환경 기준을 정의한다.

실제 `Dockerfile`, `docker-compose.yml`, `.env.example` 파일은 별도 작업에서 생성한다. 이 문서는 해당 파일을 만들 때 따라야 할 기준을 설명한다.

## 2. Technology Baseline

```text
Language: Java 21
Framework: Spring Boot 4.0.6
Database: MySQL
Database name: training_db
Container runtime: Docker
```

Training Service는 로컬 개발 환경에서도 `training_db`만 직접 사용한다.

`user_db`와 `report_db`는 로컬 개발 환경에 포함하지 않는다.

## 3. Docker Scope

로컬 Docker 구성은 다음 컨테이너를 기본 대상으로 한다.

```text
training-service
- Spring Boot 애플리케이션
- /api/trainings/** API 제공
- /internal/trainings/** API 제공

mysql
- Training Service 전용 MySQL
- database = training_db
```

OpenAI API, Report Service, Event Broker는 로컬 Docker 구성의 필수 컨테이너가 아니다.

필요한 경우 외부 서비스 endpoint 또는 mock adapter를 사용한다.

## 4. Environment Variables

`.env.example`에는 다음 값을 문서화한다.

```text
SPRING_PROFILES_ACTIVE=local
SERVER_PORT=8080

DB_HOST=mysql
DB_PORT=3306
DB_NAME=training_db
DB_USERNAME=training_user
DB_PASSWORD=training_password

OPENAI_API_KEY=
OPENAI_TIMEOUT_MS=30000

TRUSTED_USER_ID_HEADER=X-User-Id

EVENT_BROKER_ENABLED=false
EVENT_BROKER_ENDPOINT=
OUTBOX_PUBLISH_ENABLED=false
```

민감한 값은 저장소에 커밋하지 않는다.

실제 OpenAI API 키, 운영 DB 비밀번호, 운영 이벤트 브로커 접속 정보는 로컬 예시 파일에 포함하지 않는다.

## 5. Local Run Flow

로컬 실행 흐름은 다음 기준을 따른다.

```text
1. MySQL 컨테이너를 먼저 실행한다.
2. training_db 데이터베이스를 준비한다.
3. Training Service 애플리케이션을 local profile로 실행한다.
4. API 요청에는 trusted user header를 포함한다.
5. health check와 기본 API 응답을 확인한다.
```

로컬 요청 예시는 다음과 같은 사용자 식별 header를 사용한다.

```http
X-User-Id: 1
```

외부 API 요청 body 또는 query parameter에 `userId`를 넣지 않는다.

## 6. Development Boundaries

로컬 개발 환경에서도 다음 경계를 유지한다.

```text
- Training Service는 로그인, 회원가입, 사용자 프로필 관리를 구현하지 않는다.
- Training Service는 user_db 또는 report_db에 직접 접근하지 않는다.
- Training Service는 STT, TTS, 실시간 음성 대화를 구현하지 않는다.
- Training Service는 훈련 평가, 점수 생성, 피드백 생성을 위해서만 OpenAI API를 사용한다.
```
