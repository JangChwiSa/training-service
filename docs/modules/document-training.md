# Document Training Module

## Purpose

Document training helps users practice reading and understanding workplace notices, instructions, forms, and simple business documents.

## Level Assignment

- The session start API receives `level` from 1 to 5.
- Users start with `currentLevel = 1` and `highestUnlockedLevel = 1`.
- The session start API rejects a requested level greater than the user's `highestUnlockedLevel`.
- The service maps the level to `LEVEL_1` through `LEVEL_5`.
- The service randomly assigns 5 active questions from the requested difficulty.
- Assigned questions are stored in `document_session_questions`.
- `document_answer_logs` stores user answers and scoring results after submission.
- Answer submission must include exactly the 5 question IDs assigned to the session.
- Completion updates `user_document_progress` with the latest score, counts, played level, and accuracy.
- If completion accuracy is at least 80%, the next level is unlocked up to level 5.
- `GET /api/trainings/document/progress` returns the current level, highest unlocked level, and latest document result for the training screen.
