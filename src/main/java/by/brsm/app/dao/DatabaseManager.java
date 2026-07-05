package by.brsm.app.dao;

import by.brsm.app.util.AppPaths;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Инициализация SQLite и выдача соединений.
 */
public class DatabaseManager {

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Драйвер SQLite не найден", e);
        }
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + AppPaths.getDatabasePath());
    }

    /** Создаёт таблицы при первом запуске. */
    public void initializeSchema() throws SQLException {
        try (Connection conn = getConnection(); Statement st = conn.createStatement()) {
            st.execute("""
                    CREATE TABLE IF NOT EXISTS faculties (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        short_name TEXT UNIQUE NOT NULL,
                        full_name TEXT
                    )
                    """);
            st.execute("""
                    CREATE TABLE IF NOT EXISTS faculty_secretaries (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        full_name_genitive TEXT NOT NULL,
                        short_name TEXT NOT NULL,
                        faculty_id INTEGER NOT NULL REFERENCES faculties(id),
                        active INTEGER DEFAULT 1
                    )
                    """);
            st.execute("""
                    CREATE TABLE IF NOT EXISTS administrators (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        full_name_genitive TEXT NOT NULL,
                        short_name TEXT NOT NULL,
                        role TEXT NOT NULL CHECK(role IN ('SECRETARY','DEPUTY_SECRETARY')),
                        active INTEGER DEFAULT 1
                    )
                    """);
            st.execute("""
                    CREATE TABLE IF NOT EXISTS staff_types (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        short_name TEXT NOT NULL,
                        full_name TEXT NOT NULL
                    )
                    """);
            st.execute("""
                    CREATE TABLE IF NOT EXISTS protocols (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        number INTEGER UNIQUE NOT NULL,
                        date TEXT NOT NULL,
                        chairman_id INTEGER REFERENCES administrators(id),
                        meeting_secretary_id INTEGER REFERENCES administrators(id),
                        attendees_json TEXT,
                        default_votes_for INTEGER DEFAULT 17,
                        file_path TEXT,
                        created_at TEXT DEFAULT CURRENT_TIMESTAMP
                    )
                    """);
            st.execute("""
                    CREATE TABLE IF NOT EXISTS agenda_items (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        protocol_id INTEGER NOT NULL REFERENCES protocols(id) ON DELETE CASCADE,
                        order_index INTEGER NOT NULL,
                        type_code TEXT NOT NULL,
                        fields_json TEXT,
                        speaker_ref TEXT,
                        supporter_ref TEXT,
                        votes_for INTEGER,
                        requires_resolution INTEGER DEFAULT 1,
                        resolution_number INTEGER,
                        resolution_file_path TEXT
                    )
                    """);
            st.execute("""
                    CREATE TABLE IF NOT EXISTS counters (
                        key TEXT PRIMARY KEY,
                        value INTEGER NOT NULL
                    )
                    """);
        }
    }
}
