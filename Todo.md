# Didgo Todo

작성일: 2026-04-30

## 1. 방향 정리

- 독립 음성 서비스 디렉터리는 제거했고, 음성 기능은 `training-service`로 통합한다.
- 통합 대상은 사회성 훈련의 실시간 음성 대화 기능이다.
- 범용 STT/TTS 플랫폼이 아니라 사회성 훈련 전용 음성 대화 모듈로 본다.

## 2. 통합 범위

`training-service`로 흡수할 책임:

- 사회성 훈련용 실시간 음성 세션 시작/종료
- 브라우저와 서버 간 WebSocket 세션 관리
- OpenAI Realtime API relay
- 입력 음성 chunk 전달
- 출력 음성/텍스트 이벤트 relay
- transcript 수집
- 대화 로그 및 훈련 결과 원본 저장
- 종료 후 점수/요약/피드백 생성

별도로 유지하지 않을 것:

- 독립 음성 서비스 배포/라우팅/인프라 구성

## 3. 첫 AI 발화 요구사항

목표:

- `social/session` 시작 시 사용자가 먼저 말하지 않아도 AI가 먼저 말을 건 것처럼 느끼게 한다.

요구사항:

- 세션 시작 API 또는 세션 준비 단계에서 시나리오별 첫 AI 발화 문구를 확정한다.
- 클라이언트는 첫 AI 발화 음성을 재생한다.
- 이후 사용자는 그 발화에 이어서 대답한다.

핵심 판단:

- 첫 발화를 단순 음성 파일 재생으로만 처리하면 안 된다.
- 이후 Realtime 응답이 맥락에 맞으려면 첫 발화 내용이 Realtime 세션 컨텍스트에 들어가 있어야 한다.

권장 방식:

- 첫 발화 텍스트를 Realtime 대화 상태에 반영한다.
- 첫 발화 오디오는 최초 1회 TTS 생성 후 캐시된 자산으로 제공한다.
- 즉, 모델은 텍스트로 맥락을 알고, 사용자는 음성으로 듣는다.

## 4. 구현 원칙

- 첫 발화는 "고정 스크립트 + 최초 1회 TTS 생성 후 캐시" 방식으로 구현한다.
- 첫 턴부터 완전 실시간 생성으로 가지 않는다.
- 사회성 시나리오마다 `opening script`를 명시적으로 관리한다.
- 첫 발화 이후부터 사용자 음성과 Realtime 응답을 연결한다.

이유:

- 지연이 적다.
- 실패 지점이 줄어든다.
- 비용과 품질 변동을 줄일 수 있다.
- MVP에서 UX를 먼저 안정화할 수 있다.

세부 정책:

- 시나리오별 `opening_script`를 기준으로 음성 자산을 만든다.
- 음성 자산이 없으면 세션 시작 시 1회 생성한다.
- 생성된 결과는 파일 스토리지 또는 정적 자산 저장소에 캐시한다.
- 이후 요청은 캐시된 음성을 재사용한다.
- `opening_script`가 바뀌면 해시 기준으로 새 음성을 다시 생성한다.

## 5. API / 세션 작업 항목

- [x] `POST /api/trainings/social/sessions/{sessionId}/voice/prepare` 요청/응답 명세 확정
- [x] `WS /ws/trainings/social/voice` 클라이언트/서버 이벤트 계약 확정
- [x] 첫 AI 발화 전달 방식 확정: 세션 준비 API 응답 + WebSocket `session.ready` + `opening.play`
- [x] WebSocket 연결용 단기 `connectionToken` 발급/검증 방식 확정
- [x] 입력/출력 오디오 포맷 확정
- [x] 사회성 훈련 완료 API에 `voiceSummary` 메타데이터 포함 여부 확정
- [x] 세션 생성 직후 첫 AI 발화를 Realtime 세션 컨텍스트에 넣는 순서 정의
- [x] 사용자 첫 발화 이전에 어떤 서버 이벤트를 보낼지 정의
- [ ] transcript, turn log, audio session 메타데이터 저장 스키마 설계

### 5.1 음성/Realtime API 초안

공통 응답 규칙:

- 이 섹션의 Response 예시는 실제 HTTP 응답의 `data` payload를 설명한다.
- `training-service` 컨트롤러는 실제 응답을 `ApiResponse<T>`로 감싼다.
- 성공 응답 형식은 `{"success":true,"data":{...},"error":null}`이다.
- 실패 응답 형식은 `{"success":false,"data":null,"error":{"code":"...","message":"..."}}`이다.

#### 5.1.1 세션 준비 API

경로:

- `POST /api/trainings/social/sessions/{sessionId}/voice/prepare`

목적:

