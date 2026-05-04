# Training Service Task Plan

## 1. Purpose

??臾몄꽌??Training Service 援ы쁽 ?묒뾽???묒? ?⑥쐞??task濡??섎늻湲??꾪븳 湲곗? 臾몄꽌?대떎.

媛?task??蹂꾨룄 釉뚮옖移섏뿉???묒뾽?????덉쓣 ?뺣룄濡??섎늻硫? API, DB, ?명봽?? 肄붾뱶 ?덉쭏, ?뚯뒪?멸? ?④퍡 ?꾩꽦?섎뒗 ?쒖꽌濡?吏꾪뻾?쒕떎.

## 2. Task Rules

```text
- main 釉뚮옖移섏뿉 吏곸젒 援ы쁽?섏? ?딅뒗??
- 援ы쁽 ?묒뾽? feature/* 釉뚮옖移섎? ?ъ슜?쒕떎.
- 臾몄꽌留??섏젙?섎뒗 ?묒뾽? docs/* 釉뚮옖移섎? ?ъ슜?쒕떎.
- ??釉뚮옖移섏뿉???섎굹??紐⑹쟻留??대뒗??
- API ?먮뒗 DB 怨꾩빟 蹂寃쎌씠 ?꾩슂?섎㈃ 援ы쁽蹂대떎 癒쇱? 臾몄꽌瑜??섏젙?쒕떎.
- 媛?task???뚯뒪???먮뒗 寃利?湲곗????ы븿?댁빞 ?꾨즺濡?蹂몃떎.
- task ?쒖옉 ??愿??API/DB/architecture 臾몄꽌媛 議댁옱?섍퀬 ?쒕줈 異⑸룎?섏? ?딅뒗吏 ?뺤씤?쒕떎.
- ?꾩닔 臾몄꽌媛 ?녾굅??outbox泥섎읆 援ы쁽???꾩슂??DB 怨꾩빟???꾨씫?섏뼱 ?덉쑝硫?援ы쁽 task蹂대떎 臾몄꽌 ?뺥빀??task瑜?癒쇱? ?섑뻾?쒕떎.
```

## 2.1 Current Documentation Status

援ы쁽 ?쒖옉 ???ㅼ쓬 臾몄꽌 ?뺥빀???곹깭瑜??뺤씤?쒕떎.

```text
- AGENTS.md?먯꽌 ?붽뎄?섎뒗 docs/product/project-plan.md媛 議댁옱?쒕떎.
- event outbox ?뚯씠釉붿? docs/database/db-spec.md? docs/database/training-db-spec.md??outbox_events濡??뺤쓽?섏뼱 ?덈떎.
```

吏꾪뻾 湲곗?:

- 援ы쁽 task ?쒖옉 ??`docs/product/project-plan.md`瑜??ы븿??愿??臾몄꽌瑜?癒쇱? ?쎈뒗??
- outbox 援ы쁽 task??DB spec??`outbox_events` 怨꾩빟??湲곗??쇰줈 吏꾪뻾?쒕떎.

## 3. Phase 0 - Project Scaffold

### Task 0.1 Spring Boot ?꾨줈?앺듃 ?앹꽦

```text
Branch: feature/project-scaffold
```

李멸퀬 臾몄꽌:

- `docs/architecture/training-service-architecture.md`
- `docs/development/local-development.md`
- `docs/development/git-strategy.md`

?묒뾽:

- Spring Boot 4.0.6 ?꾨줈?앺듃 ?앹꽦
- Java 21 ?ㅼ젙
- 湲곕낯 package瑜?`com.didgo.trainingservice`濡??ㅼ젙
- Maven ?먮뒗 Gradle 鍮뚮뱶 ?뚯씪 援ъ꽦
- 湲곕낯 application entrypoint ?앹꽦
- `local`, `test` profile 遺꾨━

?꾨즺 湲곗?:

- ?좏뵆由ъ??댁뀡??濡쒖뺄?먯꽌 湲곕룞?쒕떎.
- 湲곕낯 context load test媛 ?듦낵?쒕떎.
- 鍮뚮뱶 紐낅졊???깃났?쒕떎.

### Task 0.2 湲곕낯 ?⑦궎吏 援ъ“ ?앹꽦

```text
Branch: feature/package-structure
```

