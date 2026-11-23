package com.findback.dao;

import com.findback.config.DatabaseConnection;
import com.findback.model.Handover;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HandoverDao {
    private static final Logger log = LoggerFactory.getLogger(HandoverDao.class);

    private static final String BASE_SELECT =
            "SELECT h.*, " +
            "u1.full_name AS handed_by_name, u2.full_name AS receiver_name, u3.full_name AS witness_name, " +
            "lr.title AS lost_title, fr.title AS found_title " +
            "FROM handovers h " +
            "LEFT JOIN users u1 ON h.handed_by_id = u1.id " +
            "LEFT JOIN users u2 ON h.received_by_id = u2.id " +
            "LEFT JOIN users u3 ON h.staff_witness_id = u3.id " +
            "LEFT JOIN item_reports lr ON h.lost_report_id = lr.id " +
            "LEFT JOIN item_reports fr ON h.found_report_id = fr.id ";

    public Optional<Handover> findById(Long id) {
        String sql = BASE_SELECT + "WHERE h.id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Error finding handover by id: {}", id, e);
        }
        return Optional.empty();
    }

    public Optional<Handover> findByReceiptCode(String code) {
        String sql = BASE_SELECT + "WHERE h.receipt_code = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Error finding handover by receipt code: {}", code, e);
        }
        return Optional.empty();
    }

    public List<Handover> findByUserId(Long userId) {
        List<Handover> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE h.received_by_id = ? OR h.handed_by_id = ? ORDER BY h.handover_date DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Error finding handovers by user: {}", userId, e);
        }
        return list;
    }

    public List<Handover> findAll() {
        List<Handover> list = new ArrayList<>();
        String sql = BASE_SELECT + "ORDER BY h.handover_date DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            log.error("Error finding all handovers", e);
        }
        return list;
    }

    public Long save(Handover h) {
        String sql = "INSERT INTO handovers (claim_id, lost_report_id, found_report_id, handed_by_id, received_by_id, staff_witness_id, handover_date, venue, remarks, receipt_code, completed, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, h.getClaimId());
            ps.setLong(2, h.getLostReportId());
            ps.setLong(3, h.getFoundReportId());
            if (h.getHandedById() != null) ps.setLong(4, h.getHandedById()); else ps.setNull(4, Types.BIGINT);
            ps.setLong(5, h.getReceivedById());
            ps.setLong(6, h.getStaffWitnessId());
            ps.setTimestamp(7, h.getHandoverDate() != null ? Timestamp.valueOf(h.getHandoverDate()) : new Timestamp(System.currentTimeMillis()));
            ps.setString(8, h.getVenue());
            ps.setString(9, h.getRemarks());
            ps.setString(10, h.getReceiptCode());
            ps.setBoolean(11, h.isCompleted());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            log.error("Error saving handover", e);
        }
        return null;
    }

    public long countCompleted() {
        String sql = "SELECT COUNT(*) FROM handovers WHERE completed = TRUE";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            log.error("Error counting completed handovers", e);
        }
        return 0;
    }

    private Handover mapRow(ResultSet rs) throws SQLException {
        Handover h = new Handover();
        h.setId(rs.getLong("id"));
        h.setClaimId(rs.getLong("claim_id"));
        h.setLostReportId(rs.getLong("lost_report_id"));
        h.setFoundReportId(rs.getLong("found_report_id"));
        long hb = rs.getLong("handed_by_id");
        if (!rs.wasNull()) h.setHandedById(hb);
        h.setReceivedById(rs.getLong("received_by_id"));
        h.setStaffWitnessId(rs.getLong("staff_witness_id"));
        Timestamp hd = rs.getTimestamp("handover_date");
        if (hd != null) h.setHandoverDate(hd.toLocalDateTime());
        h.setVenue(rs.getString("venue"));
        h.setRemarks(rs.getString("remarks"));
        h.setReceiptCode(rs.getString("receipt_code"));
        h.setCompleted(rs.getBoolean("completed"));
        Timestamp ct = rs.getTimestamp("created_at");
        if (ct != null) h.setCreatedAt(ct.toLocalDateTime());

        try { h.setHandedByName(rs.getString("handed_by_name")); } catch (Exception ignore) {}
        try { h.setReceiverName(rs.getString("receiver_name")); } catch (Exception ignore) {}
        try { h.setWitnessName(rs.getString("witness_name")); } catch (Exception ignore) {}
        try { h.setLostTitle(rs.getString("lost_title")); } catch (Exception ignore) {}
        try { h.setFoundTitle(rs.getString("found_title")); } catch (Exception ignore) {}

        return h;
    }
}
