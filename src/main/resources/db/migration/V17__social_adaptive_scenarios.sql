ALTER TABLE social_scenarios
    ADD COLUMN generated_by_user_id BIGINT;

ALTER TABLE social_scenarios
    ADD COLUMN generated_focus_summary VARCHAR(500);

CREATE INDEX idx_social_scenarios_generated_user
    ON social_scenarios (generated_by_user_id, job_type, is_active);