李멸퀬 臾몄꽌:

- `docs/architecture/training-service-architecture.md`
- `AGENTS.md`

?묒뾽:

- `common`, `config`, `training`, `event`, `external`, `support` ?⑦궎吏 ?앹꽦
- `training.session`, `training.social`, `training.safety`, `training.focus`, `training.document`, `training.progress`, `training.score`, `training.feedback`, `training.summary` ?섏쐞 援ъ“ ?앹꽦
- 鍮??⑦궎吏留??먯? ?딄퀬 理쒖냼 marker class ?먮뒗 ?ㅼ젣 珥덇린 援ъ꽦 ?대옒?ㅻ줈 ?좎?

?꾨즺 湲곗?:

- 臾몄꽌???⑦궎吏 援ъ“? ?ㅼ젣 肄붾뱶 援ъ“媛 ?쇱튂?쒕떎.
- 遺덊븘?뷀븳 broad utility ?⑦궎吏媛 ?녿떎.

## 4. Phase 1 - Local Infrastructure

### Task 1.1 Docker 濡쒖뺄 ?ㅽ뻾 ?섍꼍 援ъ꽦

```text
Branch: feature/local-docker
```

李멸퀬 臾몄꽌:

- `docs/development/local-development.md`
- `docs/architecture/training-service-architecture.md`
- `docs/architecture/overall-architecture.md`

?묒뾽:

- `Dockerfile` ?묒꽦
- `docker-compose.yml` ?묒꽦
- Training Service 而⑦뀒?대꼫? MySQL 而⑦뀒?대꼫 援ъ꽦
- MySQL database瑜?`training_db`濡??ㅼ젙
- MySQL healthcheck 援ъ꽦

?꾨즺 湲곗?:

- `docker compose up --build`濡??쒕퉬?ㅼ? MySQL??湲곕룞?쒕떎.
- ?좏뵆由ъ??댁뀡??MySQL ?곌껐???뺤씤?????덈떎.
- Voice Service, User Service, Report Service 而⑦뀒?대꼫瑜??ы븿?섏? ?딅뒗??

### Task 1.2 ?섍꼍 蹂???덉떆 ?뚯씪 ?묒꽦

```text
Branch: chore/add-env-example
```

李멸퀬 臾몄꽌:

- `docs/development/local-development.md`
- `docs/architecture/security-context.md`
- `docs/architecture/openai-integration.md`
- `docs/architecture/event-outbox.md`

?묒뾽:

- `.env.example` ?묒꽦
- DB ?묒냽 ?뺣낫, OpenAI ?ㅼ젙, trusted user header, outbox/event ?ㅼ젙 ?덉떆 異붽?
- ?ㅼ젣 secret 媛믪? ?ы븿?섏? ?딆쓬

?꾨즺 湲곗?:

- `.env.example`留뚯쑝濡?濡쒖뺄 ?ㅼ젙 ??ぉ???뚯븙?????덈떎.
- ?ㅼ젣 API key, ?댁쁺 password媛 ??μ냼???ы븿?섏? ?딅뒗??

## 5. Phase 2 - Database Migration

### Task 2.1 Migration ?꾧뎄 ?ㅼ젙

```text
Branch: feature/database-migration-setup
```

李멸퀬 臾몄꽌:

- `docs/database/db-spec.md`
- `docs/database/training-db-spec.md`
- `docs/development/local-development.md`

?묒뾽:

- Flyway ?먮뒗 Liquibase ?ㅼ젙
- test profile?먯꽌 migration ?ㅽ뻾?섎룄濡?援ъ꽦

?꾨즺 湲곗?:

- migration ?꾧뎄媛 local/test profile?먯꽌 ?ㅽ뻾 媛?ν븯??
- migration ?뚯씪 ?꾩튂, naming rule, ?ㅽ뻾 profile??臾몄꽌?붾릺???덈떎.
- ?ㅼ젣 schema 寃利앹? Task 2.2 ?댄썑 migration smoke test?먯꽌 ?섑뻾?쒕떎.

### Task 2.2 Integration test baseline for migration

```text
Branch: test/migration-baseline
```

李멸퀬 臾몄꽌:

- `docs/database/db-spec.md`
- `docs/database/training-db-spec.md`
- `docs/development/local-development.md`