- 사회성 훈련 세션에 대한 실시간 음성 대화 준비
- 첫 AI 발화 정보 반환
- WebSocket 연결 정보 반환
- Realtime 세션 메타데이터 반환

정책:

- `sessionId`는 이미 생성된 사회성 훈련 세션 ID를 사용한다.
- `scenarioId`는 path로 받은 `sessionId`에 연결된 시나리오를 서버가 조회한다.
- 클라이언트는 `scenarioId`를 요청 body로 다시 보내지 않는다.
- 현재 사용자 소유 세션이 아니면 `FORBIDDEN`으로 실패한다.
- 세션 상태가 음성 준비를 허용하지 않으면 `CONFLICT`로 실패한다.
- 응답의 `connectionToken`은 WebSocket 연결에만 사용하는 단기 토큰이다.
- `connectionToken`은 만료되거나 한 번 사용되면 재사용할 수 없다.

Request:

```json
{}
```

Response:

```json
{
  "sessionId": 10,
  "scenarioId": 1,
  "connectionMode": "SERVER_RELAY",
  "realtime": {
    "wsUrl": "/ws/trainings/social/voice",
    "protocol": "json",
    "connectionToken": "voice-session-token",
    "expiresInSeconds": 300
  },
  "opening": {
    "script": "안녕하세요. 오늘은 같이 출근 상황을 연습해볼게요.",
    "audioUrl": "https://cdn.didgo/opening/social/1/hash123.mp3",
    "audioAssetStatus": "READY"
  },
  "conversation": {
    "voice": "alloy",
    "model": "gpt-realtime-mini",
    "instructionsVersion": "v1"
  }
}
```

필드:

- `sessionId`: 사회성 훈련 세션 ID
- `scenarioId`: 시나리오 ID
- `connectionMode`: 현재는 `SERVER_RELAY`
- `realtime.wsUrl`: 브라우저가 연결할 WebSocket 경로
- `realtime.protocol`: 클라이언트 이벤트 프로토콜
- `realtime.connectionToken`: WebSocket 연결 검증용 단기 토큰
- `opening.script`: 첫 AI 발화 텍스트
- `opening.audioUrl`: 캐시된 첫 AI 발화 음성 URL
- `opening.audioAssetStatus`: `READY | GENERATING | FAILED`
- `conversation.voice`: 응답 음성 타입
- `conversation.model`: Realtime 모델명. 기본값은 비용 최소화를 위해 `gpt-realtime-mini`를 사용한다.
- `conversation.instructionsVersion`: 시스템 instruction 버전

#### 5.1.2 WebSocket API

경로:

- `WS /ws/trainings/social/voice`

목적:

- 브라우저와 서버 간 실시간 음성 대화 relay
- 사용자 음성 chunk 전달
- AI 음성/텍스트 응답 전달
- transcript 및 turn lifecycle 이벤트 전달

연결 정책:

- 클라이언트는 세션 준비 API에서 받은 `connectionToken`으로 WebSocket 연결을 시작한다.
- 토큰 전달 방식은 query parameter `token`으로 확정한다.
- 서버는 `connectionToken`으로 `sessionId`, `userId`, 만료 시간을 검증한다.
- 토큰의 사용자와 세션 소유자가 다르면 연결을 거부한다.
- 토큰이 만료되었거나 이미 사용되었으면 연결을 거부한다.
- WebSocket 연결 후 `session.start` 이벤트의 `sessionId`는 토큰에 묶인 `sessionId`와 같아야 한다.

오디오 포맷 정책:

- 브라우저 -> 서버 입력 포맷은 MVP에서 `audio/pcm`으로 고정한다.
- 출력 포맷도 MVP에서 `audio/pcm`으로 고정한다.
- sample rate는 MVP에서 24kHz로 확정한다.
- 브라우저 녹음 원본이 WebM/Opus이면 클라이언트 또는 서버에서 PCM으로 변환한 뒤 relay한다.

클라이언트 -> 서버 이벤트:

`session.start`

```json
{
  "type": "session.start",
  "sessionId": 10
}
```

`audio.chunk`

```json
{
  "type": "audio.chunk",
  "sessionId": 10,
  "chunkBase64": "<pcm-bytes>",
  "mimeType": "audio/pcm",
  "sequence": 1
}
```

`audio.commit`

```json
{
  "type": "audio.commit",
  "sessionId": 10
}
```

`response.request`

```json
{
  "type": "response.request",
  "sessionId": 10
}
```

`session.finish`

```json
{
  "type": "session.finish",
  "sessionId": 10
}
```

서버 -> 클라이언트 이벤트:

`session.ready`

