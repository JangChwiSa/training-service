CREATE TABLE user_social_weakness_profiles (
    user_id BIGINT NOT NULL,
    job_type VARCHAR(20) NOT NULL,
    weakness_label VARCHAR(100) NOT NULL,
    weakness_summary VARCHAR(500) NOT NULL,
    source_session_id BIGINT NULL,
    recent_score INT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (user_id, job_type),
    CONSTRAINT fk_user_social_weakness_profiles_session
        FOREIGN KEY (source_session_id) REFERENCES training_sessions (session_id)
);

CREATE TABLE social_adaptive_scenario_recommendations (
    recommendation_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    job_type VARCHAR(20) NOT NULL,
    scenario_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    consumed_at DATETIME(6) NULL,
    PRIMARY KEY (recommendation_id),
    CONSTRAINT fk_social_adaptive_recommendations_scenario
        FOREIGN KEY (scenario_id) REFERENCES social_scenarios (scenario_id)
);

CREATE INDEX idx_social_adaptive_recommendations_ready
    ON social_adaptive_scenario_recommendations (user_id, job_type, status, created_at);
