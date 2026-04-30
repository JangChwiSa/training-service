package com.didgo.trainingservice.training.focus.repository;

import com.didgo.trainingservice.common.exception.ErrorCode;
import com.didgo.trainingservice.common.exception.TrainingServiceException;
import com.didgo.trainingservice.training.focus.dto.FocusCommandResponse;
import com.didgo.trainingservice.training.focus.dto.FocusProgressResponse;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcFocusTrainingRepository implements FocusTrainingRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcFocusTrainingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<FocusProgressResponse> findProgress(long userId) {
        String sql = """
                SELECT current_level, highest_unlocked_level, last_played_level,
                       last_accuracy_rate, last_average_reaction_ms
                FROM user_focus_progress
                WHERE user_id = ?
                """;
        List<FocusProgressResponse> progress = jdbcTemplate.query(sql, (resultSet, rowNumber) -> new FocusProgressResponse(
                resultSet.getInt("current_level"),
                resultSet.getInt("highest_unlocked_level"),
                nullableInteger(resultSet.getObject("last_played_level")),
                resultSet.getBigDecimal("last_accuracy_rate"),
                nullableInteger(resultSet.getObject("last_average_reaction_ms"))
        ), userId);
        return progress.stream().findFirst();
    }

    @Override
    public Optional<FocusLevelRuleRow> findActiveLevelRule(int level) {
        String sql = """
                SELECT level, duration_seconds, command_interval_ms, command_complexity, required_accuracy_rate
                FROM focus_level_rules
                WHERE level = ?
                  AND is_active = true
                """;
        List<FocusLevelRuleRow> rules = jdbcTemplate.query(sql, (resultSet, rowNumber) -> new FocusLevelRuleRow(
                resultSet.getInt("level"),
                resultSet.getInt("duration_seconds"),
                resultSet.getInt("command_interval_ms"),
                resultSet.getString("command_complexity"),
                resultSet.getBigDecimal("required_accuracy_rate")
        ), level);
        return rules.stream().findFirst();
    }

    @Override
    public Optional<Integer> findSessionLevel(long sessionId) {
        String sql = """
                SELECT sub_type
                FROM training_sessions
                WHERE session_id = ?
                  AND training_type = 'FOCUS'
                """;
        List<Integer> levels = jdbcTemplate.query(sql, (resultSet, rowNumber) ->
                Integer.parseInt(resultSet.getString("sub_type")), sessionId);
        return levels.stream().findFirst();
    }

    @Override
    public List<FocusCommandResponse> saveCommands(long sessionId, List<NewFocusCommand> commands) {
        List<FocusCommandResponse> savedCommands = new ArrayList<>();
        String sql = """
                INSERT INTO focus_commands (session_id, command_order, command_text, expected_action, display_at_ms)
                VALUES (?, ?, ?, ?, ?)
                """;

        for (NewFocusCommand command : commands) {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                statement.setLong(1, sessionId);
                statement.setInt(2, command.order());
                statement.setString(3, command.commandText());
                statement.setString(4, command.expectedAction());
                statement.setInt(5, command.displayAtMs());
                return statement;
            }, keyHolder);

            Number key = keyHolder.getKey();
            if (key == null) {
                throw new TrainingServiceException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to create focus command.");
            }
            savedCommands.add(new FocusCommandResponse(
                    key.longValue(),
                    command.order(),
                    command.commandText(),
                    command.expectedAction(),
                    command.displayAtMs()
            ));
        }

        return savedCommands;
    }

    @Override
    public List<FocusCommandRow> findCommands(long sessionId) {
        String sql = """
                SELECT command_id, command_order, command_text, expected_action
                FROM focus_commands
                WHERE session_id = ?
                ORDER BY command_order ASC
                """;
        return jdbcTemplate.query(sql, (resultSet, rowNumber) -> new FocusCommandRow(
                resultSet.getLong("command_id"),
                resultSet.getInt("command_order"),
                resultSet.getString("command_text"),
                resultSet.getString("expected_action")
        ), sessionId);
    }

    @Override
    public void saveReactionLogs(long sessionId, List<ScoredFocusReaction> reactions) {
        String sql = """
                INSERT INTO focus_reaction_logs (
                    command_id, session_id, user_input, is_correct, reaction_ms, created_at
                )
                VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP(6))
                """;
        for (ScoredFocusReaction reaction : reactions) {
            jdbcTemplate.update(
                    sql,
                    reaction.commandId(),
                    sessionId,
                    reaction.userInput(),
                    reaction.correct(),
                    reaction.reactionMs()
            );
        }
    }

    private static Integer nullableInteger(Object value) {
        if (value == null) {
            return null;
        }
        return ((Number) value).intValue();
    }
}
