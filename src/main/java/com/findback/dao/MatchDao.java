package com.findback.dao;

import com.findback.config.DatabaseConnection;
import com.findback.enums.MatchStatus;
import com.findback.model.Match;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MatchDao {
    private static final Logger log = LoggerFactory.getLogger(MatchDao.class);

    private static final String BASE_SELECT =
            "SELECT m.*, " +
            "lr.title AS lost_title, lr.color AS lost_color, lr.image_path AS lost_image, lc.name AS lost_category, " +
            "fr.title AS found_title, fr.color AS found_color, fr.image_path AS found_image, fc.name AS found_category, " +
            "fl.name AS found_location, fu.full_name AS finder_name, fu.phone_number AS finder_phone " +
            "FROM matches m " +
            "LEFT JOIN item_reports lr ON m.lost_report_id = lr.id " +
            "LEFT JOIN categories lc ON lr.category_id = lc.id " +
            "LEFT JOIN item_reports fr ON m.found_report_id = fr.id " +
            "LEFT JOIN categories fc ON fr.category_id = fc.id " +
            "LEFT JOIN campus_locations fl ON fr.location_id = fl.id " +
            "LEFT JOIN users fu ON fr.user_id = fu.id ";

    public Optional<Match> findById(Long id) {
        String sql = BASE_SELECT + "WHERE m.id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Error finding match by id: {}", id, e);
        }
        return Optional.empty();
    }

    public List<Match> findByLostReportId(Long lostId) {
        List<Match> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE m.lost_report_id = ? ORDER BY m.total_score DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, lostId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Error finding matches by lost id: {}", lostId, e);
        }
        return list;
    }

    public List<Match> findByFoundReportId(Long foundId) {
        List<Match> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE m.found_report_id = ? ORDER BY m.total_score DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, foundId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Error finding matches by found id: {}", foundId, e);
        }
        return list;
    }

    public List<Match> findMatchesForUser(Long userId) {
        List<Match> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE lr.user_id = ? OR fr.user_id = ? ORDER BY m.total_score DESC";
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
            log.error("Error finding matches for user: {}", userId, e);
        }
        return list;
    }

    public List<Match> findAll() {
        List<Match> list = new ArrayList<>();
        String sql = BASE_SELECT + "ORDER BY m.total_score DESC, m.created_at DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            log.error("Error finding all matches", e);
        }
        return list;
    }

    public List<Match> findByMinScore(double minScore) {
        List<Match> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE m.total_score >= ? ORDER BY m.total_score DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, minScore);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Error finding matches by min score: {}", minScore, e);
        }
        return list;
    }

    public boolean exists(Long lostReportId, Long foundReportId) {
        String sql = "SELECT COUNT(*) FROM matches WHERE lost_report_id = ? AND found_report_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, lostReportId);
            ps.setLong(2, foundReportId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            log.error("Error checking match existence", e);
        }
        return false;
    }

    public Long save(Match m) {
        String sql = "INSERT INTO matches (lost_report_id, found_report_id, total_score, category_score, location_score, date_score, color_score, description_score, explanation, status, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, m.getLostReportId());
            ps.setLong(2, m.getFoundReportId());
            ps.setDouble(3, m.getTotalScore());
            ps.setDouble(4, m.getCategoryScore());
            ps.setDouble(5, m.getLocationScore());
            ps.setDouble(6, m.getDateScore());
            ps.setDouble(7, m.getColorScore());
            ps.setDouble(8, m.getDescriptionScore());
            ps.setString(9, m.getExplanation());
            ps.setString(10, m.getStatus() != null ? m.getStatus().name() : MatchStatus.POTENTIAL.name());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            log.error("Error saving match", e);
        }
        return null;
    }

    public void updateStatus(Long id, MatchStatus status) {
        String sql = "UPDATE matches SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status != null ? status.name() : MatchStatus.POTENTIAL.name());
            ps.setLong(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Error updating match status: {}", id, e);
        }
    }

    public long countHighScore(double minScore) {
        String sql = "SELECT COUNT(*) FROM matches WHERE total_score >= ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, minScore);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            log.error("Error counting high score matches", e);
        }
        return 0;
    }

    private Match mapRow(ResultSet rs) throws SQLException {
        Match m = new Match();
        m.setId(rs.getLong("id"));
        m.setLostReportId(rs.getLong("lost_report_id"));
        m.setFoundReportId(rs.getLong("found_report_id"));
        m.setTotalScore(rs.getDouble("total_score"));
        m.setCategoryScore(rs.getDouble("category_score"));
        m.setLocationScore(rs.getDouble("location_score"));
        m.setDateScore(rs.getDouble("date_score"));
        m.setColorScore(rs.getDouble("color_score"));
        m.setDescriptionScore(rs.getDouble("description_score"));
        m.setExplanation(rs.getString("explanation"));
        String st = rs.getString("status");
        if (st != null) {
            try { m.setStatus(MatchStatus.valueOf(st)); } catch (Exception e) { m.setStatus(MatchStatus.POTENTIAL); }
        }
        Timestamp ct = rs.getTimestamp("created_at");
        if (ct != null) m.setCreatedAt(ct.toLocalDateTime());
        Timestamp ut = rs.getTimestamp("updated_at");
        if (ut != null) m.setUpdatedAt(ut.toLocalDateTime());

        try { m.setLostTitle(rs.getString("lost_title")); } catch (Exception ignore) {}
        try { m.setLostColor(rs.getString("lost_color")); } catch (Exception ignore) {}
        try { m.setLostCategory(rs.getString("lost_category")); } catch (Exception ignore) {}
        try { m.setLostImage(rs.getString("lost_image")); } catch (Exception ignore) {}
        try { m.setFoundTitle(rs.getString("found_title")); } catch (Exception ignore) {}
        try { m.setFoundColor(rs.getString("found_color")); } catch (Exception ignore) {}
        try { m.setFoundCategory(rs.getString("found_category")); } catch (Exception ignore) {}
        try { m.setFoundLocation(rs.getString("found_location")); } catch (Exception ignore) {}
        try { m.setFoundImage(rs.getString("found_image")); } catch (Exception ignore) {}
        try { m.setFinderName(rs.getString("finder_name")); } catch (Exception ignore) {}
        try { m.setFinderPhone(rs.getString("finder_phone")); } catch (Exception ignore) {}

        return m;
    }
}
