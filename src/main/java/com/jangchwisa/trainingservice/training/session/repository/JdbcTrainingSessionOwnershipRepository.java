package com.jangchwisa.trainingservice.training.session.repository;

import java.util.List;
import java.util.OptionalLong;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcTrainingSessionOwnershipRepository implements TrainingSessionOwnershipRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcTrainingSessionOwnershipRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public OptionalLong findUserIdBySessionId(long sessionId) {
        String sql = """
                SELECT user_id
                FROM training_sessions
                WHERE session_id = ?
                """;
        List<Long> userIds = jdbcTemplate.queryForList(sql, Long.class, sessionId);

        if (userIds.isEmpty()) {
            return OptionalLong.empty();
        }

        return OptionalLong.of(userIds.getFirst());
    }
}
