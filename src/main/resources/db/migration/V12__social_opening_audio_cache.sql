CREATE TABLE social_opening_audio_assets (
    cache_key VARCHAR(64) NOT NULL,
    scenario_id BIGINT NOT NULL,
    script TEXT NOT NULL,
    model VARCHAR(100) NOT NULL,
    voice VARCHAR(100) NOT NULL,
    response_format VARCHAR(20) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    audio_data MEDIUMBLOB NOT NULL,
    created_at DATETIME(6) NOT NULL,
    accessed_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_social_opening_audio_assets PRIMARY KEY (cache_key),
    CONSTRAINT fk_social_opening_audio_assets_scenario FOREIGN KEY (scenario_id) REFERENCES social_scenarios (scenario_id)
);

CREATE INDEX idx_social_opening_audio_assets_scenario
    ON social_opening_audio_assets (scenario_id);
