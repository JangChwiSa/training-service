# Document Training Module

## Purpose

Document training helps users practice reading and understanding workplace notices, instructions, forms, and simple business documents.

## Level Assignment

- The session start API receives `level` from 1 to 5.
- The service maps the level to `LEVEL_1` through `LEVEL_5`.
- The service randomly assigns 5 active questions from the requested difficulty.
- Assigned questions are stored in `document_session_questions`.
- `document_answer_logs` stores user answers and scoring results after submission.
- Answer submission must include exactly the 5 question IDs assigned to the session.
