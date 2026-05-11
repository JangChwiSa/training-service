package com.didgo.trainingservice.training.safety.repository;

import com.didgo.trainingservice.training.safety.dto.SafetyActionLogResponse;
import com.didgo.trainingservice.training.safety.dto.SafetyActionDetailResponse;
import com.didgo.trainingservice.training.safety.dto.SafetyChoiceResponse;
import com.didgo.trainingservice.training.safety.dto.SafetyFeedbackResponse;
import com.didgo.trainingservice.training.safety.dto.SafetyScenarioListItemResponse;
import com.didgo.trainingservice.training.safety.dto.SafetySceneResponse;
import com.didgo.trainingservice.training.safety.entity.SafetyCategory;
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
                SELECT scene_id, screen_info, situation_text, question_text, image_url, image_alt, is_end_scene
                FROM safety_scenes
                WHERE scene_id = ?
                """;
        List<SafetySceneResponse> scenes = jdbcTemplate.query(sql, (resultSet, rowNumber) -> new SafetySceneResponse(
                resultSet.getLong("scene_id"),
                resultSet.getString("screen_info"),
                resultSet.getString("situation_text"),
                resultSet.getString("question_text"),
                resultSet.getString("image_url"),
                resultSet.getString("image_alt"),
                findChoices(resultSet.getLong("scene_id")),
                resultSet.getBoolean("is_end_scene")
        ), sceneId);
        return scenes.stream().findFirst();
    }

    @Override
    public Optional<SafetySceneResponse> findNextScene(long sceneId, long scenarioId) {
        String sql = """
                SELECT next_scene.scene_id
                FROM safety_scenes current_scene
                JOIN safety_scenes next_scene
                  ON next_scene.scenario_id = current_scene.scenario_id
                 AND next_scene.scene_order = current_scene.scene_order + 1
                WHERE current_scene.scene_id = ?
                  AND current_scene.scenario_id = ?
                """;
        List<Long> sceneIds = jdbcTemplate.queryForList(sql, Long.class, sceneId, scenarioId);
        if (sceneIds.isEmpty()) {
            return Optional.empty();
        }
        return findScene(sceneIds.getFirst());
    }

    @Override
    public List<SafetyChoiceResponse> findChoices(long sceneId) {
        String sql = """
                SELECT choice_id, choice_text
                FROM safety_choices
                WHERE scene_id = ?
                ORDER BY COALESCE(choice_order, choice_id) ASC
                """;
        return jdbcTemplate.query(sql, (resultSet, rowNumber) -> new SafetyChoiceResponse(
                resultSet.getLong("choice_id"),
                resultSet.getString("choice_text")
        ), sceneId);
    }

    @Override
    public Optional<SafetyChoiceRow> findChoice(long sceneId, long choiceId) {
        String sql = """
                SELECT choice_id, scene_id, next_scene_id, is_correct, result_text, effect_text, feedback_image_url, feedback_image_alt
                FROM safety_choices
                WHERE scene_id = ?
                  AND choice_id = ?
                """;
        List<SafetyChoiceRow> choices = jdbcTemplate.query(sql, (resultSet, rowNumber) -> new SafetyChoiceRow(
                resultSet.getLong("choice_id"),
                resultSet.getLong("scene_id"),
                nullableLong(resultSet.getObject("next_scene_id")),
                resultSet.getBoolean("is_correct"),
                resultSet.getString("result_text"),
                resultSet.getString("effect_text"),
                resultSet.getString("feedback_image_url"),
                resultSet.getString("feedback_image_alt")
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

    @Override
    public Optional<SafetyScoreRow> findScore(long sessionId) {
        String sql = """
                SELECT score, correct_count, total_count
                FROM training_scores
                WHERE session_id = ?
                """;
        List<SafetyScoreRow> scores = jdbcTemplate.query(sql, (resultSet, rowNumber) -> new SafetyScoreRow(
                resultSet.getInt("score"),
                resultSet.getInt("correct_count"),
                resultSet.getInt("total_count")
        ), sessionId);
        return scores.stream().findFirst();
    }

    @Override
    public Optional<SafetyFeedbackResponse> findFeedback(long sessionId) {
        String sql = """
                SELECT summary, detail_text
                FROM training_feedbacks
                WHERE session_id = ?
                  AND feedback_type = 'SUMMARY'
                ORDER BY created_at DESC
                LIMIT 1
                """;
        List<SafetyFeedbackResponse> feedbacks = jdbcTemplate.query(sql, (resultSet, rowNumber) -> new SafetyFeedbackResponse(
                resultSet.getString("summary"),
                resultSet.getString("detail_text")
        ), sessionId);
        return feedbacks.stream().findFirst();
    }

    @Override
    public List<SafetyActionDetailResponse> findActionDetails(long sessionId) {
        String sql = """
                SELECT action.scene_id,
                       scene.situation_text,
                       choice.choice_text,
                       action.is_correct
                FROM safety_action_logs action
                JOIN safety_scenes scene ON scene.scene_id = action.scene_id
                JOIN safety_choices choice ON choice.choice_id = action.choice_id
                    AND choice.scene_id = action.scene_id
                WHERE action.session_id = ?
                ORDER BY action.action_id ASC
                """;
        return jdbcTemplate.query(sql, (resultSet, rowNumber) -> new SafetyActionDetailResponse(
                resultSet.getLong("scene_id"),
                resultSet.getString("situation_text"),
                resultSet.getString("choice_text"),
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