```json
{
  "type": "session.ready",
  "sessionId": 10,
  "opening": {
    "script": "안녕하세요. 오늘은 같이 출근 상황을 연습해볼게요.",
    "audioUrl": "https://cdn.didgo/opening/social/1/hash123.mp3"
  }
}
```

`opening.play`

```json
{
  "type": "opening.play",
  "sessionId": 10,
  "script": "안녕하세요. 오늘은 같이 출근 상황을 연습해볼게요."
}
```

`transcript.partial`

```json
{
  "type": "transcript.partial",
  "sessionId": 10,
  "speaker": "USER",
  "turnNo": 1,
  "text": "안녕하세..."
}
```

`transcript.final`

```json
{
  "type": "transcript.final",
  "sessionId": 10,
  "speaker": "USER",
  "turnNo": 1,
  "text": "안녕하세요."
}
```

`audio.out`

```json
{
  "type": "audio.out",
  "sessionId": 10,
  "turnNo": 2,
  "chunkBase64": "<audio-bytes>",
  "mimeType": "audio/pcm"
}
```

`turn.complete`

```json
{
  "type": "turn.complete",
  "sessionId": 10,
  "turnNo": 2,
  "speaker": "AI",
  "finalText": "좋아요. 먼저 팀장님께 인사해볼까요?"
}
```

`session.completed`

```json
{
  "type": "session.completed",
  "sessionId": 10,
  "status": "COMPLETED"
}
```

`error`

```json
{
  "type": "error",
  "sessionId": 10,
  "code": "REALTIME_PROVIDER_UNAVAILABLE",
  "message": "실시간 음성 연결에 실패했습니다."
}
```

#### 5.1.3 세션 완료 API 확장안

경로:

- `POST /api/trainings/social/sessions/{sessionId}/complete`

목적:

- 실시간 대화 종료 후 transcript와 메타데이터 제출
- 평가, 점수, 피드백 생성
- 세션 완료 처리

Request:

```json
{
  "dialogLogs": [
    {
      "turnNo": 1,
      "speaker": "AI",
      "content": "안녕하세요. 오늘은 같이 출근 상황을 연습해볼게요."
    },
    {
      "turnNo": 1,
      "speaker": "USER",
      "content": "네, 안녕하세요."
    }
  ],
  "voiceSummary": {
    "transcriptSource": "REALTIME",
    "audioSessionId": "rt_123",
    "durationSeconds": 83
  }
}
```

Response:

```json
{
  "sessionId": 10,
  "score": 84,
  "feedbackSummary": "상황에 맞는 표현으로 대화를 잘 이어갔습니다.",
  "completed": true
}
```

#### 5.1.4 현재 확정 방향

- 세션 준비 API 응답에 `opening.script`와 `opening.audioUrl`을 포함한다.
- 첫 AI 발화는 세션 준비 API 응답으로도 내려주고, WebSocket 연결 후 `session.ready`에서 다시 확인한다.
- 실제 첫 발화 재생 트리거는 `opening.play` 이벤트로 분리한다.
- 완료 API는 기존 `dialogLogs`에 더해 `voiceSummary` 메타데이터를 받는다.
- WebSocket 연결에는 세션 준비 API에서 발급한 단기 `connectionToken`을 사용한다.
- 오디오 포맷은 MVP에서 입출력 모두 `audio/pcm`으로 고정한다.

## 6. 데이터 모델 작업 항목

- [ ] 사회성 시나리오에 첫 발화 스크립트 저장 필드 추가 여부 결정
- [ ] 첫 발화 음성 자산 저장 방식 확정
  - 요청 시 최초 1회 TTS 생성
  - 생성 결과 캐시
- [ ] 캐시 키 규칙 정의
  - `scenarioId`
  - `opening_script` hash
- [ ] 음성 자산 메타데이터 필드 설계
  - `opening_audio_asset_key` 또는 `opening_audio_url`
  - `opening_audio_script_hash`
  - `opening_audio_status`
  - `opening_audio_generated_at`
- [ ] 세션별 첫 발화 사용 이력 저장 여부 결정
- [ ] transcript, turn log, feedback raw data 저장 스키마 설계

## 7. Realtime 연동 작업 항목

- [x] Realtime 세션 초기화 시 사용할 instructions 설계
- [x] 첫 AI 발화 텍스트를 세션 컨텍스트에 반영하는 방식 확정
- [x] 이후 사용자 음성이 들어오면 같은 세션 컨텍스트를 유지하도록 흐름 정의
- [x] Realtime 응답이 첫 발화와 충돌하지 않도록 턴 lifecycle 정리
- [x] 첫 발화 컨텍스트 반영 이벤트 전송 테스트 케이스 추가
- [ ] 첫 발화 음성 생성 실패 시 fallback 정의
  - 텍스트만 반환
  - 재시도 정책