?묒뾽:

- Testcontainers MySQL 援ъ꽦
- test profile?먯꽌 MySQL 湲곕컲 migration 寃利앹씠 媛?ν븯?꾨줉 ?ㅼ젙
- 鍮?DB?먯꽌 migration???앷퉴吏 ?ㅽ뻾?섎뒗 smoke test ?묒꽦

?꾨즺 湲곗?:

- 濡쒖뺄 ?뚯뒪?몄뿉??MySQL 而⑦뀒?대꼫 湲곕컲 migration 寃利앹씠 ?듦낵?쒕떎.
- migration ?ㅽ뙣 ???뚯뒪?멸? ?ㅽ뙣?쒕떎.
- ?댄썑 schema task媛 ???뚯뒪??湲곕컲???ъ궗?⑺븷 ???덈떎.

### Task 2.3 Training DB core schema ?묒꽦

```text
Branch: feature/training-core-schema
```

李멸퀬 臾몄꽌:

- `docs/database/db-spec.md`
- `docs/database/training-db-spec.md`
- `docs/architecture/event-outbox.md`
- `docs/api/training-api-spec.md`

?묒뾽:

- `training_sessions`
- `training_scores`
- `training_feedbacks`
- `training_session_summaries`
- event outbox ?뚯씠釉?

?꾨즺 湲곗?:

- outbox ?뚯씠釉붿? DB spec??而щ읆, ?곹깭媛? retry/DLQ 愿???꾨뱶, ?몃뜳?ㅺ? 癒쇱? ?뺤쓽?섏뼱 ?덈떎.
- `training_session_summaries`??紐⑸줉 API ?ㅻ깄??議고쉶???꾩슂???꾨뱶瑜??ы븿?쒕떎.
- `user_id`???몃? ?ъ슜??ID 李몄“媛믪쑝濡???ν븯怨?`users.user_id` 臾쇰━ FK瑜?留뚮뱾吏 ?딅뒗??
- `session_id` 湲곕컲 FK? unique ?쒖빟??臾몄꽌? ?쇱튂?쒕떎.

### Task 2.4 Training module schema ?묒꽦

```text
Branch: feature/training-module-schema
```

李멸퀬 臾몄꽌:

- `docs/database/db-spec.md`
- `docs/database/training-db-spec.md`
- `docs/modules/social-training.md`
- `docs/modules/safety-training.md`
- `docs/modules/focus-training.md`
- `docs/modules/document-training.md`

?묒뾽:

- social tables
- safety tables
- focus tables
- document tables
- user progress tables

?꾨즺 湲곗?:

- 紐⑤뱺 Training Service ?뚯쑀 ?뚯씠釉붿씠 migration???ы븿?쒕떎.
- 肄섑뀗痢??뚯씠釉붿뿉??`user_id`瑜???ν븯吏 ?딅뒗??
- ?ъ슜???섑뻾 寃곌낵/progress/summary?먮뒗 `user_id`瑜???ν븳??

### Task 2.5 Local seed and fixture data ?묒꽦

```text
Branch: feature/local-seed-data
```

李멸퀬 臾몄꽌:

- `docs/database/training-db-spec.md`
- `docs/api/training-api-spec.md`
- `docs/modules/social-training.md`
- `docs/modules/safety-training.md`
- `docs/modules/focus-training.md`
- `docs/modules/document-training.md`

?묒뾽:

- social scenario seed ?곗씠???묒꽦
- safety scenario/scene/choice seed ?곗씠???묒꽦
- focus level rule seed ?곗씠???묒꽦
- document question seed ?곗씠???묒꽦
- ?뚯뒪?몄슜 fixture? local seed??梨낆엫 遺꾨━

?꾨즺 湲곗?:

- 濡쒖뺄 ?섍꼍?먯꽌 媛??덈젴 ?쒖옉 API媛 理쒖냼 1媛??댁긽???쒖꽦 肄섑뀗痢좊줈 ?숈옉?????덈떎.
- seed ?곗씠?곕뒗 Training Service ?뚯쑀 肄섑뀗痢??뚯씠釉붾쭔 ??곸쑝濡??쒕떎.
- user progress, session, score, feedback 媛숈? ?ъ슜???섑뻾 寃곌낵??seed濡?留뚮뱾吏 ?딅뒗??

