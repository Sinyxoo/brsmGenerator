package by.brsm.app.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Сквозные счётчики номеров протоколов и постановлений.
 */
public class CounterDao {

    private final DatabaseManager databaseManager;

    public CounterDao(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public int getValue(String key) throws SQLException {
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT value FROM counters WHERE key = ?")) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("value") : 0;
            }
        }
    }

    public void setValue(String key, int value) throws SQLException {
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO counters(key, value) VALUES (?, ?) ON CONFLICT(key) DO UPDATE SET value = excluded.value")) {
            ps.setString(1, key);
            ps.setInt(2, value);
            ps.executeUpdate();
        }
    }

    /** Атомарно увеличивает счётчик и возвращает новое значение. */
    public int next(String key) throws SQLException {
        try (Connection conn = databaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int current;
                try (PreparedStatement select = conn.prepareStatement("SELECT value FROM counters WHERE key = ?")) {
                    select.setString(1, key);
                    try (ResultSet rs = select.executeQuery()) {
                        current = rs.next() ? rs.getInt("value") : 0;
                    }
                }
                int next = current + 1;
                try (PreparedStatement upsert = conn.prepareStatement(
                        "INSERT INTO counters(key, value) VALUES (?, ?) ON CONFLICT(key) DO UPDATE SET value = excluded.value")) {
                    upsert.setString(1, key);
                    upsert.setInt(2, next);
                    upsert.executeUpdate();
                }
                conn.commit();
                return next;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }
}
