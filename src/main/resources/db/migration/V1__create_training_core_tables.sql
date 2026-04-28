CREATE TABLE training_sessions (
    session_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    training_type VARCHAR(20) NOT NULL,
    sub_type VARCHAR(50),
    scenario_id BIGINT,
    status VARCHAR(20) NOT NULL,
    current_step INT,
    started_at DATETIME(6) NOT NULL,
    ended_at DATETIME(6),
    CONSTRAINT pk_training_sessions PRIMARY KEY (session_id),
    CONSTRAINT ck_training_sessions_training_type CHECK (training_type IN ('SOCIAL', 'SAFETY', 'FOCUS', 'DOCUMENT')),
    CONSTRAINT ck_training_sessions_status CHECK (status IN ('IN_PROGRESS', 'COMPLETED', 'FAILED')),
    CONSTRAINT ck_training_sessions_focus_sub_type CHECK (training_type <> 'FOCUS' OR sub_type IS NOT NULL)
);

CREATE INDEX idx_training_sessions_user_type_started
    ON training_sessions (user_id, training_type, started_at);

CREATE TABLE training_scores (
    score_id BIGINT NOT NULL AUTO_INCREMENT,
    session_id BIGINT NOT NULL,
    score INT NOT NULL,
    score_type VARCHAR(30) NOT NULL,
    correct_count INT,
    total_count INT,
    accuracy_rate DECIMAL(5, 2),
    wrong_count INT,
    average_reaction_ms INT,
    raw_metrics_json TEXT,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_training_scores PRIMARY KEY (score_id),
    CONSTRAINT uq_training_scores_session UNIQUE (session_id),
    CONSTRAINT fk_training_scores_session FOREIGN KEY (session_id) REFERENCES training_sessions (session_id),
    CONSTRAINT ck_training_scores_score CHECK (score BETWEEN 0 AND 100),
    CONSTRAINT ck_training_scores_score_type CHECK (score_type IN ('AI_EVALUATION', 'ACCURACY_RATE', 'REACTION_PERFORMANCE', 'CHOICE_RESULT')),
    CONSTRAINT ck_training_scores_correct_count CHECK (correct_count IS NULL OR correct_count >= 0),
    CONSTRAINT ck_training_scores_total_count CHECK (total_count IS NULL OR total_count >= 0),
    CONSTRAINT ck_training_scores_correct_total CHECK (correct_count IS NULL OR total_count IS NULL OR correct_count <= total_count),
    CONSTRAINT ck_training_scores_accuracy_rate CHECK (accuracy_rate IS NULL OR accuracy_rate BETWEEN 0 AND 100),
    CONSTRAINT ck_training_scores_wrong_count CHECK (wrong_count IS NULL OR wrong_count >= 0),
    CONSTRAINT ck_training_scores_average_reaction CHECK (average_reaction_ms IS NULL OR average_reaction_ms >= 0)
);

CREATE TABLE training_feedbacks (
    feedback_id BIGINT NOT NULL AUTO_INCREMENT,
    session_id BIGINT NOT NULL,
    feedback_type VARCHAR(30) NOT NULL,
    feedback_source VARCHAR(20) NOT NULL,
    summary VARCHAR(500) NOT NULL,
    detail_text TEXT,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_training_feedbacks PRIMARY KEY (feedback_id),
    CONSTRAINT fk_training_feedbacks_session FOREIGN KEY (session_id) REFERENCES training_sessions (session_id),
    CONSTRAINT ck_training_feedbacks_feedback_type CHECK (feedback_type IN ('SUMMARY', 'DETAIL', 'RECOMMENDATION')),
    CONSTRAINT ck_training_feedbacks_feedback_source CHECK (feedback_source IN ('AI', 'SYSTEM'))
);

CREATE INDEX idx_training_feedbacks_session
    ON training_feedbacks (session_id);