## 6. Phase 3 - Common Foundation

### Task 3.1 怨듯넻 ?묐떟怨??덉쇅 泥섎━

```text
Branch: feature/common-api-response
```

李멸퀬 臾몄꽌:

- `docs/api/api-spec.md`
- `docs/api/training-api-spec.md`
- `docs/architecture/training-service-architecture.md`

?묒뾽:

- 怨듯넻 ?묐떟 ?щ㎎ 援ы쁽
- 怨듯넻 ?ㅻ쪟 ?묐떟 援ы쁽
- global exception handler 援ы쁽
- validation error ?묐떟 援ы쁽

?꾨즺 湲곗?:

- API spec??怨듯넻 ?묐떟/?ㅻ쪟 援ъ“? ?쇱튂?쒕떎.
- validation ?ㅽ뙣 ?뚯뒪?멸? ?덈떎.

### Task 3.2 Security context 援ы쁽

```text
Branch: feature/security-context
```

李멸퀬 臾몄꽌:

- `docs/architecture/security-context.md`
- `docs/api/api-spec.md`
- `docs/api/training-api-spec.md`
- `AGENTS.md`

?묒뾽:

- trusted header `X-User-Id` 湲곕컲 current user resolver 援ы쁽
- ?몃? API?먯꽌 request body/query `userId`瑜??ъ슜?섏? ?딅룄濡?controller contract 援ъ꽦
- ?대? API??path `userId` ?ъ슜 寃쎄퀎 遺꾨━

?꾨즺 湲곗?:

- ?몃? API??current user context?먯꽌留?userId瑜??삳뒗??
- `X-User-Id` ?꾨씫/?섎せ??媛??뚯뒪?멸? ?덈떎.
- 濡쒓렇?? ?뚯썝媛?? ?좏겙 諛쒓툒 濡쒖쭅???녿떎.

### Task 3.3 Session ownership validator 援ы쁽

```text
Branch: feature/session-ownership-validation
```

李멸퀬 臾몄꽌:

- `docs/architecture/security-context.md`
- `docs/api/training-api-spec.md`
- `docs/database/training-db-spec.md`
- `AGENTS.md`

?묒뾽:

- `sessionId`? current `userId` ?뚯쑀沅?寃利?怨듯넻 ?쒕퉬??援ы쁽
- ?곸꽭 議고쉶, ?ㅼ쓬 ?λ㈃, ?꾨즺 泥섎━?먯꽌 ?ъ궗??媛?ν븯寃?援ъ꽦

?꾨즺 湲곗?:

- ?ㅻⅨ ?ъ슜?먯쓽 session ?묎렐? ?ㅽ뙣?쒕떎.
- not found? forbidden ?뺤콉???뚯뒪?몃줈 怨좎젙?쒕떎.

## 7. Phase 4 - Session and Query Core

### Task 4.1 Training session core 援ы쁽

```text
Branch: feature/training-session-core
```

李멸퀬 臾몄꽌:

- `docs/database/training-db-spec.md`
- `docs/api/training-api-spec.md`
- `docs/architecture/training-service-architecture.md`

?묒뾽:

- `TrainingSession` entity/repository/service 援ы쁽
- training type, status, current step, started/ended time 泥섎━
- session ?앹꽦 怨듯넻 濡쒖쭅 援ы쁽

?꾨즺 湲곗?:

- SOCIAL, SAFETY, FOCUS, DOCUMENT 怨듯넻 ?몄뀡 ?앹꽦???ъ궗??媛?ν븯??
- status ?꾩씠 ?뚯뒪?멸? ?덈떎.

### Task 4.2 Progress summary API 援ы쁽

```text
Branch: feature/training-progress-api
```

李멸퀬 臾몄꽌:

- `docs/api/api-spec.md`
- `docs/api/training-api-spec.md`
- `docs/database/training-db-spec.md`
- `docs/architecture/security-context.md`

?묒뾽:

- `GET /api/trainings/progress?type={trainingType}`
- social/safety/document/focus progress 議고쉶

?꾨즺 湲곗?:

- current user 湲곗??쇰줈留?議고쉶?쒕떎.
- ?곗씠???놁쓬 湲곕낯 ?묐떟 ?뺤콉???뚯뒪?몃줈 怨좎젙?쒕떎.