- [ ] 동일 시나리오 동시 요청 시 TTS 중복 생성 방지 정책 정의

## 8. 사회성 대화 평가 기준

평가 목표:

- 사용자의 대화가 시나리오 맥락에 맞았는지 판단한다.
- 간단한 평가 문장과 함께 1~100점 기준 점수를 생성한다.
- 총점은 세부 항목 점수 합산 방식으로 계산한다.

총점 구성:

- `맥락 적합성` 30점
- `의사표현 적절성` 25점
- `상호작용 완성도` 20점
- `과제 수행도` 15점
- `기본 대화 태도` 10점

세부 기준:

### 8.1 맥락 적합성 30점

- 시나리오 상황을 이해하고 그에 맞게 반응했는가
- 상대방 질문, 요청, 안내에 벗어나지 않게 답했는가
- 무관한 발화나 엇나간 대화가 적었는가

### 8.2 의사표현 적절성 25점

- 표현이 사회적으로 자연스럽고 적절한가
- 요청, 확인, 인사, 감사, 거절 표현이 상황에 맞는가
- 지나치게 공격적이거나 단절적인 표현이 없는가

### 8.3 상호작용 완성도 20점

- 질문과 응답이 자연스럽게 이어졌는가
- 필요한 경우 되묻기, 확인, 응답 확장이 있었는가
- 한 단어 반복 수준이 아니라 대화 흐름을 유지했는가

### 8.4 과제 수행도 15점

- 시나리오 훈련 목표를 실제로 수행했는가
- 필요한 핵심 발화를 놓치지 않았는가
- 대화 종료 시점까지 목표 행동을 이어갔는가

### 8.5 기본 대화 태도 10점

- 인사, 마무리, 예의 표현이 있는가
- 무응답, 반복, 과도한 이탈이 없는가
- 최소한의 성실한 대화 태도를 유지했는가

점수 해석 기준:

- `90~100`: 매우 적절함
- `80~89`: 전반적으로 적절함
- `70~79`: 기본 수행 가능, 일부 보완 필요
- `60~69`: 맥락 이해나 표현 보완 필요
- `40~59`: 시나리오 수행이 불안정함
- `1~39`: 훈련 목표 달성이 어려움

평가 출력 최소 형식:

```json
{
  "score": 84,
  "summary": "상황에 맞는 표현으로 대화를 잘 이어갔습니다.",
  "feedback": "인사와 요청 표현은 적절했고 상대 질문에도 자연스럽게 답했습니다. 다만 대화 확장성과 구체성은 조금 더 보완할 수 있습니다.",
  "subscores": {
    "contextFit": 27,
    "expression": 21,
    "interaction": 16,
    "taskCompletion": 12,
    "conversationManner": 8
  }
}
```

평가 입력 최소 구성:

- 시나리오 설명
- 시나리오 목표
- 기대 행동 또는 기대 표현
- 사용자 발화 로그
- AI 발화 로그

평가 구현 원칙:

- LLM이 총점을 임의로 찍지 않게 한다.
- 항목별 점수와 근거를 먼저 생성하고 총점은 합산한다.
- 시나리오 목표를 기준으로 판단한다.
- transcript만이 아니라 AI와 사용자 간 상호작용 전체 맥락을 본다.

추가 작업 항목:

- [ ] 사회성 대화 평가 DTO 설계
- [ ] 평가 프롬프트 초안 작성
- [ ] 항목별 점수 산출 규칙 정의
- [ ] 총점 합산 규칙 구현
- [ ] 간단 평가 문장 생성 규칙 정의
- [ ] 평가 결과 저장 스키마 설계

## 9. 구현 순서 제안

1. 사회성 음성 책임을 `training-service` 사회성 훈련 모듈 안에서 정리
2. 첫 AI 발화 포함 세션 준비 API 계약 확정
3. 시나리오별 `opening script` 데이터 모델 추가
4. 최초 1회 TTS 생성 + 캐시 방식 구현
5. Realtime 세션 컨텍스트 반영 로직 구현
6. WebSocket relay 구현
7. transcript / 로그 저장 구현
8. 사회성 대화 평가 로직 구현
9. 종료 후 요약/점수 동기화 구현

## 10. 오픈 이슈

- 첫 AI 발화를 assistant message로 넣을지, system 성격 문맥으로 넣을지 최종 결정 필요
- 첫 발화 재생 완료 전 사용자 발화가 들어오면 어떻게 처리할지 정책 필요
- OpenAI Realtime 장애 시 fallback UX 필요
- 장기적으로 사회성 훈련 외 다른 훈련에도 음성 인터랙션을 확장할지 결정 필요
