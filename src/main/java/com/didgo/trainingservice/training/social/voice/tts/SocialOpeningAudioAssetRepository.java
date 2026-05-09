package com.didgo.trainingservice.training.social.voice.tts;

import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class SocialOpeningAudioAssetRepository {

    private final JdbcTemplate jdbcTemplate;

    public SocialOpeningAudioAssetRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<SocialOpeningAudioAsset> findByCacheKey(String cacheKey) {
        String sql = """
                SELECT cache_key, scenario_id, script, model, voice, response_format, content_type, audio_data
                FROM social_opening_audio_assets
                WHERE cache_key = ?
                """;
        List<SocialOpeningAudioAsset> assets = jdbcTemplate.query(sql, (resultSet, rowNumber) -> new SocialOpeningAudioAsset(
                resultSet.getString("cache_key"),
                resultSet.getLong("scenario_id"),
                resultSet.getString("script"),
                resultSet.getString("model"),
                resultSet.getString("voice"),
                resultSet.getString("response_format"),
                resultSet.getString("content_type"),
                resultSet.getBytes("audio_data")
        ), cacheKey);
        if (!assets.isEmpty()) {
            jdbcTemplate.update("""
                    UPDATE social_opening_audio_assets
                    SET accessed_at = CURRENT_TIMESTAMP(6)
                    WHERE cache_key = ?
                    """, cacheKey);
        }
        return assets.stream().findFirst();
    }

    public void save(SocialOpeningAudioAsset asset) {
        jdbcTemplate.update("""
                INSERT INTO social_opening_audio_assets (
                    cache_key, scenario_id, script, model, voice, response_format, content_type, audio_data, created_at, accessed_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6))
                ON DUPLICATE KEY UPDATE
                    accessed_at = CURRENT_TIMESTAMP(6)
                """,
                asset.cacheKey(),
                asset.scenarioId(),
                asset.script(),
                asset.model(),
                asset.voice(),
                asset.responseFormat(),
                asset.contentType(),
                asset.audioData()
        );
    }
}
