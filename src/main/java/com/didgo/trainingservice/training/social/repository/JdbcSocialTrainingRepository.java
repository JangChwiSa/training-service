package com.didgo.trainingservice.training.social.repository;

import com.didgo.trainingservice.training.social.dto.SocialDialogLogRequest;
import com.didgo.trainingservice.training.social.dto.SocialDialogLogResponse;
import com.didgo.trainingservice.training.social.dto.SocialFeedbackResponse;
import com.didgo.trainingservice.training.social.dto.SocialScenarioDetailResponse;
import com.didgo.trainingservice.training.social.dto.SocialScenarioListItemResponse;
import com.didgo.trainingservice.training.social.entity.SocialDialogSpeaker;
import com.didgo.trainingservice.training.social.entity.SocialJobType;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
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
    public Optional<SocialScenarioSummaryRow> findScenarioSummaryBySessionId(long sessionId) {
        String sql = """
                SELECT scenario.scenario_id, scenario.title
                FROM training_sessions session
                JOIN social_scenarios scenario ON scenario.scenario_id = session.scenario_id
                WHERE session.session_id = ?
                """;
        List<SocialScenarioSummaryRow> rows = jdbcTemplate.query(sql, (resultSet, rowNumber) -> new SocialScenarioSummaryRow(
                resultSet.getLong("scenario_id"),
                resultSet.getString("title")
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
}
