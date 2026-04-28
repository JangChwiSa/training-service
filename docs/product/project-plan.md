# Project Plan

## 1. Purpose

이 문서는 Training Service 구현의 제품 목표와 범위를 정의한다.

이 저장소는 전체 시스템 중 Training Service만 구현한다. Training Service는 훈련 콘텐츠, 훈련 세션, 훈련 진행 상태, 훈련 로그, 점수, 피드백, 세션 요약, `TrainingCompleted` 이벤트 발행을 담당한다.

## 2. Product Goal

Training Service의 목표는 발달장애인 직업 훈련 과정에서 발생하는 훈련 수행 기록을 안정적으로 저장하고, 훈련 결과를 점수와 피드백으로 정리하여 리포트 갱신에 사용할 수 있게 만드는 것이다.

지원하는 훈련 유형은 다음과 같다.

```text
- Social training
- Safety training
- Focus training
- Document training
```

## 3. User-Facing Capabilities

Training Service는 API Gateway 뒤에서 다음 사용자 기능을 지원한다.

```text
- 훈련 유형별 진행 현황 조회
- 완료된 훈련 기록 목록 조회
- 사회성, 안전, 문서 이해 훈련 상세 조회
- 사회성 훈련 시나리오 조회 및 세션 시작
- 안전 훈련 시나리오 조회, 세션 시작, 다음 장면 진행
- 집중력 훈련 진행 상태 조회, 세션 시작, 반응 결과 제출
- 문서 이해 훈련 세션 시작, 답변 제출, 결과 조회
- 훈련 완료 후 점수, 피드백, 진행 상태, 요약 생성
```

집중력 훈련은 별도 상세 조회 API를 제공하지 않고, 진행 현황과 훈련 기록 목록으로 결과 확인을 대체한다.

## 4. Service Boundaries

Training Service는 다음을 구현하지 않는다.

```text
- 로그인
- 회원가입
- 사용자 프로필 관리
- STT
- TTS
- 실시간 음성 대화 처리
- 리포트 집계
- 리포트 해석
- 프론트엔드 렌더링
```

Training Service는 `training_db`만 직접 사용한다. `user_db`와 `report_db`에는 직접 접근하지 않는다.

사용자 식별자는 API Gateway 또는 신뢰된 내부 호출자가 전달한 인증 컨텍스트에서 얻는다. 외부 API는 request body 또는 query parameter로 `userId`를 받지 않는다.

## 5. Completion and Reporting Flow

훈련 완료 처리는 다음 순서를 기준으로 한다.

```text
1. 원본 로그 또는 결과 저장
2. 점수 저장
3. 피드백 저장
4. 사용자 진행 상태 갱신
5. 훈련 세션 요약 생성
6. 세션 완료 처리
7. outbox event 저장
8. TrainingCompleted 이벤트 발행
```

완료 데이터와 outbox event 저장은 같은 트랜잭션에서 처리한다. Event Broker 발행은 별도 publisher가 수행한다.

Report Service는 `TrainingCompleted` 이벤트를 소비하여 report data를 갱신한다. Training Service는 Report Service DB를 직접 갱신하지 않는다.

## 6. OpenAI Usage

Training Service는 완료된 훈련 결과 분석, 점수 생성, 피드백 생성을 위해 OpenAI API를 사용할 수 있다.

기본 기준은 다음과 같다.

```text
- Social training: 완료된 dialogue logs 기반 AI 평가와 피드백 생성
- Safety training: 선택 결과 기반 deterministic scoring, 필요 시 adaptive feedback에 AI 사용
- Focus training: 반응 결과 기반 deterministic scoring, 필요 시 adaptive feedback에 AI 사용
- Document training: 정답 기준 deterministic scoring, 필요 시 adaptive feedback에 AI 사용
```

Training Service는 STT, TTS, 실시간 음성 대화 생성에는 OpenAI API를 사용하지 않는다. 해당 책임은 Voice Service에 있다.

OpenAI 요청에는 불필요한 사용자 프로필과 개인정보를 포함하지 않는다.

## 7. Success Criteria

Training Service 구현은 다음 조건을 만족해야 한다.

```text
- API 명세와 실제 request/response가 일치한다.
- DB 명세와 migration/entity가 일치한다.
- sessionId 기반 API는 현재 사용자 소유권을 검증한다.
- 훈련 완료 시 score, feedback, progress, summary, outbox event가 생성된다.
- 중복 완료 요청은 중복 score, feedback, summary, event를 만들지 않는다.
- 테스트에서 실제 OpenAI API를 호출하지 않는다.
- Training Service가 user_db 또는 report_db에 직접 접근하지 않는다.
```
