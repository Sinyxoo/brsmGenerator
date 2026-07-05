package by.brsm.app.dao;

import by.brsm.app.model.AgendaItem;
import by.brsm.app.model.AgendaTypeCode;
import by.brsm.app.util.JsonUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Доступ к пунктам повестки дня.
 */
public class AgendaItemDao {

    private final DatabaseManager databaseManager;

    public AgendaItemDao(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public List<AgendaItem> findByProtocolId(int protocolId) throws SQLException {
        List<AgendaItem> list = new ArrayList<>();
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT * FROM agenda_items WHERE protocol_id = ? ORDER BY order_index")) {
            ps.setInt(1, protocolId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    public void insertAll(int protocolId, List<AgendaItem> items) throws SQLException {
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     """
                             INSERT INTO agenda_items(protocol_id, order_index, type_code, fields_json,
                                                    speaker_ref, supporter_ref, votes_for,
                                                    requires_resolution, resolution_number, resolution_file_path)
                             VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                             """)) {
            for (AgendaItem item : items) {
                ps.setInt(1, protocolId);
                ps.setInt(2, item.getOrderIndex());
                ps.setString(3, item.getType().name());
                ps.setString(4, JsonUtils.toJson(item.getFields()));
                ps.setString(5, item.getSpeaker() != null ? "person:" + item.getSpeaker().getId() : null);
                ps.setString(6, item.getSupporter() != null ? "person:" + item.getSupporter().getId() : null);
                ps.setInt(7, item.getVotesFor());
                ps.setInt(8, item.isRequiresResolution() ? 1 : 0);
                if (item.getResolutionNumber() != null) {
                    ps.setInt(9, item.getResolutionNumber());
                } else {
                    ps.setNull(9, java.sql.Types.INTEGER);
                }
                ps.setString(10, item.getResolutionFilePath());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public void updateResolutionInfo(int itemId, Integer resolutionNumber, String filePath) throws SQLException {
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE agenda_items SET resolution_number = ?, resolution_file_path = ? WHERE id = ?")) {
            if (resolutionNumber != null) {
                ps.setInt(1, resolutionNumber);
            } else {
                ps.setNull(1, java.sql.Types.INTEGER);
            }
            ps.setString(2, filePath);
            ps.setInt(3, itemId);
            ps.executeUpdate();
        }
    }

    private AgendaItem map(ResultSet rs) throws SQLException {
        AgendaItem item = new AgendaItem();
        item.setId(rs.getInt("id"));
        item.setProtocolId(rs.getInt("protocol_id"));
        item.setOrderIndex(rs.getInt("order_index"));
        item.setType(AgendaTypeCode.valueOf(rs.getString("type_code")));
        item.setFields(JsonUtils.mapFromJson(rs.getString("fields_json")));
        item.setVotesFor(rs.getInt("votes_for"));
        item.setRequiresResolution(rs.getInt("requires_resolution") == 1);
        int resolutionNumber = rs.getInt("resolution_number");
        if (!rs.wasNull()) {
            item.setResolutionNumber(resolutionNumber);
        }
        item.setResolutionFilePath(rs.getString("resolution_file_path"));
        return item;
    }
}
