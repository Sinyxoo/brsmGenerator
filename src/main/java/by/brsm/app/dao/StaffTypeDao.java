package by.brsm.app.dao;

import by.brsm.app.model.StaffType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Доступ к справочнику типов штабов.
 */
public class StaffTypeDao {

    private final DatabaseManager databaseManager;

    public StaffTypeDao(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public List<StaffType> findAll() throws SQLException {
        List<StaffType> list = new ArrayList<>();
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT id, short_name, full_name FROM staff_types ORDER BY short_name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                StaffType type = new StaffType();
                type.setId(rs.getInt("id"));
                type.setShortName(rs.getString("short_name"));
                type.setFullName(rs.getString("full_name"));
                list.add(type);
            }
        }
        return list;
    }

    public StaffType insert(StaffType type) throws SQLException {
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO staff_types(short_name, full_name) VALUES (?, ?)",
                     PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, type.getShortName());
            ps.setString(2, type.getFullName());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    type.setId(keys.getInt(1));
                }
            }
        }
        return type;
    }

    public void update(StaffType type) throws SQLException {
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE staff_types SET short_name = ?, full_name = ? WHERE id = ?")) {
            ps.setString(1, type.getShortName());
            ps.setString(2, type.getFullName());
            ps.setInt(3, type.getId());
            ps.executeUpdate();
        }
    }
}
