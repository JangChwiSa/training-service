CREATE TABLE social_scenarios (
    scenario_id BIGINT NOT NULL AUTO_INCREMENT,
    job_type VARCHAR(20) NOT NULL,
    title VARCHAR(255) NOT NULL,
    background_text TEXT,
    situation_text TEXT NOT NULL,
    character_info TEXT,
    difficulty VARCHAR(50),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_social_scenarios PRIMARY KEY (scenario_id),
    CONSTRAINT ck_social_scenarios_job_type CHECK (job_type IN ('OFFICE', 'LABOR'))
);

CREATE TABLE social_dialog_logs (
    log_id BIGINT NOT NULL AUTO_INCREMENT,
    session_id BIGINT NOT NULL,
    turn_no INT NOT NULL,
    speaker VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_social_dialog_logs PRIMARY KEY (log_id),
    CONSTRAINT uq_social_dialog_logs_session_turn_speaker UNIQUE (session_id, turn_no, speaker),
    CONSTRAINT fk_social_dialog_logs_session FOREIGN KEY (session_id) REFERENCES training_sessions (session_id),
    CONSTRAINT ck_social_dialog_logs_speaker CHECK (speaker IN ('USER', 'AI'))
);

CREATE INDEX idx_social_dialog_logs_session_turn
    ON social_dialog_logs (session_id, turn_no);

CREATE TABLE user_social_progress (
    progress_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    recent_session_id BIGINT,
    recent_score INT,
    recent_feedback_summary VARCHAR(500),
    completed_count INT NOT NULL,
    last_completed_at DATETIME(6),
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_user_social_progress PRIMARY KEY (progress_id),
    CONSTRAINT uq_user_social_progress_user UNIQUE (user_id),
    CONSTRAINT fk_user_social_progress_recent_session FOREIGN KEY (recent_session_id) REFERENCES training_sessions (session_id),
    CONSTRAINT ck_user_social_progress_recent_score CHECK (recent_score IS NULL OR recent_score BETWEEN 0 AND 100),
    CONSTRAINT ck_user_social_progress_completed_count CHECK (completed_count >= 0)
);

CREATE TABLE safety_scenarios (
    scenario_id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    category VARCHAR(50) NOT NULL,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_safety_scenarios PRIMARY KEY (scenario_id),
    CONSTRAINT ck_safety_scenarios_category CHECK (category IN ('SEXUAL_EDUCATION', 'INFECTIOUS_DISEASE', 'COMMUTE_SAFETY'))
);

CREATE INDEX idx_safety_scenarios_category_active
    ON safety_scenarios (category, is_active);

CREATE TABLE safety_scenes (
    scene_id BIGINT NOT NULL AUTO_INCREMENT,
    scenario_id BIGINT NOT NULL,
    scene_order INT NOT NULL,
    screen_info TEXT,
    situation_text TEXT NOT NULL,
    question_text TEXT NOT NULL,
    is_end_scene BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_safety_scenes PRIMARY KEY (scene_id),
    CONSTRAINT uq_safety_scenes_scenario_order UNIQUE (scenario_id, scene_order),
    CONSTRAINT fk_safety_scenes_scenario FOREIGN KEY (scenario_id) REFERENCES safety_scenarios (scenario_id)
);

CREATE TABLE safety_choices (
    choice_id BIGINT NOT NULL AUTO_INCREMENT,
    scene_id BIGINT NOT NULL,
    choice_text TEXT NOT NULL,
    next_scene_id BIGINT,
    is_correct BOOLEAN NOT NULL,
    CONSTRAINT pk_safety_choices PRIMARY KEY (choice_id),
    CONSTRAINT fk_safety_choices_scene FOREIGN KEY (scene_id) REFERENCES safety_scenes (scene_id),
    CONSTRAINT fk_safety_choices_next_scene FOREIGN KEY (next_scene_id) REFERENCES safety_scenes (scene_id)
);

CREATE TABLE safety_action_logs (
    action_id BIGINT NOT NULL AUTO_INCREMENT,
    session_id BIGINT NOT NULL,
    scene_id BIGINT NOT NULL,
    choice_id BIGINT NOT NULL,
    is_correct BOOLEAN NOT NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_safety_action_logs PRIMARY KEY (action_id),
    CONSTRAINT fk_safety_action_logs_session FOREIGN KEY (session_id) REFERENCES training_sessions (session_id),
    CONSTRAINT fk_safety_action_logs_scene FOREIGN KEY (scene_id) REFERENCES safety_scenes (scene_id),
    CONSTRAINT fk_safety_action_logs_choice FOREIGN KEY (choice_id) REFERENCES safety_choices (choice_id)
);

CREATE INDEX idx_safety_action_logs_session
    ON safety_action_logs (session_id);

CREATE TABLE user_safety_progress (
    progress_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    recent_session_id BIGINT,
    correct_count INT NOT NULL,
    total_count INT NOT NULL,
    completed_count INT NOT NULL,
    last_completed_at DATETIME(6),
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_user_safety_progress PRIMARY KEY (progress_id),
    CONSTRAINT uq_user_safety_progress_user UNIQUE (user_id),
    CONSTRAINT fk_user_safety_progress_recent_session FOREIGN KEY (recent_session_id) REFERENCES training_sessions (session_id),
    CONSTRAINT ck_user_safety_progress_correct_count CHECK (correct_count >= 0),
    CONSTRAINT ck_user_safety_progress_total_count CHECK (total_count >= 0),
    CONSTRAINT ck_user_safety_progress_correct_total CHECK (correct_count <= total_count),
    CONSTRAINT ck_user_safety_progress_completed_count CHECK (completed_count >= 0)
);

