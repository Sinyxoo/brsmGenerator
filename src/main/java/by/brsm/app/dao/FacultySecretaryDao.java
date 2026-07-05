package by.brsm.app.dao;

import by.brsm.app.model.Faculty;
import by.brsm.app.model.FacultySecretary;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Доступ к справочнику секретарей факультетов.
 */
public class FacultySecretaryDao {

    private final DatabaseManager databaseManager;
    private final FacultyDao facultyDao;

    public FacultySecretaryDao(DatabaseManager databaseManager, FacultyDao facultyDao) {
        this.databaseManager = databaseManager;
        this.facultyDao = facultyDao;
    }

    public List<FacultySecretary> findAllActive() throws SQLException {
        return findAll(false);
    }

    public List<FacultySecretary> findAll(boolean includeInactive) throws SQLException {
        List<FacultySecretary> list = new ArrayList<>();
        String sql = includeInactive
                ? """
                SELECT fs.*, f.short_name AS faculty_short, f.full_name AS faculty_full
                FROM faculty_secretaries fs
                JOIN faculties f ON f.id = fs.faculty_id
                ORDER BY fs.short_name
                """
                : """
                SELECT fs.*, f.short_name AS faculty_short, f.full_name AS faculty_full
                FROM faculty_secretaries fs
                JOIN faculties f ON f.id = fs.faculty_id
                WHERE fs.active = 1
                ORDER BY fs.short_name
                """;
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public Optional<FacultySecretary> findByFacultyId(int facultyId) throws SQLException {
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("""
                     SELECT fs.*, f.short_name AS faculty_short, f.full_name AS faculty_full
                     FROM faculty_secretaries fs
                     JOIN faculties f ON f.id = fs.faculty_id
                     WHERE fs.faculty_id = ? AND fs.active = 1 LIMIT 1
                     """)) {
            ps.setInt(1, facultyId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public Optional<FacultySecretary> findById(int id) throws SQLException {
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("""
                     SELECT fs.*, f.short_name AS faculty_short, f.full_name AS faculty_full
                     FROM faculty_secretaries fs
                     JOIN faculties f ON f.id = fs.faculty_id
                     WHERE fs.id = ?
                     """)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public FacultySecretary insert(FacultySecretary secretary) throws SQLException {
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO faculty_secretaries(full_name_genitive, short_name, faculty_id, active) VALUES (?,?,?,?)",
                     PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, secretary.getFullNameGenitive());
            ps.setString(2, secretary.getShortName());
            ps.setInt(3, secretary.getFaculty().getId());
            ps.setInt(4, secretary.isActive() ? 1 : 0);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    secretary.setId(keys.getInt(1));
                }
            }
        }
        return secretary;
    }

    public void update(FacultySecretary secretary) throws SQLException {
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE faculty_secretaries SET full_name_genitive=?, short_name=?, faculty_id=?, active=? WHERE id=?")) {
            ps.setString(1, secretary.getFullNameGenitive());
            ps.setString(2, secretary.getShortName());
            ps.setInt(3, secretary.getFaculty().getId());
            ps.setInt(4, secretary.isActive() ? 1 : 0);
            ps.setInt(5, secretary.getId());
            ps.executeUpdate();
        }
    }

    public void deactivate(int id) throws SQLException {
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE faculty_secretaries SET active = 0 WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private FacultySecretary map(ResultSet rs) throws SQLException {
        FacultySecretary s = new FacultySecretary();
        s.setId(rs.getInt("id"));
        s.setFullNameGenitive(rs.getString("full_name_genitive"));
        s.setShortName(rs.getString("short_name"));
        s.setActive(rs.getInt("active") == 1);
        Faculty faculty = new Faculty();
        faculty.setId(rs.getInt("faculty_id"));
        faculty.setShortName(rs.getString("faculty_short"));
        faculty.setFullName(rs.getString("faculty_full"));
        s.setFaculty(faculty);
        return s;
    }
}
