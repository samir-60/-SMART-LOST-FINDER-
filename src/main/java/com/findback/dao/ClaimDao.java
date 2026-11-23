package com.findback.dao;

import com.findback.config.DatabaseConnection;
import com.findback.enums.ClaimStatus;
import com.findback.model.Claim;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClaimDao {
    private static final Logger log = LoggerFactory.getLogger(ClaimDao.class);

    private static final String BASE_SELECT =
            "SELECT c.*, u.full_name AS claimant_name, u.phone_number AS claimant_phone, " +
            "lr.title AS lost_title, fr.title AS found_title, " +
            "fu.full_name AS finder_name, fu.phone_number AS finder_phone " +
            "FROM claims c " +
            "LEFT JOIN users u ON c.claimant_id = u.id " +
            "LEFT JOIN item_reports lr ON c.lost_report_id = lr.id " +
            "LEFT JOIN item_reports fr ON c.found_report_id = fr.id " +
            "LEFT JOIN users fu ON fr.user_id = fu.id ";

    public Optional<Claim> findById(Long id) {
        String sql = BASE_SELECT + "WHERE c.id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Error finding claim by id: {}", id, e);
        }
        return Optional.empty();
    }

    public List<Claim> findByClaimantId(Long claimantId) {
        List<Claim> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE c.claimant_id = ? ORDER BY c.created_at DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, claimantId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Error finding claims by claimant id: {}", claimantId, e);
        }
        return list;
    }

    public List<Claim> findAll() {
        List<Claim> list = new ArrayList<>();
        String sql = BASE_SELECT + "ORDER BY c.created_at DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            log.error("Error finding all claims", e);
        }
        return list;
    }

    public List<Claim> findByStatus(ClaimStatus status) {
        List<Claim> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE c.status = ? ORDER BY c.created_at DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status != null ? status.name() : "");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Error finding claims by status", e);
        }
        return list;
    }

    public Long save(Claim c) {
        String sql = "INSERT INTO claims (claimant_id, lost_report_id, found_report_id, match_id, proof_description, proof_image_path, status, admin_remarks, reviewed_by_id, reviewed_at, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, c.getClaimantId());
            ps.setLong(2, c.getLostReportId());
            ps.setLong(3, c.getFoundReportId());
            if (c.getMatchId() != null) ps.setLong(4, c.getMatchId()); else ps.setNull(4, Types.BIGINT);
            ps.setString(5, c.getProofDescription());
            ps.setString(6, c.getProofImagePath());
            ps.setString(7, c.getStatus() != null ? c.getStatus().name() : ClaimStatus.PENDING.name());
            ps.setString(8, c.getAdminRemarks());
            if (c.getReviewedById() != null) ps.setLong(9, c.getReviewedById()); else ps.setNull(9, Types.BIGINT);
            if (c.getReviewedAt() != null) ps.setTimestamp(10, Timestamp.valueOf(c.getReviewedAt())); else ps.setNull(10, Types.TIMESTAMP);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            log.error("Error saving claim", e);
        }
        return null;
    }

    public void update(Claim c) {
        String sql = "UPDATE claims SET status = ?, admin_remarks = ?, reviewed_by_id = ?, reviewed_at = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getStatus() != null ? c.getStatus().name() : ClaimStatus.PENDING.name());
            ps.setString(2, c.getAdminRemarks());
            if (c.getReviewedById() != null) ps.setLong(3, c.getReviewedById()); else ps.setNull(3, Types.BIGINT);
            if (c.getReviewedAt() != null) ps.setTimestamp(4, Timestamp.valueOf(c.getReviewedAt())); else ps.setNull(4, Types.TIMESTAMP);
            ps.setLong(5, c.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Error updating claim: {}", c.getId(), e);
        }
    }

    public long countByStatus(ClaimStatus status) {
        String sql = "SELECT COUNT(*) FROM claims WHERE status = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status != null ? status.name() : "");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            log.error("Error counting claims by status", e);
        }
        return 0;
    }

    private Claim mapRow(ResultSet rs) throws SQLException {
        Claim c = new Claim();
        c.setId(rs.getLong("id"));
        c.setClaimantId(rs.getLong("claimant_id"));
        c.setLostReportId(rs.getLong("lost_report_id"));
        c.setFoundReportId(rs.getLong("found_report_id"));
        long mId = rs.getLong("match_id");
        if (!rs.wasNull()) c.setMatchId(mId);
        c.setProofDescription(rs.getString("proof_description"));
        c.setProofImagePath(rs.getString("proof_image_path"));
        String st = rs.getString("status");
        if (st != null) {
            try { c.setStatus(ClaimStatus.valueOf(st)); } catch (Exception e) { c.setStatus(ClaimStatus.PENDING); }
        }
        c.setAdminRemarks(rs.getString("admin_remarks"));
        long rb = rs.getLong("reviewed_by_id");
        if (!rs.wasNull()) c.setReviewedById(rb);
        Timestamp ra = rs.getTimestamp("reviewed_at");
        if (ra != null) c.setReviewedAt(ra.toLocalDateTime());
        Timestamp ct = rs.getTimestamp("created_at");
        if (ct != null) c.setCreatedAt(ct.toLocalDateTime());
        Timestamp ut = rs.getTimestamp("updated_at");
        if (ut != null) c.setUpdatedAt(ut.toLocalDateTime());

        try { c.setClaimantName(rs.getString("claimant_name")); } catch (Exception ignore) {}
        try { c.setClaimantPhone(rs.getString("claimant_phone")); } catch (Exception ignore) {}
        try { c.setLostTitle(rs.getString("lost_title")); } catch (Exception ignore) {}
        try { c.setFoundTitle(rs.getString("found_title")); } catch (Exception ignore) {}
        try { c.setFinderName(rs.getString("finder_name")); } catch (Exception ignore) {}
        try { c.setFinderPhone(rs.getString("finder_phone")); } catch (Exception ignore) {}

        return c;
    }
}