### Task 4.3 Training session list API 援ы쁽

```text
Branch: feature/training-session-list-api
```

李멸퀬 臾몄꽌:

- `docs/api/api-spec.md`
- `docs/api/training-api-spec.md`
- `docs/database/db-spec.md`
- `docs/database/training-db-spec.md`

?묒뾽:

- `GET /api/trainings/sessions`
- `training_session_summaries`留?議고쉶
- SAFETY category ?꾪꽣
- `completed_at DESC` ?뺣젹
- paging 泥섎━

?꾨즺 湲곗?:

- 紐⑸줉 API?먯꽌 ?먮낯 濡쒓렇, score, feedback ?뚯씠釉붿쓣 議고쉶?섏? ?딅뒗??
- SOCIAL, SAFETY, DOCUMENT, FOCUS ?묐떟 ?뚯뒪?멸? ?덈떎.

### Task 4.4 Internal training query API 援ы쁽

```text
Branch: feature/internal-training-query-api
```

李멸퀬 臾몄꽌:

- `docs/api/api-spec.md`
- `docs/api/training-api-spec.md`
- `docs/architecture/security-context.md`
- `docs/database/training-db-spec.md`

?묒뾽:

- `GET /internal/trainings/users/{userId}/summary`
- `GET /internal/trainings/users/{userId}/latest-results`
- ?대? API ?몄텧??寃쎄퀎? ?몃? API ?몄텧 諛⑹? ?ㅼ젙

?꾨즺 湲곗?:

- summary API??progress ?뚯씠釉붾쭔 議고쉶?쒕떎.
- latest-results API??completed session, score, feedback 湲곗??쇰줈 ?묐떟?쒕떎.
- ?몃? `/api/trainings/**` API? ?щ━ ?대? API?먯꽌留?path `userId`瑜??덉슜?쒕떎.
- Report Service DB?먮뒗 吏곸젒 ?묎렐?섏? ?딅뒗??

## 8. Phase 5 - Training Modules

### Task 5.1 Social training API 援ы쁽

```text
Branch: feature/social-training-api
```

李멸퀬 臾몄꽌:

- `docs/api/api-spec.md`
- `docs/api/training-api-spec.md`
- `docs/database/training-db-spec.md`
- `docs/modules/social-training.md`
- `docs/architecture/sequence-diagrams.md`

?묒뾽:

- job type ?좏깮 API
- social scenario 紐⑸줉/?곸꽭 議고쉶
- social session ?쒖옉
- social detail 議고쉶

?꾨즺 湲곗?:

- `training_sessions.sub_type = jobType` ???湲곗????곕Ⅸ??
- scenario 議고쉶??active content 湲곗??쇰줈 ?숈옉?쒕떎.
- detail 議고쉶??session ownership??寃利앺븳??

### Task 5.2 Safety training API 援ы쁽

```text
Branch: feature/safety-training-api
```

李멸퀬 臾몄꽌:

- `docs/api/api-spec.md`
- `docs/api/training-api-spec.md`
- `docs/database/training-db-spec.md`
- `docs/modules/safety-training.md`
- `docs/architecture/sequence-diagrams.md`

?묒뾽:

- safety scenario 紐⑸줉 議고쉶
- safety session ?쒖옉 諛?泥??λ㈃ 議고쉶
- next scene 泥섎━
- safety detail 議고쉶

?꾨즺 湲곗?:

- ?좏깮 ?대젰 ??κ낵 current step 媛깆떊???숈옉?쒕떎.
- session ownership 寃利??뚯뒪?멸? ?덈떎.
- category ?꾪꽣媛 ?숈옉?쒕떎.

### Task 5.3 Focus training API 援ы쁽

```text
Branch: feature/focus-training-api
```

李멸퀬 臾몄꽌:

- `docs/api/api-spec.md`
- `docs/api/training-api-spec.md`
- `docs/database/training-db-spec.md`
- `docs/modules/focus-training.md`
- `docs/architecture/sequence-diagrams.md`

?묒뾽:

