package by.brsm.app.dao;

import by.brsm.app.model.AdminRole;
import by.brsm.app.model.Administrator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Доступ к справочнику администрации комитета.
 */
public class AdministratorDao {

    private final DatabaseManager databaseManager;

    public AdministratorDao(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public List<Administrator> findAllActive() throws SQLException {
        return findAll(false);
    }

    public List<Administrator> findAll(boolean includeInactive) throws SQLException {
        List<Administrator> list = new ArrayList<>();
        String sql = includeInactive
                ? "SELECT * FROM administrators ORDER BY short_name"
                : "SELECT * FROM administrators WHERE active = 1 ORDER BY short_name";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public Optional<Administrator> findById(int id) throws SQLException {
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM administrators WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public Optional<Administrator> findByRole(AdminRole role) throws SQLException {
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT * FROM administrators WHERE role = ? AND active = 1 LIMIT 1")) {
            ps.setString(1, role.name());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public Administrator insert(Administrator admin) throws SQLException {
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO administrators(full_name_genitive, short_name, role, active) VALUES (?,?,?,?)",
                     PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, admin.getFullNameGenitive());
            ps.setString(2, admin.getShortName());
            ps.setString(3, admin.getRole().name());
            ps.setInt(4, admin.isActive() ? 1 : 0);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    admin.setId(keys.getInt(1));
                }
            }
        }
        return admin;
    }

    public void update(Administrator admin) throws SQLException {
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE administrators SET full_name_genitive=?, short_name=?, role=?, active=? WHERE id=?")) {
            ps.setString(1, admin.getFullNameGenitive());
            ps.setString(2, admin.getShortName());
            ps.setString(3, admin.getRole().name());
            ps.setInt(4, admin.isActive() ? 1 : 0);
            ps.setInt(5, admin.getId());
            ps.executeUpdate();
        }
    }

    public void deactivate(int id) throws SQLException {
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE administrators SET active = 0 WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Administrator map(ResultSet rs) throws SQLException {
        Administrator a = new Administrator();
        a.setId(rs.getInt("id"));
        a.setFullNameGenitive(rs.getString("full_name_genitive"));
        a.setShortName(rs.getString("short_name"));
        a.setRole(AdminRole.valueOf(rs.getString("role")));
        a.setActive(rs.getInt("active") == 1);
        return a;
    }
}
