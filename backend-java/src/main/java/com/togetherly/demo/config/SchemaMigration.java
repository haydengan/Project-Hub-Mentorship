package com.togetherly.demo.config;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * One-time schema migrations that Hibernate hbm2ddl=update cannot handle
 * (dropping constraints, etc.). Each migration is idempotent.
 */
@Component
public class SchemaMigration implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SchemaMigration.class);
    private final DataSource dataSource;

    public SchemaMigration(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            dropActivityLogUniqueConstraint(conn);
            addTotalMinutesColumn(conn);
        }
    }

    private void dropActivityLogUniqueConstraint(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            // Find unique constraint on activity_log table
            ResultSet rs = stmt.executeQuery(
                    "SELECT constraint_name FROM information_schema.table_constraints " +
                    "WHERE table_name = 'activity_log' AND constraint_type = 'UNIQUE' LIMIT 1");
            if (rs.next()) {
                String constraintName = rs.getString(1);
                stmt.execute("ALTER TABLE activity_log DROP CONSTRAINT " + constraintName);
                log.info("Dropped unique constraint '{}' from activity_log", constraintName);
            }
        } catch (Exception e) {
            // Constraint may already be dropped — safe to ignore
            log.debug("activity_log constraint migration skipped: {}", e.getMessage());
        }
    }

    private void addTotalMinutesColumn(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(
                    "SELECT 1 FROM information_schema.columns " +
                    "WHERE table_name = 'streak' AND column_name = 'total_minutes'");
            if (!rs.next()) {
                stmt.execute("ALTER TABLE streak ADD COLUMN total_minutes BIGINT NOT NULL DEFAULT 0");
                log.info("Added total_minutes column to streak table");
            }
        } catch (Exception e) {
            log.debug("streak total_minutes migration skipped: {}", e.getMessage());
        }
    }
}
