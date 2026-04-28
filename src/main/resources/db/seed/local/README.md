# Local Seed Data

Local seed migrations live in this directory and run only with the `local` profile.

These files may insert Training Service owned content data:

```text
social_scenarios
safety_scenarios
safety_scenes
safety_choices
focus_level_rules
document_questions
```

Do not insert user execution results here:

```text
training_sessions
training_scores
training_feedbacks
training_session_summaries
outbox_events
user_*_progress
```
