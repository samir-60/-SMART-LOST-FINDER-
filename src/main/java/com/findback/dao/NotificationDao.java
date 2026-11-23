package com.findback.dao;

import com.findback.config.DatabaseConnection;
import com.findback.enums.NotificationType;
import com.findback.model.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationDao {
    private static final Logger log = LoggerFactory.getLogger(NotificationDao.class);

    public List<Notification> findByUserId(Long userId) {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Error finding notifications by user: {}", userId, e);
        }
        return list;
    }

    public List<Notification> findUnreadByUserId(Long userId) {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE user_id = ? AND is_read = FALSE ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Error finding unread notifications: {}", userId, e);
        }
        return list;
    }

    public Long save(Notification n) {
        String sql = "INSERT INTO notifications (user_id, title, message, target_url, type, is_read, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, n.getUserId());
            ps.setString(2, n.getTitle());
            ps.setString(3, n.getMessage());
            ps.setString(4, n.getTargetUrl());
            ps.setString(5, n.getType() != null ? n.getType().name() : NotificationType.GENERAL.name());
            ps.setBoolean(6, n.isRead());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            log.error("Error saving notification", e);
        }
        return null;
    }

    public void markRead(Long notifId) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, notifId);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Error marking notification as read: {}", notifId, e);
        }
    }

    public void markAllRead(Long userId) {
        markAllReadForUser(userId);
    }

    public void markAllReadForUser(Long userId) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Error marking all notifications read for user: {}", userId, e);
        }
    }

    public long countUnread(Long userId) {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = FALSE";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            log.error("Error counting unread notifications: {}", userId, e);
        }
        return 0;
    }

    private Notification mapRow(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setId(rs.getLong("id"));
        n.setUserId(rs.getLong("user_id"));
        n.setTitle(rs.getString("title"));
        n.setMessage(rs.getString("message"));
        n.setTargetUrl(rs.getString("target_url"));
        String tp = rs.getString("type");
        if (tp != null) {
            try { n.setType(NotificationType.valueOf(tp)); } catch (Exception e) { n.setType(NotificationType.GENERAL); }
        }
        n.setRead(rs.getBoolean("is_read"));
        Timestamp ct = rs.getTimestamp("created_at");
        if (ct != null) n.setCreatedAt(ct.toLocalDateTime());
        return n;
    }
}
