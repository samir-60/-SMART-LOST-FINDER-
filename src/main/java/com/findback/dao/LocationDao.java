package com.findback.dao;

import com.findback.config.DatabaseConnection;
import com.findback.model.CampusLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LocationDao {
    private static final Logger log = LoggerFactory.getLogger(LocationDao.class);

    public Optional<CampusLocation> findById(Long id) {
        String sql = "SELECT * FROM campus_locations WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Error finding location by id: {}", id, e);
        }
        return Optional.empty();
    }

    public List<CampusLocation> findAll() {
        List<CampusLocation> list = new ArrayList<>();
        String sql = "SELECT * FROM campus_locations ORDER BY name ASC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            log.error("Error finding all locations", e);
        }
        return list;
    }

    public List<CampusLocation> findAllActive() {
        List<CampusLocation> list = new ArrayList<>();
        String sql = "SELECT * FROM campus_locations WHERE active = TRUE ORDER BY name ASC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            log.error("Error finding active locations", e);
        }
        return list;
    }

    public Long save(CampusLocation loc) {
        String sql = "INSERT INTO campus_locations (name, zone, description, active, created_at) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, loc.getName());
            ps.setString(2, loc.getZone());
            ps.setString(3, loc.getDescription());
            ps.setBoolean(4, loc.isActive());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            log.error("Error saving location: {}", loc.getName(), e);
        }
        return null;
    }

    public void toggleActive(Long id) {
        String sql = "UPDATE campus_locations SET active = NOT active WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Error toggling location active status: {}", id, e);
        }
    }

    public boolean delete(Long id) {
        String sql = "DELETE FROM campus_locations WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("Error deleting campus location: {}", id, e);
            return false;
        }
    }

    private CampusLocation mapRow(ResultSet rs) throws SQLException {
        CampusLocation l = new CampusLocation();
        l.setId(rs.getLong("id"));
        l.setName(rs.getString("name"));
        l.setZone(rs.getString("zone"));
        l.setDescription(rs.getString("description"));
        l.setActive(rs.getBoolean("active"));
        Timestamp ct = rs.getTimestamp("created_at");
        if (ct != null) l.setCreatedAt(ct.toLocalDateTime());
        return l;
    }
}
