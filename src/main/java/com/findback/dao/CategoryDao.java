package com.findback.dao;

import com.findback.config.DatabaseConnection;
import com.findback.model.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CategoryDao {
    private static final Logger log = LoggerFactory.getLogger(CategoryDao.class);

    public Optional<Category> findById(Long id) {
        String sql = "SELECT * FROM categories WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Error finding category by id: {}", id, e);
        }
        return Optional.empty();
    }

    public List<Category> findAll() {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT * FROM categories ORDER BY name ASC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            log.error("Error finding all categories", e);
        }
        return list;
    }

    public List<Category> findAllActive() {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT * FROM categories WHERE active = TRUE ORDER BY name ASC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            log.error("Error finding active categories", e);
        }
        return list;
    }

    public Long save(Category c) {
        String sql = "INSERT INTO categories (name, description, icon_name, active, created_at) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getName());
            ps.setString(2, c.getDescription());
            ps.setString(3, c.getIconName());
            ps.setBoolean(4, c.isActive());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            log.error("Error saving category: {}", c.getName(), e);
        }
        return null;
    }

    public void toggleActive(Long id) {
        String sql = "UPDATE categories SET active = NOT active WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Error toggling category active status: {}", id, e);
        }
    }

    public boolean delete(Long id) {
        String sql = "DELETE FROM categories WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("Error deleting category: {}", id, e);
            return false;
        }
    }

    private Category mapRow(ResultSet rs) throws SQLException {
        Category c = new Category();
        c.setId(rs.getLong("id"));
        c.setName(rs.getString("name"));
        c.setDescription(rs.getString("description"));
        c.setIconName(rs.getString("icon_name"));
        c.setActive(rs.getBoolean("active"));
        Timestamp ct = rs.getTimestamp("created_at");
        if (ct != null) c.setCreatedAt(ct.toLocalDateTime());
        return c;
    }
}
