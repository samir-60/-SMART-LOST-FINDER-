package com.findback.dao;

import com.findback.config.DatabaseConnection;
import com.findback.enums.Role;
import com.findback.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDao {
    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Error finding user by id: {}", id, e);
        }
        return Optional.empty();
    }

    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Error finding user by email: {}", email, e);
        }
        return Optional.empty();
    }

    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY id ASC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            log.error("Error finding all users", e);
        }
        return list;
    }

    public Long save(User user) {
        String sql = "INSERT INTO users (full_name, email, password_hash, student_id, department, phone_number, role, active, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getStudentId());
            ps.setString(5, user.getDepartment());
            ps.setString(6, user.getPhoneNumber());
            ps.setString(7, user.getRole() != null ? user.getRole().name() : Role.ROLE_STUDENT.name());
            ps.setBoolean(8, user.isActive());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            log.error("Error saving user: {}", user.getEmail(), e);
            throw new RuntimeException("Database error saving user: " + e.getMessage(), e);
        }
        throw new RuntimeException("Database error: Generated ID not returned after saving user.");
    }

    public void update(User user) {
        String sql = "UPDATE users SET full_name = ?, email = ?, student_id = ?, department = ?, phone_number = ?, role = ?, active = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getStudentId());
            ps.setString(4, user.getDepartment());
            ps.setString(5, user.getPhoneNumber());
            ps.setString(6, user.getRole() != null ? user.getRole().name() : Role.ROLE_STUDENT.name());
            ps.setBoolean(7, user.isActive());
            ps.setLong(8, user.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Error updating user: {}", user.getId(), e);
        }
    }

    public boolean delete(Long id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("Error deleting user: {}", id, e);
            return false;
        }
    }

    public long countAll() {
        String sql = "SELECT COUNT(*) FROM users";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            log.error("Error counting all users", e);
        }
        return 0;
    }

    public long countByRole(Role role) {
        String sql = "SELECT COUNT(*) FROM users WHERE role = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, role != null ? role.name() : "");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            log.error("Error counting users by role: {}", role, e);
        }
        return 0;
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getLong("id"));
        u.setFullName(rs.getString("full_name"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setStudentId(rs.getString("student_id"));
        u.setDepartment(rs.getString("department"));
        u.setPhoneNumber(rs.getString("phone_number"));
        String roleStr = rs.getString("role");
        if (roleStr != null) {
            String cleanRole = roleStr.trim().toUpperCase();
            if (cleanRole.equals("ADMIN") || cleanRole.equals("ROLE_ADMIN")) {
                u.setRole(Role.ROLE_ADMIN);
            } else if (cleanRole.equals("MODERATOR") || cleanRole.equals("ROLE_MODERATOR")) {
                u.setRole(Role.ROLE_MODERATOR);
            } else {
                u.setRole(Role.ROLE_STUDENT);
            }
        } else {
            u.setRole(Role.ROLE_STUDENT);
        }
        u.setActive(rs.getBoolean("active"));
        Timestamp ct = rs.getTimestamp("created_at");
        if (ct != null) u.setCreatedAt(ct.toLocalDateTime());
        Timestamp ut = rs.getTimestamp("updated_at");
        if (ut != null) u.setUpdatedAt(ut.toLocalDateTime());
        return u;
    }
}