- focus progress 議고쉶
- focus session ?쒖옉
- `POST /api/trainings/focus/sessions` ?덉뿉??focus commands ?앹꽦 ???묐떟?쇰줈 諛섑솚
- focus reaction logs???꾨즺 API?먯꽌 ?쇨큵 ?쒖텧?섎룄濡?DTO/寃利?湲곗? 以鍮?

?꾨즺 湲곗?:

- level? `training_sessions.sub_type`????ν븳??
- focus detail API??留뚮뱾吏 ?딅뒗??
- 蹂꾨룄 focus command 議고쉶 API瑜?留뚮뱾吏 ?딅뒗??
- command/reaction 湲곕낯 ?쒖빟 ?뚯뒪?멸? ?덈떎.

### Task 5.4 Document training API 援ы쁽

```text
Branch: feature/document-training-api
```

李멸퀬 臾몄꽌:

- `docs/api/api-spec.md`
- `docs/api/training-api-spec.md`
- `docs/database/training-db-spec.md`
- `docs/modules/document-training.md`
- `docs/architecture/sequence-diagrams.md`

?묒뾽:

- document session ?쒖옉
- question ?쒓났
- `POST /api/trainings/document/sessions/{sessionId}/answers` ?붿껌/?묐떟 DTO? 寃利?湲곗? 以鍮?
- document detail 議고쉶

?꾨즺 湲곗?:

- question蹂?以묐났 ?듬? ?뺤콉???뚯뒪?몃줈 怨좎젙?쒕떎.
- detail 議고쉶??session ownership??寃利앺븳??
- ?ㅼ젣 ?듬? ??? 梨꾩젏, ?꾨즺 泥섎━??Module completion task?먯꽌 援ы쁽?쒕떎.
- document detail API???꾨즺 ?곗씠?곌? ?꾩슂??議고쉶?대?濡?Phase 7 completion flow ?댄썑 理쒖쥌 ?묐떟 寃利앹쓣 ?꾨즺?쒕떎.

## 9. Phase 6 - OpenAI Evaluation Boundary

### Task 6.1 OpenAI adapter boundary 援ы쁽

```text
Branch: feature/openai-adapter
```

李멸퀬 臾몄꽌:

- `docs/architecture/openai-integration.md`
- `docs/architecture/training-service-architecture.md`
- `docs/api/training-api-spec.md`
- `AGENTS.md`

?묒뾽:

- OpenAI adapter interface
- local fake adapter
- timeout ?ㅼ젙
- ?붿껌/?묐떟 DTO
- evaluation result瑜?score/feedback ???紐⑤뜽濡?蹂?섑븯???대? DTO

?꾨즺 湲곗?:

- ?뚯뒪?몄뿉???ㅼ젣 OpenAI API瑜??몄텧?섏? ?딅뒗??
- OpenAI ?먮Ц ?묐떟???몃? API濡?洹몃?濡??몄텧?섏? ?딅뒗??
- completion flow??adapter interface留??섏〈?????덈떎.

### Task 6.2 AI evaluation integration 援ы쁽

```text
Branch: feature/openai-training-evaluation
```

李멸퀬 臾몄꽌:

- `docs/architecture/openai-integration.md`
- `docs/api/training-api-spec.md`
- `docs/database/training-db-spec.md`
- `docs/modules/social-training.md`
- `docs/modules/safety-training.md`
- `docs/modules/focus-training.md`
- `docs/modules/document-training.md`

?묒뾽:

- social dialogue evaluation? OpenAI adapter瑜??ъ슜?쒕떎.
- safety, focus, document??deterministic scoring??湲곕낯?쇰줈 ?섍퀬, adaptive feedback???꾩슂??寃쎌슦?먮쭔 OpenAI adapter瑜??ъ슜?쒕떎.
- training score generation
- feedback generation
- raw metrics ???
- timeout/retry/fallback ?뺤콉 援ы쁽

?꾨즺 湲곗?:

- ?ㅽ뙣/timeout/fallback ?뺤콉???뚯뒪?몃줈 怨좎젙?쒕떎.
- 媛쒖씤?뺣낫? 遺덊븘?뷀븳 user profile??OpenAI ?붿껌???ы븿?섏? ?딅뒗??
- fallback???ъ슜??寃쎌슦 feedback_source ?먮뒗 raw_metrics_json??洹쇨굅瑜??④릿??
- 紐⑤뱢蹂?AI ?ъ슜 ?щ?媛 ?뚯뒪??double ?먮뒗 ?ㅼ젙?쇰줈 遺꾨━?섏뼱 ?뚯뒪?몄뿉???ㅼ젣 OpenAI API瑜??몄텧?섏? ?딅뒗??

