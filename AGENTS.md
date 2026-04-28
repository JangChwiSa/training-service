# Agent Instructions

## Repository Scope

This repository implements the **Training Service** only.

Training Service owns:

```text
- /api/trainings/**
- /internal/trainings/**
- training_db
- training sessions, logs, progress, scores, feedback, summaries
- TrainingCompleted event publishing
- OpenAI usage for completed training evaluation, score generation, and feedback generation
```

Training Service must not implement:

```text
- login, signup, logout, token issuing, or user profile management
- STT, TTS, low-level audio processing, or real-time voice dialogue
- report aggregation, report interpretation, or report_db writes
- frontend rendering
```

## Source of Truth

Use `docs/development/tasks.md` as the implementation task list.

Before starting any task:

```text
1. Open the task in docs/development/tasks.md.
2. Read every "참고 문서" listed for that task.
3. Follow those documents as the API, DB, architecture, and module contracts.
4. If task instructions conflict with a referenced spec, stop and explain the conflict before editing code.
```

Core documents that commonly apply:

```text
docs/product/project-plan.md
docs/architecture/training-service-architecture.md
docs/architecture/security-context.md
docs/architecture/openai-integration.md
docs/architecture/event-outbox.md
docs/api/training-api-spec.md
docs/database/training-db-spec.md
docs/development/tasks.md
docs/development/git-strategy.md
```

## Boundary Rules

Always preserve service ownership boundaries.

```text
User Service = authentication, login, signup, user profile
Voice Service = STT, TTS, real-time voice and AI dialogue
Training Service = training records, scores, feedback, progress, completion state
Report Service = report interpretation and aggregation
```

Training Service may store external `user_id` values, but it must not create physical FK relationships to `user_db`.

Training Service must not directly access `user_db` or `report_db`.

## Security Rules

External APIs must not accept `userId` from request body or query parameters.

Use the authenticated context or trusted gateway header for the current user.

For every `sessionId` based external API:

```text
- verify the session belongs to the current user
- do not expose another user's logs, scores, feedback, progress, or summaries
```

Do not commit secrets, API keys, generated credentials, local `.env` files, or production configuration values.

## Completion and Event Rules

Training completion must follow the documented flow:

```text
1. save original logs or results
2. save score
3. save feedback
4. update user progress
5. create training session summary
6. mark session completed
7. save outbox event in the same transaction
8. publish TrainingCompleted through the outbox publisher
```

Use the `outbox_events` contract in `docs/database/training-db-spec.md`.

Duplicate completion must not create duplicate score, feedback, summary, or event records.

## OpenAI Rules

Training Service may use OpenAI only for completed training evaluation, score generation, feedback generation, and adaptive feedback support.

Training Service must not use OpenAI for STT, TTS, or real-time voice dialogue.

Do not send unnecessary user profile data or sensitive personal data to OpenAI.

Tests must not call the real OpenAI API.

## Development Workflow

Before coding:

```text
1. Check the current git branch.
2. Find the active task in docs/development/tasks.md.
3. Read the task's referenced documents.
4. Confirm API and DB contracts.
5. Explain the implementation plan.
```

After coding:

```text
1. Add or update focused tests.
2. Verify service boundary rules.
3. Run the relevant checks when available.
4. Summarize changed files, test evidence, and remaining risks.
```

Follow branch, commit, and PR rules in `docs/development/git-strategy.md`.
