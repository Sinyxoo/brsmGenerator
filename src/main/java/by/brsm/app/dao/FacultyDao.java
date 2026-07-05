package by.brsm.app.dao;

import by.brsm.app.model.Faculty;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Доступ к справочнику факультетов.
 */
public class FacultyDao {

    private final DatabaseManager databaseManager;

    public FacultyDao(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public List<Faculty> findAll() throws SQLException {
        List<Faculty> list = new ArrayList<>();
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT id, short_name, full_name FROM faculties ORDER BY short_name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public Optional<Faculty> findById(int id) throws SQLException {
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT id, short_name, full_name FROM faculties WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public Faculty insert(Faculty faculty) throws SQLException {
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO faculties(short_name, full_name) VALUES (?, ?)",
                     PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, faculty.getShortName());
            ps.setString(2, faculty.getFullName());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    faculty.setId(keys.getInt(1));
                }
            }
        }
        return faculty;
    }

    public void update(Faculty faculty) throws SQLException {
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE faculties SET short_name = ?, full_name = ? WHERE id = ?")) {
            ps.setString(1, faculty.getShortName());
            ps.setString(2, faculty.getFullName());
            ps.setInt(3, faculty.getId());
            ps.executeUpdate();
        }
    }

    private Faculty map(ResultSet rs) throws SQLException {
        Faculty f = new Faculty();
        f.setId(rs.getInt("id"));
        f.setShortName(rs.getString("short_name"));
        f.setFullName(rs.getString("full_name"));
        return f;
    }
}
