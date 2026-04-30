# Repository Guidelines

## Role Scope

This repository is for implementing the **Training Service** only.

The Training Service is responsible for:

```text
- Training content retrieval
- Training session creation
- Training progress management
- Training logs
- Training scores
- Training feedback
- Training completion status
- Training session summaries
- TrainingCompleted event publishing
- OpenAI API usage for training evaluation, score generation, and feedback generation
```

The Training Service is not responsible for:

```text
- User authentication
- Login
- Signup
- User profile management
- Low-level voice processing
- STT
- TTS
- Real-time voice interaction
- Report aggregation
- Report interpretation
- Frontend rendering
```

## System Context

The whole system consists of:

```text
User
  |
Nginx
  |
Frontend Server
  |
Nginx
  |
API Gateway
  |
----------------------------------------------
|              |                 |          |
User Service   Training Service  Voice      Report
|              |                 Service    Service
               |                  |
               OpenAI API         OpenAI API

Object Storage
- Voice files
- Attachments
- Long-term original files

Event Broker
- TrainingCompleted
- VoiceFeedbackGenerated
- ReportUpdated

Infra
- Monitoring
- Logging
- Tracing
- Circuit Breaker
- Retry
- DLQ
- Outbox Pattern
```

Service data ownership:

```text
User Service owns user_db.
Training Service owns training_db.
Voice Service owns voice and AI interaction responsibilities.
```

Both **Training Service** and **Voice Service** may use OpenAI API.

## Training Service Boundaries

Training Service owns `training_db`.

Training Service must not directly access:

```text
- user_db
- Frontend state
- Authentication credentials
```

Training Service may call or use:

```text
- OpenAI API for training evaluation and feedback generation
- Voice Service when voice-related processing is required
- Event Broker to publish training-related events
- Object Storage when training-related files or long-term original files must be referenced or stored
```

Training Service must not implement:

```text
- Login
- Signup
- User profile management
- STT/TTS implementation
- Report summary interpretation owned by Report Service
```

## OpenAI Responsibility Boundary

Both Training Service and Voice Service may use OpenAI API, but their responsibilities are different.

Voice Service uses OpenAI API for:

```text
- STT
- TTS
- Real-time voice dialogue
- Voice-based AI response generation
```

Training Service uses OpenAI API for:

```text
- Training evaluation
- Score generation
- Feedback generation
- Analysis of completed training logs
- Adaptive training support when required
```

Training Service may analyze:

```text
- Completed dialogue logs
- Selected choices
- Document answers
- Focus reaction results
```

Training Service should not implement low-level voice processing such as STT/TTS.

Voice Service should not own:

```text
- Training sessions
- Training scores
- Training progress
- Training summaries
- Training completion state
```

## User Identity Rules

External APIs must not receive `userId` directly from request body or query parameters.

Training Service may receive `userId` from:

```text
- API Gateway after token validation
- Authenticated context
- Trusted internal request headers
```

Training Service must always verify that a `sessionId` belongs to the current `userId`.

## Required Documents

Before implementing or modifying Training Service code, read the relevant documents.

Core documents:

```text
docs/product/project-plan.md
docs/architecture/overall-architecture.md
docs/architecture/sequence-diagrams.md
docs/api/api-spec.md
docs/database/db-spec.md
```

Training Service-specific documents:

```text
docs/api/training-api-spec.md
docs/database/training-db-spec.md
```

Module-specific documents:

```text
docs/modules/social-training.md
docs/modules/safety-training.md
docs/modules/focus-training.md
docs/modules/document-training.md
```

## Training Service Core Domains

Training Service includes the following domains:

```text
TrainingSession
SocialScenario
SocialDialogLog
UserSocialProgress
SafetyScenario
SafetyScene
SafetyChoice
SafetyActionLog
UserSafetyProgress
FocusLevelRule
FocusCommand
FocusReactionLog
UserFocusProgress
DocumentQuestion
DocumentAnswerLog
UserDocumentProgress
TrainingScore
TrainingFeedback
TrainingSessionSummary
```

## API Ownership

Training Service owns APIs under:

```text
/api/trainings/**
/internal/trainings/**
```

Training Service does not own:

```text
/api/auth/**
/api/users/**
/api/voice/**
/api/reports/**
```

