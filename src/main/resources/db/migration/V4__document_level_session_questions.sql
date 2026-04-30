UPDATE document_questions
SET difficulty = 'LEVEL_1'
WHERE difficulty IS NULL
   OR difficulty NOT IN ('LEVEL_1', 'LEVEL_2', 'LEVEL_3', 'LEVEL_4', 'LEVEL_5');

ALTER TABLE document_questions
    MODIFY difficulty VARCHAR(50) NOT NULL;

ALTER TABLE document_questions
    ADD CONSTRAINT ck_document_questions_difficulty
        CHECK (difficulty IN ('LEVEL_1', 'LEVEL_2', 'LEVEL_3', 'LEVEL_4', 'LEVEL_5'));

CREATE INDEX idx_document_questions_difficulty_active
    ON document_questions (difficulty, is_active);

CREATE TABLE document_session_questions (
    session_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    display_order INT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_document_session_questions PRIMARY KEY (session_id, question_id),
    CONSTRAINT uq_document_session_questions_order UNIQUE (session_id, display_order),
    CONSTRAINT fk_document_session_questions_session FOREIGN KEY (session_id) REFERENCES training_sessions (session_id),
    CONSTRAINT fk_document_session_questions_question FOREIGN KEY (question_id) REFERENCES document_questions (question_id),
    CONSTRAINT ck_document_session_questions_order CHECK (display_order >= 1)
);

CREATE INDEX idx_document_session_questions_session_order
    ON document_session_questions (session_id, display_order);
