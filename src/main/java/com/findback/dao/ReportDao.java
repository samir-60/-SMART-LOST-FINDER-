package com.findback.dao;

import com.findback.config.DatabaseConnection;
import com.findback.enums.CustodyStatus;
import com.findback.enums.ReportStatus;
import com.findback.enums.ReportType;
import com.findback.model.ItemReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

public class ReportDao {
    private static final Logger log = LoggerFactory.getLogger(ReportDao.class);

    private static final String BASE_SELECT =
            "SELECT r.*, u.full_name AS user_name, u.phone_number AS user_phone, c.name AS category_name, l.name AS location_name " +
            "FROM item_reports r " +
            "LEFT JOIN users u ON r.user_id = u.id " +
            "LEFT JOIN categories c ON r.category_id = c.id " +
            "LEFT JOIN campus_locations l ON r.location_id = l.id ";

    public Optional<ItemReport> findById(Long id) {
        String sql = BASE_SELECT + "WHERE r.id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Error finding report by id: {}", id, e);
        }
        return Optional.empty();
    }

    public List<ItemReport> findByUserId(Long userId) {
        List<ItemReport> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE r.user_id = ? ORDER BY r.created_at DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Error finding reports by user: {}", userId, e);
        }
        return list;
    }

    public List<ItemReport> findByTypeAndStatus(ReportType type, ReportStatus status) {
        List<ItemReport> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE r.report_type = ? AND r.status = ? ORDER BY r.created_at DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, type != null ? type.name() : "");
            ps.setString(2, status != null ? status.name() : "");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Error finding reports by type and status", e);
        }
        return list;
    }

    public List<ItemReport> findAll() {
        List<ItemReport> list = new ArrayList<>();
        String sql = BASE_SELECT + "ORDER BY r.created_at DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            log.error("Error finding all reports", e);
        }
        return list;
    }

    public List<ItemReport> findRecentByType(ReportType type, int limit) {
        List<ItemReport> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE r.report_type = ? ORDER BY r.created_at DESC LIMIT ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, type != null ? type.name() : "");
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Error finding recent reports by type", e);
        }
        return list;
    }

    public Long save(ItemReport r) {
        String sql = "INSERT INTO item_reports (user_id, category_id, location_id, report_type, title, description, color, specific_location, event_date, image_path, status, custody_status, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, r.getUserId());
            ps.setLong(2, r.getCategoryId());
            ps.setLong(3, r.getLocationId());
            ps.setString(4, r.getReportType() != null ? r.getReportType().name() : ReportType.LOST.name());
            ps.setString(5, r.getTitle());
            ps.setString(6, r.getDescription());
            ps.setString(7, r.getColor());
            ps.setString(8, r.getSpecificLocation());
            ps.setDate(9, r.getEventDate() != null ? Date.valueOf(r.getEventDate()) : Date.valueOf(LocalDate.now()));
            ps.setString(10, r.getImagePath());
            ps.setString(11, r.getStatus() != null ? r.getStatus().name() : ReportStatus.OPEN.name());
            ps.setString(12, r.getCustodyStatus() != null ? r.getCustodyStatus().name() : CustodyStatus.WITH_FINDER.name());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            log.error("Error saving item report: {}", r.getTitle(), e);
            throw new RuntimeException("Database error saving report: " + e.getMessage(), e);
        }
        throw new RuntimeException("Failed to save report: database did not return generated key.");
    }

    public void update(ItemReport r) {
        String sql = "UPDATE item_reports SET title = ?, description = ?, color = ?, category_id = ?, location_id = ?, specific_location = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, r.getTitle());
            ps.setString(2, r.getDescription());
            ps.setString(3, r.getColor());
            ps.setLong(4, r.getCategoryId());
            ps.setLong(5, r.getLocationId());
            ps.setString(6, r.getSpecificLocation());
            ps.setLong(7, r.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Error updating item report: {}", r.getId(), e);
        }
    }

    public void updateStatus(Long id, ReportStatus status) {
        String sql = "UPDATE item_reports SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status != null ? status.name() : ReportStatus.OPEN.name());
            ps.setLong(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Error updating report status: {}", id, e);
        }
    }

    public boolean delete(Long id) {
        String sql = "DELETE FROM item_reports WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("Error deleting report: {}", id, e);
            return false;
        }
    }

    public long countByType(ReportType type) {
        String sql = "SELECT COUNT(*) FROM item_reports WHERE report_type = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, type != null ? type.name() : "");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            log.error("Error counting reports by type", e);
        }
        return 0;
    }

    public long countByStatus(ReportStatus status) {
        String sql = "SELECT COUNT(*) FROM item_reports WHERE status = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status != null ? status.name() : "");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            log.error("Error counting reports by status", e);
        }
        return 0;
    }

    public List<Object[]> countByCategory() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT c.name, COUNT(r.id) FROM categories c LEFT JOIN item_reports r ON c.id = r.category_id GROUP BY c.name";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Object[]{rs.getString(1), rs.getLong(2)});
            }
        } catch (SQLException e) {
            log.error("Error counting reports by category", e);
        }
        return list;
    }

    public List<Object[]> countByLocation() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT l.name, COUNT(r.id) FROM campus_locations l LEFT JOIN item_reports r ON l.id = r.location_id GROUP BY l.name";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Object[]{rs.getString(1), rs.getLong(2)});
            }
        } catch (SQLException e) {
            log.error("Error counting reports by location", e);
        }
        return list;
    }

    public List<ItemReport> search(String keyword, Long categoryId, Long locationId, ReportType type, String color) {
        List<ItemReport> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(BASE_SELECT).append("WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (LOWER(r.title) LIKE ? OR LOWER(r.description) LIKE ?) ");
            String kw = "%" + keyword.trim().toLowerCase() + "%";
            params.add(kw);
            params.add(kw);
        }
        if (categoryId != null && categoryId > 0) {
            sql.append("AND r.category_id = ? ");
            params.add(categoryId);
        }
        if (locationId != null && locationId > 0) {
            sql.append("AND r.location_id = ? ");
            params.add(locationId);
        }
        if (type != null) {
            sql.append("AND r.report_type = ? ");
            params.add(type.name());
        }
        if (color != null && !color.trim().isEmpty() && !color.equalsIgnoreCase("All")) {
            sql.append("AND LOWER(r.color) LIKE ? ");
            params.add("%" + color.trim().toLowerCase() + "%");
        }

        sql.append("ORDER BY r.created_at DESC");

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Error searching reports", e);
        }
        return list;
    }

    private ItemReport mapRow(ResultSet rs) throws SQLException {
        ItemReport r = new ItemReport();
        r.setId(rs.getLong("id"));
        r.setUserId(rs.getLong("user_id"));
        r.setCategoryId(rs.getLong("category_id"));
        r.setLocationId(rs.getLong("location_id"));
        String rt = rs.getString("report_type");
        if (rt != null) {
            try { r.setReportType(ReportType.valueOf(rt)); } catch (Exception e) { r.setReportType(ReportType.LOST); }
        }
        r.setTitle(rs.getString("title"));
        r.setDescription(rs.getString("description"));
        r.setColor(rs.getString("color"));
        r.setSpecificLocation(rs.getString("specific_location"));
        Date ed = rs.getDate("event_date");
        if (ed != null) r.setEventDate(ed.toLocalDate());
        r.setImagePath(rs.getString("image_path"));
        String st = rs.getString("status");
        if (st != null) {
            try { r.setStatus(ReportStatus.valueOf(st)); } catch (Exception e) { r.setStatus(ReportStatus.OPEN); }
        }
        String cs = rs.getString("custody_status");
        if (cs != null) {
            try { r.setCustodyStatus(CustodyStatus.valueOf(cs)); } catch (Exception e) { r.setCustodyStatus(CustodyStatus.WITH_FINDER); }
        }
        Timestamp ct = rs.getTimestamp("created_at");
        if (ct != null) r.setCreatedAt(ct.toLocalDateTime());
        Timestamp ut = rs.getTimestamp("updated_at");
        if (ut != null) r.setUpdatedAt(ut.toLocalDateTime());

        try { r.setUserName(rs.getString("user_name")); } catch (Exception ignore) {}
        try { r.setUserPhone(rs.getString("user_phone")); } catch (Exception ignore) {}
        try { r.setCategoryName(rs.getString("category_name")); } catch (Exception ignore) {}
        try { r.setLocationName(rs.getString("location_name")); } catch (Exception ignore) {}

        return r;
    }
}