CREATE TABLE focus_level_rules (
    level INT NOT NULL,
    duration_seconds INT NOT NULL DEFAULT 180,
    command_interval_ms INT NOT NULL,
    command_complexity VARCHAR(50) NOT NULL,
    required_accuracy_rate DECIMAL(5, 2) NOT NULL DEFAULT 90,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_focus_level_rules PRIMARY KEY (level),
    CONSTRAINT ck_focus_level_rules_level CHECK (level >= 1),
    CONSTRAINT ck_focus_level_rules_duration CHECK (duration_seconds > 0),
    CONSTRAINT ck_focus_level_rules_interval CHECK (command_interval_ms > 0),
    CONSTRAINT ck_focus_level_rules_required_accuracy CHECK (required_accuracy_rate BETWEEN 0 AND 100)
);

CREATE TABLE focus_commands (
    command_id BIGINT NOT NULL AUTO_INCREMENT,
    session_id BIGINT NOT NULL,
    command_order INT NOT NULL,
    command_text VARCHAR(255) NOT NULL,
    expected_action VARCHAR(255) NOT NULL,
    display_at_ms INT NOT NULL,
    CONSTRAINT pk_focus_commands PRIMARY KEY (command_id),
    CONSTRAINT uq_focus_commands_session_order UNIQUE (session_id, command_order),
    CONSTRAINT fk_focus_commands_session FOREIGN KEY (session_id) REFERENCES training_sessions (session_id)
);

CREATE INDEX idx_focus_commands_session_order
    ON focus_commands (session_id, command_order);

CREATE TABLE focus_reaction_logs (
    reaction_id BIGINT NOT NULL AUTO_INCREMENT,
    command_id BIGINT NOT NULL,
    session_id BIGINT NOT NULL,
    user_input VARCHAR(255) NOT NULL,
    is_correct BOOLEAN NOT NULL,
    reaction_ms INT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_focus_reaction_logs PRIMARY KEY (reaction_id),
    CONSTRAINT uq_focus_reaction_logs_command_session UNIQUE (command_id, session_id),
    CONSTRAINT fk_focus_reaction_logs_command FOREIGN KEY (command_id) REFERENCES focus_commands (command_id),
    CONSTRAINT fk_focus_reaction_logs_session FOREIGN KEY (session_id) REFERENCES training_sessions (session_id),
    CONSTRAINT ck_focus_reaction_logs_reaction_ms CHECK (reaction_ms >= 0)
);

CREATE INDEX idx_focus_reaction_logs_session
    ON focus_reaction_logs (session_id);

CREATE TABLE user_focus_progress (
    progress_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    current_level INT NOT NULL,
    highest_unlocked_level INT NOT NULL,
    last_played_level INT,
    last_accuracy_rate DECIMAL(5, 2),
    last_average_reaction_ms INT,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_user_focus_progress PRIMARY KEY (progress_id),
    CONSTRAINT uq_user_focus_progress_user UNIQUE (user_id),
    CONSTRAINT ck_user_focus_progress_current_level CHECK (current_level >= 1),
    CONSTRAINT ck_user_focus_progress_highest_level CHECK (highest_unlocked_level >= 1),
    CONSTRAINT ck_user_focus_progress_last_accuracy CHECK (last_accuracy_rate IS NULL OR last_accuracy_rate BETWEEN 0 AND 100),
    CONSTRAINT ck_user_focus_progress_last_reaction CHECK (last_average_reaction_ms IS NULL OR last_average_reaction_ms >= 0)
);

CREATE TABLE document_questions (
    question_id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    document_text TEXT,
    question_text TEXT NOT NULL,
    question_type VARCHAR(50),
    correct_answer TEXT NOT NULL,
    explanation TEXT,
    difficulty VARCHAR(50),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_document_questions PRIMARY KEY (question_id)
);

CREATE TABLE document_answer_logs (
    answer_id BIGINT NOT NULL AUTO_INCREMENT,
    session_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    user_answer TEXT NOT NULL,
    correct_answer TEXT NOT NULL,
    is_correct BOOLEAN NOT NULL,
    explanation TEXT,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_document_answer_logs PRIMARY KEY (answer_id),
    CONSTRAINT uq_document_answer_logs_session_question UNIQUE (session_id, question_id),
    CONSTRAINT fk_document_answer_logs_session FOREIGN KEY (session_id) REFERENCES training_sessions (session_id),
    CONSTRAINT fk_document_answer_logs_question FOREIGN KEY (question_id) REFERENCES document_questions (question_id)
);

CREATE INDEX idx_document_answer_logs_session
    ON document_answer_logs (session_id);

CREATE TABLE user_document_progress (
    progress_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    recent_session_id BIGINT,
    correct_count INT NOT NULL,
    total_count INT NOT NULL,
    recent_score INT,
    completed_count INT NOT NULL,
    last_completed_at DATETIME(6),
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_user_document_progress PRIMARY KEY (progress_id),
    CONSTRAINT uq_user_document_progress_user UNIQUE (user_id),
    CONSTRAINT fk_user_document_progress_recent_session FOREIGN KEY (recent_session_id) REFERENCES training_sessions (session_id),
    CONSTRAINT ck_user_document_progress_correct_count CHECK (correct_count >= 0),
    CONSTRAINT ck_user_document_progress_total_count CHECK (total_count >= 0),
    CONSTRAINT ck_user_document_progress_correct_total CHECK (correct_count <= total_count),
    CONSTRAINT ck_user_document_progress_recent_score CHECK (recent_score IS NULL OR recent_score BETWEEN 0 AND 100),
    CONSTRAINT ck_user_document_progress_completed_count CHECK (completed_count >= 0)
);
