ALTER TABLE social_scenarios
    ADD COLUMN seed_code VARCHAR(100);

ALTER TABLE social_scenarios
    ADD COLUMN category_code VARCHAR(100);

ALTER TABLE social_scenarios
    ADD COLUMN situation_order INT;

ALTER TABLE social_scenarios
    ADD COLUMN evaluation_point TEXT;

ALTER TABLE social_scenarios
    ADD COLUMN example_answer TEXT;

CREATE UNIQUE INDEX uq_social_scenarios_seed_code
    ON social_scenarios (seed_code);

CREATE INDEX idx_social_scenarios_category_order
    ON social_scenarios (job_type, category_code, situation_order);

ALTER TABLE safety_scenarios
    ADD COLUMN seed_code VARCHAR(100);

CREATE UNIQUE INDEX uq_safety_scenarios_seed_code
    ON safety_scenarios (seed_code);

ALTER TABLE safety_scenes
    ADD COLUMN seed_code VARCHAR(100);

CREATE UNIQUE INDEX uq_safety_scenes_seed_code
    ON safety_scenes (seed_code);

ALTER TABLE safety_choices
    ADD COLUMN seed_code VARCHAR(100);

ALTER TABLE safety_choices
    ADD COLUMN choice_order INT;

ALTER TABLE safety_choices
    ADD COLUMN result_text TEXT;

ALTER TABLE safety_choices
    ADD COLUMN effect_text TEXT;

CREATE UNIQUE INDEX uq_safety_choices_seed_code
    ON safety_choices (seed_code);

CREATE INDEX idx_safety_choices_scene_order
    ON safety_choices (scene_id, choice_order);

ALTER TABLE document_questions
    ADD COLUMN seed_code VARCHAR(100);

ALTER TABLE document_questions
    ADD COLUMN correct_feedback TEXT;

ALTER TABLE document_questions
    ADD COLUMN wrong_feedback TEXT;

CREATE UNIQUE INDEX uq_document_questions_seed_code
    ON document_questions (seed_code);

CREATE TABLE document_question_choices (
    choice_id BIGINT NOT NULL AUTO_INCREMENT,
    seed_code VARCHAR(100),
    question_id BIGINT NOT NULL,
    choice_order INT NOT NULL,
    choice_text TEXT NOT NULL,
    is_correct BOOLEAN NOT NULL,
    CONSTRAINT pk_document_question_choices PRIMARY KEY (choice_id),
    CONSTRAINT uq_document_question_choices_seed_code UNIQUE (seed_code),
    CONSTRAINT uq_document_question_choices_question_order UNIQUE (question_id, choice_order),
    CONSTRAINT fk_document_question_choices_question FOREIGN KEY (question_id) REFERENCES document_questions (question_id),
    CONSTRAINT ck_document_question_choices_order CHECK (choice_order >= 1)
);

CREATE INDEX idx_document_question_choices_question_order
    ON document_question_choices (question_id, choice_order);
