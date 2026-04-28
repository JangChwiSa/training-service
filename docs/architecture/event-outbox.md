# Event and Outbox

## 1. Purpose

이 문서는 Training Service의 이벤트 발행 기준과 outbox 처리 기준을 정의한다.

Training Service가 발행하는 핵심 이벤트는 `TrainingCompleted`이다.

## 2. TrainingCompleted Event

`TrainingCompleted`는 훈련 세션이 정상적으로 완료된 후 발행한다.

Producer는 Training Service이다.

Consumer는 Report Service이다.

Report Service는 이 이벤트를 기반으로 리포트 데이터를 갱신한다.

Training Service는 `report_db`에 직접 접근하지 않는다.

## 3. Completion Flow

훈련 완료 처리는 다음 순서를 기준으로 한다.

```text
1. 원본 로그 또는 결과 저장
2. 점수 저장
3. 피드백 저장
4. 사용자 진행 상태 갱신
5. 훈련 세션 요약 생성
6. 세션 완료 처리
7. TrainingCompleted 이벤트 발행
```

이벤트는 점수, 피드백, progress, summary, session status가 저장된 후 발행한다.

## 4. Event Payload

기본 payload는 다음 형식을 기준으로 한다.

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

`eventId`는 소비자 멱등 처리를 위해 반드시 포함한다.

`userId`는 Report Service가 사용자별 리포트를 갱신하기 위한 식별자이다.

## 5. Outbox Rule

훈련 완료 데이터 저장과 이벤트 발행은 원자성 문제가 생길 수 있다.

이벤트 브로커 발행 실패로 완료 데이터와 이벤트 상태가 어긋나지 않도록 outbox 패턴을 사용한다.

기본 처리 기준은 다음과 같다.

```text
1. 훈련 완료 트랜잭션 안에서 완료 데이터와 outbox event를 함께 저장한다.
2. outbox publisher가 미발행 event를 조회한다.
3. Event Broker에 publish를 시도한다.
4. publish 성공 시 outbox event를 published 상태로 변경한다.
5. publish 실패 시 retry 대상 상태로 남긴다.
```

## 6. Retry and DLQ

일시적 실패는 retry 대상이다.

반복 실패하거나 복구 불가능한 payload 오류는 DLQ 대상으로 분류한다.

```text
Retry 대상:
- 일시적 네트워크 오류
- Event Broker 일시 장애
- timeout

DLQ 대상:
- payload schema 오류
- 필수 필드 누락
- 최대 retry 횟수 초과
```

DLQ로 이동한 이벤트는 운영자가 원인을 확인한 뒤 재처리 여부를 결정한다.

## 7. Idempotency

이벤트 소비자는 `eventId` 기준으로 멱등 처리한다.

Training Service는 같은 세션 완료 요청이 중복 처리되지 않도록 `sessionId` 기준 완료 상태를 확인한다.

이미 완료된 세션은 점수, 피드백, summary, event를 중복 생성하지 않는다.
