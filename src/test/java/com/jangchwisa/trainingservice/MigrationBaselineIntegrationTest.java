package com.jangchwisa.trainingservice;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationState;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class MigrationBaselineIntegrationTest extends AbstractMySqlIntegrationTest {

    @Autowired
    Flyway flyway;

    @Autowired
    DataSource dataSource;

    @Test
    void migrationsRunAgainstEmptyMySqlDatabase() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metadata = connection.getMetaData();

            assertThat(metadata.getDatabaseProductName()).containsIgnoringCase("MySQL");
            assertThat(flywaySchemaHistoryExists(connection)).isTrue();
            assertThat(tableExists(connection, "training_sessions")).isTrue();
            assertThat(tableExists(connection, "training_scores")).isTrue();
            assertThat(tableExists(connection, "training_feedbacks")).isTrue();
            assertThat(tableExists(connection, "training_session_summaries")).isTrue();
            assertThat(tableExists(connection, "outbox_events")).isTrue();
            assertThat(tableExists(connection, "social_scenarios")).isTrue();
            assertThat(tableExists(connection, "social_dialog_logs")).isTrue();
            assertThat(tableExists(connection, "user_social_progress")).isTrue();
            assertThat(tableExists(connection, "safety_scenarios")).isTrue();
            assertThat(tableExists(connection, "safety_scenes")).isTrue();
            assertThat(tableExists(connection, "safety_choices")).isTrue();
            assertThat(tableExists(connection, "safety_action_logs")).isTrue();
            assertThat(tableExists(connection, "user_safety_progress")).isTrue();
            assertThat(tableExists(connection, "focus_level_rules")).isTrue();
            assertThat(tableExists(connection, "focus_commands")).isTrue();
            assertThat(tableExists(connection, "focus_reaction_logs")).isTrue();
            assertThat(tableExists(connection, "user_focus_progress")).isTrue();
            assertThat(tableExists(connection, "document_questions")).isTrue();
            assertThat(tableExists(connection, "document_question_choices")).isTrue();
            assertThat(tableExists(connection, "document_session_questions")).isTrue();
            assertThat(tableExists(connection, "document_answer_logs")).isTrue();
            assertThat(tableExists(connection, "user_document_progress")).isTrue();
            assertThat(columnExists(connection, "social_scenarios", "seed_code")).isTrue();
            assertThat(columnExists(connection, "social_scenarios", "evaluation_point")).isTrue();
            assertThat(columnExists(connection, "safety_choices", "result_text")).isTrue();
            assertThat(columnExists(connection, "document_questions", "correct_feedback")).isTrue();
            assertThat(contentTableUserIdColumnCount(connection)).isZero();
            assertThat(userIdForeignKeyCount(connection)).isZero();
        }

        MigrationInfo[] migrations = flyway.info().all();

        assertThat(migrations)
                .allSatisfy(migration -> assertThat(migration.getState())
                        .isNotEqualTo(MigrationState.FAILED));
    }

    private boolean flywaySchemaHistoryExists(Connection connection) throws Exception {
        String sql = """
                SELECT COUNT(*)
                FROM information_schema.tables
                WHERE table_schema = DATABASE()
                  AND table_name = 'flyway_schema_history'
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getInt(1) == 1;
        }
    }

    private boolean tableExists(Connection connection, String tableName) throws Exception {
        String sql = """
                SELECT COUNT(*)
                FROM information_schema.tables
                WHERE table_schema = DATABASE()
                  AND table_name = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, tableName);

            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1) == 1;
            }
        }
    }

    private boolean columnExists(Connection connection, String tableName, String columnName) throws Exception {
        String sql = """
                SELECT COUNT(*)
                FROM information_schema.columns
                WHERE table_schema = DATABASE()
                  AND table_name = ?
                  AND column_name = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, tableName);
            statement.setString(2, columnName);

            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1) == 1;
            }
        }
    }

    private int userIdForeignKeyCount(Connection connection) throws Exception {
        String sql = """
                SELECT COUNT(*)
                FROM information_schema.key_column_usage
                WHERE table_schema = DATABASE()
                  AND column_name = 'user_id'
                  AND referenced_table_name IS NOT NULL
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getInt(1);
        }
    }

    private int contentTableUserIdColumnCount(Connection connection) throws Exception {
        String sql = """
                SELECT COUNT(*)
                FROM information_schema.columns
                WHERE table_schema = DATABASE()
                  AND column_name = 'user_id'
                  AND table_name IN (
                      'social_scenarios',
                      'safety_scenarios',
                      'safety_scenes',
                      'safety_choices',
                      'focus_level_rules',
                      'document_questions',
                      'document_question_choices'
                  )
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getInt(1);
        }
    }
}