## 10. Phase 7 - Completion Flow

### Task 7.1 Completion transaction core 援ы쁽

```text
Branch: feature/training-completion-core
```

李멸퀬 臾몄꽌:

- `docs/architecture/event-outbox.md`
- `docs/architecture/openai-integration.md`
- `docs/api/training-api-spec.md`
- `docs/database/training-db-spec.md`
- `AGENTS.md`

?묒뾽:

- ?꾨즺 泥섎━ 怨듯넻 ?몃옖??뀡 援ъ꽦
- ?먮낯 濡쒓렇/寃곌낵 ???
- score ???
- feedback ???
- progress 媛깆떊
- summary ?앹꽦
- session completed 泥섎━
- 媛숈? ?몃옖??뀡 ?덉뿉??outbox event ???

?꾨즺 湲곗?:

- ?꾨즺 泥섎━ ?쒖꽌媛 event-outbox 臾몄꽌? ?쇱튂?쒕떎.
- ?꾨즺 ?곗씠?곗? outbox event媛 媛숈? ?몃옖??뀡?먯꽌 ??λ맂??
- 以묎컙 ?ㅽ뙣 ???몃옖??뀡 rollback ?뚯뒪?멸? ?덈떎.
- duplicate completion ?뚯뒪?멸? ?덈떎.
- ?대? ?꾨즺???몄뀡? score, feedback, summary, event瑜?以묐났 ?앹꽦?섏? ?딅뒗??

### Task 7.2 Module completion 援ы쁽

```text
Branch: feature/training-module-completion
```

李멸퀬 臾몄꽌:

- `docs/api/api-spec.md`
- `docs/api/training-api-spec.md`
- `docs/database/training-db-spec.md`
- `docs/architecture/event-outbox.md`
- `docs/architecture/sequence-diagrams.md`

?묒뾽:

- social complete
- safety complete
- focus complete
- document answers submit and complete

?꾨즺 湲곗?:

- 媛??꾨즺 API媛 score, feedback, progress, summary, outbox event瑜??앹꽦?쒕떎.
- ?대? ?꾨즺???몄뀡?????以묐났 ?붿껌 ?뺤콉???뚯뒪?몃줈 怨좎젙?쒕떎.

## 11. Phase 8 - Event Outbox Publisher

### Task 8.1 Event publisher 援ы쁽

```text
Branch: feature/training-completed-publisher
```

李멸퀬 臾몄꽌:

- `docs/architecture/event-outbox.md`
- `docs/api/training-api-spec.md`
- `docs/database/training-db-spec.md`
- `docs/architecture/overall-architecture.md`

?묒뾽:

- outbox publisher
- publish success ?곹깭 蹂寃?
- retry ????곹깭 ?좎?
- DLQ ?꾪솚 湲곗? 援ы쁽 以鍮?

?꾨즺 湲곗?:

- completion flow媛 ??ν븳 outbox event瑜?諛쒗뻾 ??곸쑝濡?議고쉶?쒕떎.
- publish ?깃났/?ㅽ뙣 ?뚯뒪?멸? ?덈떎.
- eventId媛 payload???ы븿?쒕떎.
- Report Service DB??吏곸젒 ?묎렐?섏? ?딅뒗??

## 12. Phase 9 - Infra Hardening

### Task 9.1 Health check and actuator

```text
Branch: feature/health-check
```

李멸퀬 臾몄꽌:

- `docs/development/local-development.md`
- `docs/architecture/training-service-architecture.md`

?묒뾽:

- actuator health ?ㅼ젙
- DB health ?뺤씤
- local docker healthcheck? ?곌껐

?꾨즺 湲곗?:

- `/actuator/health`媛 DB ?곹깭瑜??ы븿?쒕떎.
- docker compose ?섍꼍?먯꽌 healthcheck媛 ?듦낵?쒕떎.

### Task 9.2 Logging and tracing baseline

```text
Branch: feature/logging-tracing-baseline
```

