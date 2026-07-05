package by.brsm.app.dao;

import by.brsm.app.model.Administrator;
import by.brsm.app.model.Protocol;
import by.brsm.app.util.JsonUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Доступ к таблице протоколов.
 */
public class ProtocolDao {

    private final DatabaseManager databaseManager;
    private final AdministratorDao administratorDao;

    public ProtocolDao(DatabaseManager databaseManager, AdministratorDao administratorDao) {
        this.databaseManager = databaseManager;
        this.administratorDao = administratorDao;
    }

    public List<Protocol> findAllSummaries() throws SQLException {
        List<Protocol> list = new ArrayList<>();
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("""
                     SELECT p.*,
                            (SELECT COUNT(*) FROM agenda_items ai WHERE ai.protocol_id = p.id) AS item_count
                     FROM protocols p
                     ORDER BY p.date DESC, p.number DESC
                     """);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Protocol protocol = mapBase(rs);
                protocol.setItems(new ArrayList<>());
                list.add(protocol);
            }
        }
        return list;
    }

    public Optional<Protocol> findById(int id) throws SQLException {
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM protocols WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapBase(rs)) : Optional.empty();
            }
        }
    }

    public boolean existsByNumber(int number, Integer excludeId) throws SQLException {
        String sql = excludeId == null
                ? "SELECT 1 FROM protocols WHERE number = ?"
                : "SELECT 1 FROM protocols WHERE number = ? AND id <> ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, number);
            if (excludeId != null) {
                ps.setInt(2, excludeId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public int findMaxNumber() throws SQLException {
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT COALESCE(MAX(number), 0) FROM protocols");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public Protocol insert(Protocol protocol, List<Integer> attendeeIds) throws SQLException {
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     """
                             INSERT INTO protocols(number, date, chairman_id, meeting_secretary_id,
                                                   attendees_json, default_votes_for, file_path)
                             VALUES (?, ?, ?, ?, ?, ?, ?)
                             """,
                     PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, protocol.getNumber());
            ps.setString(2, protocol.getDate().toString());
            ps.setInt(3, protocol.getChairman().getId());
            ps.setInt(4, protocol.getMeetingSecretary().getId());
            ps.setString(5, JsonUtils.attendeeIdsToJson(attendeeIds));
            ps.setInt(6, protocol.getDefaultVotesFor());
            ps.setString(7, protocol.getFilePath());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    protocol.setId(keys.getInt(1));
                }
            }
        }
        return protocol;
    }

    public void updateFilePath(int protocolId, String filePath) throws SQLException {
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE protocols SET file_path = ? WHERE id = ?")) {
            ps.setString(1, filePath);
            ps.setInt(2, protocolId);
            ps.executeUpdate();
        }
    }

    private Protocol mapBase(ResultSet rs) throws SQLException {
        Protocol protocol = new Protocol();
        protocol.setId(rs.getInt("id"));
        protocol.setNumber(rs.getInt("number"));
        protocol.setDate(LocalDate.parse(rs.getString("date")));
        protocol.setDefaultVotesFor(rs.getInt("default_votes_for"));
        protocol.setFilePath(rs.getString("file_path"));

        int chairmanId = rs.getInt("chairman_id");
        int secretaryId = rs.getInt("meeting_secretary_id");
        administratorDao.findById(chairmanId).ifPresent(protocol::setChairman);
        administratorDao.findById(secretaryId).ifPresent(protocol::setMeetingSecretary);
        return protocol;
    }
}
