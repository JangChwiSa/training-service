````md
# Overall Architecture

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
user_db        training_db                 report_db
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
````

## Data Ownership

```text
user_db
- User basic information
- Disability/personality information
- Desired job

training_db
- Training sessions
- Training progress
- Dialogue logs
- AI feedback results
- Training scores
- Completion status

report_db
- Area-based competency scores
- Overall progress summary
- Job readiness score
- Strength/weakness analysis
- Overall comments
- Report snapshots
```

## Core Boundaries

```text
User Service = authentication, login, and user profile management
Voice Service = voice and AI interaction engine
Training Service = owner of training records, scores, feedback, and progress
Report Service = analysis service that interprets training results
```

## Training Service Focus

This repository focuses only on **Training Service** implementation.

Training Service is responsible for:

```text
- Training content retrieval
- Training session creation
- Training progress management
- Dialogue log storage
- AI feedback result storage
- Training score storage
- Training completion status management
- Training session summary management
- TrainingCompleted event publishing
- OpenAI API usage for training evaluation, score generation, and feedback generation
```

Training Service is not responsible for:

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

## OpenAI Responsibility Boundary

```text
Both Training Service and Voice Service may use OpenAI API.

Training Service uses OpenAI API for:
- Training evaluation
- Score generation
- Feedback generation
- Analysis of completed training logs
- Adaptive training support when required

Voice Service uses OpenAI API for:
- STT
- TTS
- Real-time voice dialogue
- Voice-based AI response generation
```

## Service Interaction Principles

```text
- API Gateway is the external entry point for backend services.
- User Service owns user identity, authentication, and profile information.
- Training Service owns training records, scores, feedback, progress, and completion state.
- Voice Service owns voice and AI interaction processing.
- Report Service interprets training results and manages report summaries.
- Training Service must not directly access user_db or report_db.
- Training Service owns training_db.
- Training Service may publish TrainingCompleted events.
- Report Service may consume TrainingCompleted events to update reports.
- Voice Service may publish VoiceFeedbackGenerated events.
```

## Event Broker

```text
TrainingCompleted
- Published when a training session is successfully completed.
- Usually published by Training Service.
- Consumed by Report Service to update report data.

VoiceFeedbackGenerated
- Published when voice or AI feedback generation is completed.
- Usually published by Voice Service when voice-related feedback processing is done.

ReportUpdated
- Published when report data is recalculated or updated.
- Usually published by Report Service.
```

## Object Storage

```text
Object Storage is used for:
- Voice files
- Attachments
- Long-term original files

Training Service may store references to files in Object Storage when training-related records need to keep file metadata or original file references.
```

## Infrastructure Concerns

```text
Monitoring
- Track service health, latency, error rate, and throughput.

Logging
- Record important application events, errors, and training processing results.

Tracing
- Trace requests across API Gateway, Training Service, Voice Service, and Report Service.

Circuit Breaker
- Protect services from repeated downstream failures.

Retry
- Retry transient failures such as network errors or temporary downstream failures.

DLQ
- Store messages that failed repeated processing.

Outbox Pattern
- Ensure database changes and event publishing are handled reliably together.
```

```