李멸퀬 臾몄꽌:

- `docs/architecture/overall-architecture.md`
- `docs/architecture/security-context.md`
- `docs/architecture/openai-integration.md`
- `docs/architecture/event-outbox.md`

?묒뾽:

- request logging 湲곗? ?ㅼ젙
- error logging 湲곗? ?ㅼ젙
- event publish log 湲곗? ?ㅼ젙
- trace id ?먮뒗 correlation id ?섏슜 以鍮?

?꾨즺 湲곗?:

- 誘쇨컧 ?뺣낫? OpenAI API key瑜?濡쒓렇???④린吏 ?딅뒗??
- sessionId/userId 湲곕컲 ?댁쁺 異붿쟻??媛?ν븯??

### Task 9.3 Swagger UI and OpenAPI documentation

```text
Branch: feature/swagger-ui
```

Reference documents:

- `docs/api/api-spec.md`
- `docs/api/training-api-spec.md`
- `docs/architecture/security-context.md`
- `AGENTS.md`

Work:

- Add Springdoc/OpenAPI dependency for Swagger UI.
- Expose Swagger UI for Training Service APIs under `/api/trainings/**` and `/internal/trainings/**`.
- Document trusted user header handling such as `X-User-Id` without accepting `userId` in request body or query parameters.
- Group external Training APIs and internal Training APIs clearly.
- Ensure API response examples use the common success/error response envelope.
- Keep Swagger/OpenAPI metadata within Training Service boundaries only.

Completion criteria:

- Swagger UI is accessible in local development.
- OpenAPI docs include Training Service-owned APIs only.
- Security boundary rules are visible in API documentation.
- Existing API contracts and database schemas are not changed.
- Application context and API contract tests pass.

## 13. Phase 10 - Test and Quality Gate

### Task 10.1 Repository and service integration tests

```text
Branch: test/integration-baseline
```

李멸퀬 臾몄꽌:

- `docs/database/training-db-spec.md`
- `docs/api/training-api-spec.md`
- `docs/architecture/event-outbox.md`
- `AGENTS.md`

?묒뾽:

- Phase 2??Testcontainers MySQL 湲곕컲 ?ъ궗??
- repository integration test
- 二쇱슂 service transaction integration test

?꾨즺 湲곗?:

- CI ?먮뒗 濡쒖뺄 verify?먯꽌 MySQL 湲곕컲 repository/service ?뚯뒪?멸? ?듦낵?쒕떎.
- migration 寃利앹? Phase 2 baseline怨?以묐났 援ы쁽?섏? ?딅뒗??

### Task 10.2 API contract tests

```text
Branch: test/api-contract
```

李멸퀬 臾몄꽌:

- `docs/api/api-spec.md`
- `docs/api/training-api-spec.md`
- `docs/architecture/security-context.md`
- `AGENTS.md`

?묒뾽:

- progress API contract test
- session list API contract test
- detail API contract test
- completion API contract test

?꾨즺 湲곗?:

- API spec??request/response? ?뚯뒪?멸? ?쇱튂?쒕떎.
- userId body/query ?ъ슜???놁쓬???뚯뒪?명븳??

### Task 10.3 Boundary and regression tests

```text
Branch: test/boundary-regression
```

李멸퀬 臾몄꽌:

- `docs/architecture/security-context.md`
- `docs/architecture/openai-integration.md`
- `docs/architecture/event-outbox.md`
- `docs/api/training-api-spec.md`
- `AGENTS.md`

?묒뾽:

- session ownership regression test
- duplicate completion regression test
- invalid session access test
- OpenAI failure boundary test
- outbox retry boundary test

?꾨즺 湲곗?:

- AGENTS.md Testing Guidelines???듭떖 ??ぉ??紐⑤몢 而ㅻ쾭?쒕떎.

## 14. Implementation Order

沅뚯옣 援ы쁽 ?쒖꽌???ㅼ쓬怨?媛숇떎.

```text
0. Documentation gaps ?뺣━
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

Phase 6 ?댄썑遺?곕뒗 OpenAI integration, completion flow, event outbox媛 ?쒕줈 ?곹뼢??二쇰?濡?API/DB 臾몄꽌? ?뚯뒪?몃? ?④퍡 ?뺤씤?쒕떎.
