package com.jangchwisa.trainingservice.training.safety.repository;

import com.jangchwisa.trainingservice.training.safety.dto.SafetyActionLogResponse;
import com.jangchwisa.trainingservice.training.safety.dto.SafetyChoiceResponse;
import com.jangchwisa.trainingservice.training.safety.dto.SafetyScenarioListItemResponse;
import com.jangchwisa.trainingservice.training.safety.dto.SafetySceneResponse;
import com.jangchwisa.trainingservice.training.safety.entity.SafetyCategory;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcSafetyTrainingRepository implements SafetyTrainingRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcSafetyTrainingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<SafetyScenarioListItemResponse> findActiveScenarios(SafetyCategory category) {
        String sql = """
                SELECT scenario_id, category, title, description
                FROM safety_scenarios
                WHERE is_active = true
                """;
        if (category != null) {
            sql += "  AND category = ?\n";
            sql += "ORDER BY scenario_id ASC\n";
            return jdbcTemplate.query(sql, (resultSet, rowNumber) -> new SafetyScenarioListItemResponse(
                    resultSet.getLong("scenario_id"),
                    SafetyCategory.valueOf(resultSet.getString("category")),
                    resultSet.getString("title"),
                    resultSet.getString("description")
            ), category.name());
        }
        sql += "ORDER BY scenario_id ASC\n";
        return jdbcTemplate.query(sql, (resultSet, rowNumber) -> new SafetyScenarioListItemResponse(
                resultSet.getLong("scenario_id"),
                SafetyCategory.valueOf(resultSet.getString("category")),
                resultSet.getString("title"),
                resultSet.getString("description")
        ));
    }

    @Override
    public boolean existsActiveScenario(long scenarioId) {
        String sql = """
                SELECT COUNT(*)
                FROM safety_scenarios
                WHERE scenario_id = ?
                  AND is_active = true
                """;
        Long count = jdbcTemplate.queryForObject(sql, Long.class, scenarioId);
        return count != null && count > 0;
    }

    @Override
    public Optional<SafetySceneResponse> findFirstScene(long scenarioId) {
        String sql = """
                SELECT scene_id
                FROM safety_scenes
                WHERE scenario_id = ?
                  AND scene_order = 1
                """;
        List<Long> sceneIds = jdbcTemplate.queryForList(sql, Long.class, scenarioId);
        if (sceneIds.isEmpty()) {
            return Optional.empty();
        }
        return findScene(sceneIds.getFirst());
    }

    @Override
    public Optional<SafetySceneResponse> findScene(long sceneId) {
        String sql = """
                SELECT scene_id, screen_info, situation_text, question_text, is_end_scene
                FROM safety_scenes
                WHERE scene_id = ?
                """;
        List<SafetySceneResponse> scenes = jdbcTemplate.query(sql, (resultSet, rowNumber) -> new SafetySceneResponse(
                resultSet.getLong("scene_id"),
                resultSet.getString("screen_info"),
                resultSet.getString("situation_text"),
                resultSet.getString("question_text"),
                findChoices(resultSet.getLong("scene_id")),
                resultSet.getBoolean("is_end_scene")
        ), sceneId);
        return scenes.stream().findFirst();
    }

    @Override
    public List<SafetyChoiceResponse> findChoices(long sceneId) {
        String sql = """
                SELECT choice_id, choice_text
                FROM safety_choices
                WHERE scene_id = ?
                ORDER BY choice_id ASC
                """;
        return jdbcTemplate.query(sql, (resultSet, rowNumber) -> new SafetyChoiceResponse(
                resultSet.getLong("choice_id"),
                resultSet.getString("choice_text")
        ), sceneId);
    }

    @Override
    public Optional<SafetyChoiceRow> findChoice(long sceneId, long choiceId) {
        String sql = """
                SELECT choice_id, scene_id, next_scene_id, is_correct
                FROM safety_choices
                WHERE scene_id = ?
                  AND choice_id = ?
                """;
        List<SafetyChoiceRow> choices = jdbcTemplate.query(sql, (resultSet, rowNumber) -> new SafetyChoiceRow(
                resultSet.getLong("choice_id"),
                resultSet.getLong("scene_id"),
                nullableLong(resultSet.getObject("next_scene_id")),
                resultSet.getBoolean("is_correct")
        ), sceneId, choiceId);
        return choices.stream().findFirst();
    }

    @Override
    public void saveActionLog(long sessionId, long sceneId, long choiceId, boolean correct) {
        String sql = """
                INSERT INTO safety_action_logs (session_id, scene_id, choice_id, is_correct, created_at)
                VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP(6))
                """;
        jdbcTemplate.update(sql, sessionId, sceneId, choiceId, correct);
    }

    @Override
    public Optional<SafetyScenarioSummaryRow> findScenarioSummaryBySessionId(long sessionId) {
        String sql = """
                SELECT scenario.scenario_id, scenario.category, scenario.title
                FROM training_sessions session
                JOIN safety_scenarios scenario ON scenario.scenario_id = session.scenario_id
                WHERE session.session_id = ?
                """;
        List<SafetyScenarioSummaryRow> rows = jdbcTemplate.query(sql, (resultSet, rowNumber) -> new SafetyScenarioSummaryRow(
                resultSet.getLong("scenario_id"),
                SafetyCategory.valueOf(resultSet.getString("category")),
                resultSet.getString("title")
        ), sessionId);
        return rows.stream().findFirst();
    }

    @Override
    public SafetyActionSummaryRow summarizeActions(long sessionId) {
        String sql = """
                SELECT COALESCE(SUM(CASE WHEN is_correct THEN 1 ELSE 0 END), 0) AS correct_count,
                       COUNT(*) AS total_count
                FROM safety_action_logs
                WHERE session_id = ?
                """;
        return jdbcTemplate.queryForObject(sql, (resultSet, rowNumber) -> new SafetyActionSummaryRow(
                resultSet.getInt("correct_count"),
                resultSet.getInt("total_count")
        ), sessionId);
    }

    @Override
    public List<SafetyActionLogResponse> findActionLogs(long sessionId) {
        String sql = """
                SELECT scene_id, choice_id, is_correct
                FROM safety_action_logs
                WHERE session_id = ?
                ORDER BY action_id ASC
                """;
        return jdbcTemplate.query(sql, (resultSet, rowNumber) -> new SafetyActionLogResponse(
                resultSet.getLong("scene_id"),
                resultSet.getLong("choice_id"),
                resultSet.getBoolean("is_correct")
        ), sessionId);
    }

    private static Long nullableLong(Object value) {
        if (value == null) {
            return null;
        }
        return ((Number) value).longValue();
    }
}
