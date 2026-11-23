package com.findback.dao;

import com.findback.config.DatabaseConnection;
import com.findback.model.AdminAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdminActionDao {
    private static final Logger log = LoggerFactory.getLogger(AdminActionDao.class);

    public Long save(AdminAction a) {
        String sql = "INSERT INTO admin_actions (admin_id, action_type, target_entity, target_id, details, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, a.getAdminId());
            ps.setString(2, a.getActionType());
            ps.setString(3, a.getTargetEntity());
            if (a.getTargetId() != null) ps.setLong(4, a.getTargetId()); else ps.setNull(4, Types.BIGINT);
            ps.setString(5, a.getDetails());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            log.error("Error saving admin action", e);
        }
        return null;
    }

    public List<AdminAction> findRecent(int limit) {
        List<AdminAction> list = new ArrayList<>();
        String sql = "SELECT a.*, u.full_name AS admin_name FROM admin_actions a " +
                     "LEFT JOIN users u ON a.admin_id = u.id ORDER BY a.created_at DESC LIMIT ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AdminAction a = new AdminAction();
                    a.setId(rs.getLong("id"));
                    a.setAdminId(rs.getLong("admin_id"));
                    a.setActionType(rs.getString("action_type"));
                    a.setTargetEntity(rs.getString("target_entity"));
                    long tid = rs.getLong("target_id");
                    if (!rs.wasNull()) a.setTargetId(tid);
                    a.setDetails(rs.getString("details"));
                    Timestamp ct = rs.getTimestamp("created_at");
                    if (ct != null) a.setCreatedAt(ct.toLocalDateTime());
                    try { a.setAdminName(rs.getString("admin_name")); } catch (Exception ignore) {}
                    list.add(a);
                }
            }
        } catch (SQLException e) {
            log.error("Error finding recent admin actions", e);
        }
        return list;
    }
}
