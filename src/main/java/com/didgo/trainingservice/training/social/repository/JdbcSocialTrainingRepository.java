package com.didgo.trainingservice.training.social.repository;

import com.didgo.trainingservice.training.social.dto.SocialDialogLogRequest;
import com.didgo.trainingservice.training.social.dto.SocialDialogLogResponse;
import com.didgo.trainingservice.training.social.dto.SocialFeedbackResponse;
import com.didgo.trainingservice.training.social.dto.SocialScenarioDetailResponse;
import com.didgo.trainingservice.training.social.dto.SocialScenarioListItemResponse;
import com.didgo.trainingservice.training.social.entity.SocialDialogSpeaker;
import com.didgo.trainingservice.training.social.entity.SocialJobType;
import com.didgo.trainingservice.training.social.service.SocialAdaptiveScenarioDraft;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcSocialTrainingRepository implements SocialTrainingRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcSocialTrainingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<SocialScenarioListItemResponse> findActiveScenariosByJobType(SocialJobType jobType) {
        String sql = """
                SELECT scenario_id, title, difficulty
                FROM social_scenarios
                WHERE job_type = ?
                  AND is_active = true
                  AND generated_by_user_id IS NULL
                ORDER BY scenario_id ASC
                """;
        return jdbcTemplate.query(sql, (resultSet, rowNumber) -> new SocialScenarioListItemResponse(
                resultSet.getLong("scenario_id"),
                resultSet.getString("title"),
                parseDifficulty(resultSet.getString("difficulty"))
        ), jobType.name());
    }

    @Override
    public Optional<SocialScenarioDetailResponse> findActiveScenarioDetail(long scenarioId) {
        String sql = """
                SELECT scenario_id, job_type, title, background_text, situation_text, character_info, difficulty
                FROM social_scenarios
                WHERE scenario_id = ?
                  AND is_active = true
                """;
        List<SocialScenarioDetailResponse> scenarios = jdbcTemplate.query(sql, (resultSet, rowNumber) -> new SocialScenarioDetailResponse(
                resultSet.getLong("scenario_id"),
                SocialJobType.valueOf(resultSet.getString("job_type")),
                resultSet.getString("title"),
                resultSet.getString("background_text"),
                resultSet.getString("situation_text"),
                resultSet.getString("character_info"),
                parseDifficulty(resultSet.getString("difficulty"))
        ), scenarioId);
        return scenarios.stream().findFirst();
    }

    @Override
    public Optional<SocialScenarioDetailResponse> findAccessibleScenarioDetail(long scenarioId, long userId) {
        String sql = """
                SELECT scenario_id, job_type, title, background_text, situation_text, character_info, difficulty
                FROM social_scenarios
                WHERE scenario_id = ?
                  AND is_active = true
                  AND (generated_by_user_id IS NULL OR generated_by_user_id = ?)
                """;
        List<SocialScenarioDetailResponse> scenarios = jdbcTemplate.query(sql, (resultSet, rowNumber) -> new SocialScenarioDetailResponse(
                resultSet.getLong("scenario_id"),
                SocialJobType.valueOf(resultSet.getString("job_type")),
                resultSet.getString("title"),
                resultSet.getString("background_text"),
                resultSet.getString("situation_text"),
                resultSet.getString("character_info"),
                parseDifficulty(resultSet.getString("difficulty"))
        ), scenarioId, userId);
        return scenarios.stream().findFirst();
    }

    @Override
    public boolean existsActiveScenario(long scenarioId, SocialJobType jobType) {
        String sql = """
                SELECT COUNT(*)
                FROM social_scenarios
                WHERE scenario_id = ?
                  AND job_type = ?
                  AND is_active = true
                """;
        Long count = jdbcTemplate.queryForObject(sql, Long.class, scenarioId, jobType.name());
        return count != null && count > 0;
    }

    @Override
    public boolean existsAccessibleScenario(long scenarioId, SocialJobType jobType, long userId) {
        String sql = """
                SELECT COUNT(*)
                FROM social_scenarios
                WHERE scenario_id = ?
                  AND job_type = ?
                  AND is_active = true
                  AND (generated_by_user_id IS NULL OR generated_by_user_id = ?)
                """;
        Long count = jdbcTemplate.queryForObject(sql, Long.class, scenarioId, jobType.name(), userId);
        return count != null && count > 0;
    }

    @Override
    public long saveGeneratedScenario(long userId, SocialJobType jobType, SocialAdaptiveScenarioDraft draft) {
        String sql = """
                INSERT INTO social_scenarios (
                    job_type, title, background_text, situation_text, character_info, difficulty, is_active,
                    seed_code, category_code, situation_order, evaluation_point, example_answer,
                    generated_by_user_id, generated_focus_summary
                )
                VALUES (?, ?, ?, ?, ?, ?, true, NULL, ?, NULL, ?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, jobType.name());
            statement.setString(2, draft.title());
            statement.setString(3, draft.backgroundText());
            statement.setString(4, draft.situationText());
            statement.setString(5, draft.characterInfo());
            statement.setString(6, draft.difficulty());
            statement.setString(7, draft.categoryCode());
            statement.setString(8, draft.evaluationPoint());
            statement.setString(9, draft.exampleAnswer());
            statement.setLong(10, userId);
            statement.setString(11, draft.focusSummary());
            return statement;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("Generated social scenario id was not returned.");
        }
        return key.longValue();
    }

    @Override
    public List<SocialHistoryRow> findRecentSocialHistory(long userId, int limit) {
        String sql = """
                SELECT summary.session_id,
                       summary.scenario_title,
                       summary.score,
                       feedback.summary AS feedback_summary,
                       feedback.detail_text AS feedback_detail
                FROM training_session_summaries summary
                LEFT JOIN training_feedbacks feedback
                       ON feedback.session_id = summary.session_id
                      AND feedback.feedback_type = 'SUMMARY'
                WHERE summary.user_id = ?
                  AND summary.training_type = 'SOCIAL'
                ORDER BY summary.completed_at DESC
                LIMIT ?
                """;
        return jdbcTemplate.query(sql, (resultSet, rowNumber) -> new SocialHistoryRow(
                resultSet.getLong("session_id"),
                resultSet.getString("scenario_title"),
                nullableInteger(resultSet.getObject("score")),
                resultSet.getString("feedback_summary"),
                resultSet.getString("feedback_detail")
        ), userId, Math.max(1, limit));
    }

    @Override
    public Optional<SocialAdaptiveRecommendationRow> findReadyAdaptiveRecommendation(long userId, SocialJobType jobType) {
        String sql = """
                SELECT recommendation.recommendation_id,
                       recommendation.scenario_id,
                       scenario.generated_focus_summary
                FROM social_adaptive_scenario_recommendations recommendation
                JOIN social_scenarios scenario ON scenario.scenario_id = recommendation.scenario_id
                WHERE recommendation.user_id = ?
                  AND recommendation.job_type = ?
                  AND recommendation.status = 'READY'
                  AND scenario.is_active = true
                  AND scenario.generated_by_user_id = ?
                ORDER BY recommendation.created_at DESC, recommendation.recommendation_id DESC
                LIMIT 1
                """;
        List<SocialAdaptiveRecommendationRow> rows = jdbcTemplate.query(sql, (resultSet, rowNumber) -> new SocialAdaptiveRecommendationRow(
                resultSet.getLong("recommendation_id"),
                resultSet.getLong("scenario_id"),
                resultSet.getString("generated_focus_summary")
        ), userId, jobType.name(), userId);
        return rows.stream().findFirst();
    }

    @Override
    public void saveAdaptiveRecommendation(long userId, SocialJobType jobType, long scenarioId) {
        String sql = """
                INSERT INTO social_adaptive_scenario_recommendations (
                    user_id, job_type, scenario_id, status, created_at
                )
                VALUES (?, ?, ?, 'READY', CURRENT_TIMESTAMP(6))
                """;
        jdbcTemplate.update(sql, userId, jobType.name(), scenarioId);
    }

    @Override
    public void markAdaptiveRecommendationConsumed(long recommendationId) {
        String sql = """
                UPDATE social_adaptive_scenario_recommendations
                SET status = 'CONSUMED',
                    consumed_at = CURRENT_TIMESTAMP(6)
                WHERE recommendation_id = ?
                  AND status = 'READY'
                """;
        jdbcTemplate.update(sql, recommendationId);
    }

    @Override
    public void upsertWeaknessProfile(
            long userId,
            SocialJobType jobType,
            long sourceSessionId,
            Integer recentScore,
            String weaknessLabel,
            String weaknessSummary
    ) {
        String sql = """
                INSERT INTO user_social_weakness_profiles (
                    user_id, job_type, weakness_label, weakness_summary, source_session_id, recent_score, updated_at
                )
                VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP(6))
                ON DUPLICATE KEY UPDATE
                    weakness_label = VALUES(weakness_label),
                    weakness_summary = VALUES(weakness_summary),
                    source_session_id = VALUES(source_session_id),
                    recent_score = VALUES(recent_score),
                    updated_at = CURRENT_TIMESTAMP(6)
                """;
        jdbcTemplate.update(sql, userId, jobType.name(), weaknessLabel, weaknessSummary, sourceSessionId, recentScore);
    }

    @Override
    public Optional<SocialWeaknessProfileRow> findWeaknessProfile(long userId, SocialJobType jobType) {
        String sql = """
                SELECT user_id, job_type, weakness_label, weakness_summary, source_session_id, recent_score
                FROM user_social_weakness_profiles
                WHERE user_id = ?
                  AND job_type = ?
                """;
        List<SocialWeaknessProfileRow> rows = jdbcTemplate.query(sql, (resultSet, rowNumber) -> new SocialWeaknessProfileRow(
                resultSet.getLong("user_id"),
                SocialJobType.valueOf(resultSet.getString("job_type")),
                resultSet.getString("weakness_label"),
                resultSet.getString("weakness_summary"),
                nullableLong(resultSet.getObject("source_session_id")),
                nullableInteger(resultSet.getObject("recent_score"))
        ), userId, jobType.name());
        return rows.stream().findFirst();
    }

    @Override
    public Optional<SocialScenarioSummaryRow> findScenarioSummaryBySessionId(long sessionId) {
        String sql = """
                SELECT scenario.scenario_id, scenario.title, scenario.job_type
                FROM training_sessions session
                JOIN social_scenarios scenario ON scenario.scenario_id = session.scenario_id
                WHERE session.session_id = ?
                """;
        List<SocialScenarioSummaryRow> rows = jdbcTemplate.query(sql, (resultSet, rowNumber) -> new SocialScenarioSummaryRow(
                resultSet.getLong("scenario_id"),
                resultSet.getString("title"),
                SocialJobType.valueOf(resultSet.getString("job_type"))
        ), sessionId);
        return rows.stream().findFirst();
    }

    @Override
    public void saveDialogLogs(long sessionId, List<SocialDialogLogRequest> dialogLogs) {
        String sql = """
                INSERT INTO social_dialog_logs (session_id, turn_no, speaker, content, created_at)
                VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP(6))
                """;
        for (SocialDialogLogRequest dialogLog : dialogLogs) {
            jdbcTemplate.update(
                    sql,
                    sessionId,
                    dialogLog.turnNo(),
                    dialogLog.speaker().name(),
                    dialogLog.content()
            );
        }
    }

    @Override
    public Optional<SocialScoreRow> findScore(long sessionId) {
        String sql = """
                SELECT score, score_type
                FROM training_scores
                WHERE session_id = ?
                """;
        List<SocialScoreRow> scores = jdbcTemplate.query(sql, (resultSet, rowNumber) -> new SocialScoreRow(
                resultSet.getInt("score"),
                resultSet.getString("score_type")
        ), sessionId);
        return scores.stream().findFirst();
    }

    @Override
    public Optional<SocialFeedbackResponse> findFeedback(long sessionId) {
        String sql = """
                SELECT summary, detail_text
                FROM training_feedbacks
                WHERE session_id = ?
                  AND feedback_type = 'SUMMARY'
                ORDER BY created_at DESC
                LIMIT 1
                """;
        List<SocialFeedbackResponse> feedbacks = jdbcTemplate.query(sql, (resultSet, rowNumber) -> new SocialFeedbackResponse(
                resultSet.getString("summary"),
                resultSet.getString("detail_text")
        ), sessionId);
        return feedbacks.stream().findFirst();
    }

    @Override
    public List<SocialDialogLogResponse> findDialogLogs(long sessionId) {
        String sql = """
                SELECT turn_no, speaker, content
                FROM social_dialog_logs
                WHERE session_id = ?
                ORDER BY turn_no ASC,
                         CASE speaker
                             WHEN 'USER' THEN 1
                             WHEN 'AI' THEN 2
                             ELSE 3
                         END
                """;
        return jdbcTemplate.query(sql, (resultSet, rowNumber) -> new SocialDialogLogResponse(
                resultSet.getInt("turn_no"),
                SocialDialogSpeaker.valueOf(resultSet.getString("speaker")),
                resultSet.getString("content")
        ), sessionId);
    }

    private static Integer parseDifficulty(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim().toUpperCase();
        return switch (normalized) {
            case "EASY" -> 1;
            case "MEDIUM" -> 2;
            case "HARD" -> 3;
            default -> {
                if (normalized.startsWith("LEVEL_")) {
                    yield Integer.valueOf(normalized.substring("LEVEL_".length()));
                }
                yield Integer.valueOf(normalized);
            }
        };
    }

    private static Integer nullableInteger(Object value) {
        if (value == null) {
            return null;
        }
        return ((Number) value).intValue();
    }

    private static Long nullableLong(Object value) {
        if (value == null) {
            return null;
        }
        return ((Number) value).longValue();
    }
}