## Database Ownership

Training Service owns `training_db`.

Training Service tables include:

```text
training_sessions
social_scenarios
social_dialog_logs
user_social_progress
safety_scenarios
safety_scenes
safety_choices
safety_action_logs
user_safety_progress
focus_level_rules
focus_commands
focus_reaction_logs
user_focus_progress
document_questions
document_answer_logs
user_document_progress
training_scores
training_feedbacks
training_session_summaries
```

## Event Rules

Training Service publishes:

```text
TrainingCompleted
```

Training Service may consume or react to:

```text
VoiceFeedbackGenerated
```

Report Service may consume:

```text
TrainingCompleted
```

When training completion is successful, Training Service should:

```text
1. Save original logs or results.
2. Save score.
3. Save feedback.
4. Update user progress.
5. Create training session summary.
6. Mark session as completed.
7. Publish TrainingCompleted event.
```

If event publishing fails, use retry, DLQ, or outbox pattern when available.

## Implementation Rules

Follow these rules when implementing Training Service:

```text
- Do not implement User Service logic inside Training Service.
- Do not implement Voice Service low-level audio logic inside Training Service.
- Do not implement Report Service aggregation logic inside Training Service.
- Training Service may store final dialogue logs and AI feedback results.
- Training Service may call OpenAI API for completed training evaluation.
- Training Service must verify that a sessionId belongs to the current userId.
- Training Service must update progress tables after training completion.
- Training Service must create or update training_session_summaries after training completion.
- Training Service must publish TrainingCompleted event after successful training completion.
- Do not silently change API contracts.
- Do not silently change database schemas.
- If a requested change conflicts with the API spec or DB spec, explain the conflict first.
```

## Development Workflow

Before coding:

```text
1. Read the relevant specification documents.
2. Identify the affected Training Service domain.
3. Check the API contract.
4. Check the DB schema.
5. Explain the implementation plan.
6. Then implement.
```

After coding:

```text
1. Add or update tests.
2. Verify service boundary rules.
3. Summarize changed files.
4. Mention any remaining risks or TODOs.
```

## Testing Guidelines

Training Service tests should cover:

```text
- Session creation
- Session ownership validation
- Training completion
- Score saving
- Feedback saving
- Progress update
- Training session summary creation
- OpenAI feedback generation integration boundary
- Event publishing
- Invalid session access
- Duplicate completion handling
- Not found cases
- Validation errors
```

## Git Strategy

Git branching, commit, and Pull Request rules are documented in:

```text
docs/development/git-strategy.md
```

## Security Rules

```text
- Never trust userId from the client request body.
- Always derive user identity from the authenticated context or trusted gateway header.
- Validate that the requested sessionId belongs to the current user.
- Do not expose another user's training logs.
- Do not store secrets or API keys in the repository.
```

## Coding Style & Naming Conventions

Follow the conventions of the selected language and framework.

Prefer:

```text
- Clear package/module boundaries
- Domain-focused class names
- Small, focused files
- Explicit service responsibilities
- Descriptive method names
- Consistent DTO naming
- Clear test names that describe behavior
```

Avoid:

```text
- Broad utility classes
- Mixing controller logic with business logic
- Direct database access from unrelated layers
- Cross-service database access
- Hidden side effects
```

## Commit & Pull Request Guidelines

Use concise, imperative commit messages.

Examples:

```text
Add training session creation API
Implement social training completion flow
Add training progress summary query
Fix session ownership validation
```

Pull requests should include:

```text
- Short summary
- Main changed files
- Test evidence
- Related API or DB spec references
- Known limitations or TODOs
```

## Security & Configuration Tips

Do not commit:

```text
- Secrets
- API keys
- Generated credentials
- Local environment files
- Production configuration values
```

Use safe examples such as:

```text
.env.example
application-local.example.yml
```

Required environment variables should be documented near the code that consumes them and in contributor-facing docs.

## Important Reminder

This repository is focused on **Training Service**.

When in doubt, preserve these boundaries:

```text
User Service = authentication, login, and user profile management
Voice Service = voice and AI interaction engine
Training Service = owner of training records, scores, feedback, and progress
Report Service = analysis service that interprets training results
```

