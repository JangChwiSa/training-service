package com.jangchwisa.trainingservice.training.session.repository;

import com.jangchwisa.trainingservice.common.exception.ErrorCode;
import com.jangchwisa.trainingservice.common.exception.TrainingServiceException;
import com.jangchwisa.trainingservice.training.session.entity.TrainingSession;
import com.jangchwisa.trainingservice.training.session.entity.TrainingSessionStatus;
import com.jangchwisa.trainingservice.training.session.entity.TrainingType;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcTrainingSessionRepository implements TrainingSessionRepository {

    private static final RowMapper<TrainingSession> ROW_MAPPER = (resultSet, rowNumber) -> new TrainingSession(
            resultSet.getLong("session_id"),
            resultSet.getLong("user_id"),
            TrainingType.valueOf(resultSet.getString("training_type")),
            resultSet.getString("sub_type"),
            nullableLong(resultSet.getObject("scenario_id")),
            TrainingSessionStatus.valueOf(resultSet.getString("status")),
            resultSet.getInt("current_step"),
            resultSet.getTimestamp("started_at").toLocalDateTime(),
            resultSet.getTimestamp("ended_at") == null
                    ? null
                    : resultSet.getTimestamp("ended_at").toLocalDateTime()
    );

    private final JdbcTemplate jdbcTemplate;

    public JdbcTrainingSessionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public TrainingSession save(TrainingSession trainingSession) {
        String sql = """
                INSERT INTO training_sessions (
                    user_id,
                    training_type,
                    sub_type,
                    scenario_id,
                    status,
                    current_step,
                    started_at,
                    ended_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, trainingSession.userId());
            statement.setString(2, trainingSession.trainingType().name());
            statement.setString(3, trainingSession.subType());
            if (trainingSession.scenarioId() == null) {
                statement.setObject(4, null);
            } else {
                statement.setLong(4, trainingSession.scenarioId());
            }
            statement.setString(5, trainingSession.status().name());
            statement.setInt(6, trainingSession.currentStep());
            statement.setTimestamp(7, Timestamp.valueOf(trainingSession.startedAt()));
            statement.setTimestamp(8, trainingSession.endedAt() == null
                    ? null
                    : Timestamp.valueOf(trainingSession.endedAt()));
            return statement;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new TrainingServiceException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to create training session.");
        }

        return trainingSession.withSessionId(key.longValue());
    }

    @Override
    public Optional<TrainingSession> findById(long sessionId) {
        String sql = """
                SELECT session_id, user_id, training_type, sub_type, scenario_id, status,
                       current_step, started_at, ended_at
                FROM training_sessions
                WHERE session_id = ?
                """;
        List<TrainingSession> sessions = jdbcTemplate.query(sql, ROW_MAPPER, sessionId);

        if (sessions.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(sessions.getFirst());
    }

    @Override
    public void update(TrainingSession trainingSession) {
        String sql = """
                UPDATE training_sessions
                SET status = ?,
                    current_step = ?,
                    ended_at = ?
                WHERE session_id = ?
                """;
        int updatedRows = jdbcTemplate.update(
                sql,
                trainingSession.status().name(),
                trainingSession.currentStep(),
                trainingSession.endedAt() == null ? null : Timestamp.valueOf(trainingSession.endedAt()),
                trainingSession.sessionId()
        );

        if (updatedRows == 0) {
            throw new TrainingServiceException(ErrorCode.NOT_FOUND, "Training session was not found.");
        }
    }

    private static Long nullableLong(Object value) {
        if (value == null) {
            return null;
        }
        return ((Number) value).longValue();
    }
}