CREATE TABLE training_session_summaries (
    summary_id BIGINT NOT NULL AUTO_INCREMENT,
    session_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    training_type VARCHAR(20) NOT NULL,
    scenario_id BIGINT,
    scenario_title VARCHAR(255),
    category VARCHAR(50),
    title VARCHAR(255) NOT NULL,
    score INT,
    summary_text VARCHAR(1000),
    feedback_summary VARCHAR(500),
    correct_count INT,
    total_count INT,
    accuracy_rate DECIMAL(5, 2),
    wrong_count INT,
    played_level INT,
    average_reaction_ms INT,
    completed_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_training_session_summaries PRIMARY KEY (summary_id),
    CONSTRAINT uq_training_session_summaries_session UNIQUE (session_id),
    CONSTRAINT fk_training_session_summaries_session FOREIGN KEY (session_id) REFERENCES training_sessions (session_id),
    CONSTRAINT ck_training_session_summaries_type CHECK (training_type IN ('SOCIAL', 'SAFETY', 'FOCUS', 'DOCUMENT')),
    CONSTRAINT ck_training_session_summaries_category CHECK (category IS NULL OR category IN ('SEXUAL_EDUCATION', 'INFECTIOUS_DISEASE', 'COMMUTE_SAFETY')),
    CONSTRAINT ck_training_session_summaries_score CHECK (score IS NULL OR score BETWEEN 0 AND 100),
    CONSTRAINT ck_training_session_summaries_correct_count CHECK (correct_count IS NULL OR correct_count >= 0),
    CONSTRAINT ck_training_session_summaries_total_count CHECK (total_count IS NULL OR total_count >= 0),
    CONSTRAINT ck_training_session_summaries_correct_total CHECK (correct_count IS NULL OR total_count IS NULL OR correct_count <= total_count),
    CONSTRAINT ck_training_session_summaries_accuracy CHECK (accuracy_rate IS NULL OR accuracy_rate BETWEEN 0 AND 100),
    CONSTRAINT ck_training_session_summaries_wrong_count CHECK (wrong_count IS NULL OR wrong_count >= 0),
    CONSTRAINT ck_training_session_summaries_played_level CHECK (played_level IS NULL OR played_level >= 1),
    CONSTRAINT ck_training_session_summaries_reaction CHECK (average_reaction_ms IS NULL OR average_reaction_ms >= 0)
);

CREATE INDEX idx_training_session_summaries_user_type_completed
    ON training_session_summaries (user_id, training_type, completed_at);

CREATE INDEX idx_training_session_summaries_user_type_category_completed
    ON training_session_summaries (user_id, training_type, category, completed_at);

CREATE TABLE outbox_events (
    event_id VARCHAR(64) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    aggregate_type VARCHAR(50) NOT NULL,
    aggregate_id BIGINT NOT NULL,
    session_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    training_type VARCHAR(20) NOT NULL,
    payload_json TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    retry_count INT NOT NULL,
    max_retry_count INT NOT NULL,
    next_retry_at DATETIME(6),
    published_at DATETIME(6),
    last_error_message VARCHAR(1000),
    dlq_reason VARCHAR(500),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_outbox_events PRIMARY KEY (event_id),
    CONSTRAINT uq_outbox_events_session_type UNIQUE (session_id, event_type),
    CONSTRAINT fk_outbox_events_session FOREIGN KEY (session_id) REFERENCES training_sessions (session_id),
    CONSTRAINT ck_outbox_events_event_type CHECK (event_type IN ('TrainingCompleted')),
    CONSTRAINT ck_outbox_events_aggregate_type CHECK (aggregate_type IN ('TrainingSession')),
    CONSTRAINT ck_outbox_events_training_type CHECK (training_type IN ('SOCIAL', 'SAFETY', 'FOCUS', 'DOCUMENT')),
    CONSTRAINT ck_outbox_events_status CHECK (status IN ('PENDING', 'PUBLISHED', 'RETRY', 'DLQ')),
    CONSTRAINT ck_outbox_events_retry_count CHECK (retry_count >= 0),
    CONSTRAINT ck_outbox_events_max_retry_count CHECK (max_retry_count >= 0),
    CONSTRAINT ck_outbox_events_retry_limit CHECK (retry_count <= max_retry_count)
);

CREATE INDEX idx_outbox_events_status_next_retry_created
    ON outbox_events (status, next_retry_at, created_at);

CREATE INDEX idx_outbox_events_session
    ON outbox_events (session_id);
