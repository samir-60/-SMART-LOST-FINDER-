package com.findback.service;

import com.findback.dao.AdminActionDao;
import com.findback.dao.CategoryDao;
import com.findback.dao.ClaimDao;
import com.findback.dao.HandoverDao;
import com.findback.dao.LocationDao;
import com.findback.dao.MatchDao;
import com.findback.dao.ReportDao;
import com.findback.dao.UserDao;
import com.findback.enums.ClaimStatus;
import com.findback.enums.MatchStatus;
import com.findback.enums.ReportStatus;
import com.findback.enums.ReportType;
import com.findback.enums.Role;
import com.findback.model.AdminAction;
import com.findback.model.CampusLocation;
import com.findback.model.Category;
import com.findback.model.User;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Administrative operations — dashboard stats, user management, category/location CRUD, and audit logging.
 */
public class AdminService {

    private final UserDao userDao;
    private final ReportDao reportDao;
    private final MatchDao matchDao;
    private final ClaimDao claimDao;
    private final HandoverDao handoverDao;
    private final CategoryDao categoryDao;
    private final LocationDao locationDao;
    private final AdminActionDao adminActionDao;

    public AdminService() {
        this.userDao = new UserDao();
        this.reportDao = new ReportDao();
        this.matchDao = new MatchDao();
        this.claimDao = new ClaimDao();
        this.handoverDao = new HandoverDao();
        this.categoryDao = new CategoryDao();
        this.locationDao = new LocationDao();
        this.adminActionDao = new AdminActionDao();
    }

    /**
     * Builds a dashboard statistics map with counts and breakdowns.
     */
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new LinkedHashMap<>();

        stats.put("totalUsers", userDao.countAll());
        stats.put("totalLost", reportDao.countByType(ReportType.LOST));
        stats.put("totalFound", reportDao.countByType(ReportType.FOUND));
        stats.put("totalOpen", reportDao.countByStatus(ReportStatus.OPEN));
        stats.put("potentialMatches", matchDao.countHighScore(70.0));
        stats.put("pendingClaims", claimDao.countByStatus(ClaimStatus.PENDING));
        stats.put("recoveredItems", handoverDao.countCompleted());

        // Reports by category
        Map<String, Long> reportsByCategory = new LinkedHashMap<>();
        List<Object[]> catCounts = reportDao.countByCategory();
        for (Object[] row : catCounts) {
            String categoryName = (String) row[0];
            Long count = ((Number) row[1]).longValue();
            reportsByCategory.put(categoryName, count);
        }
        stats.put("reportsByCategory", reportsByCategory);

        // Reports by location
        Map<String, Long> reportsByLocation = new LinkedHashMap<>();
        List<Object[]> locCounts = reportDao.countByLocation();
        for (Object[] row : locCounts) {
            String locationName = (String) row[0];
            Long count = ((Number) row[1]).longValue();
            reportsByLocation.put(locationName, count);
        }
        stats.put("reportsByLocation", reportsByLocation);

        return stats;
    }

    /**
     * Toggles a user's active status (enable/disable account).
     */
    public void toggleUserActive(Long userId) {
        User user = userDao.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        user.setActive(!user.isActive());
        userDao.update(user);
    }

    /**
     * Changes a user's role.
     */
    public void changeUserRole(Long userId, Role newRole) {
        User user = userDao.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        user.setRole(newRole);
        userDao.update(user);
    }

    /**
     * Creates a new item category.
     */
    public Category createCategory(String name, String description, String icon) {
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        category.setIconName(icon);
        category.setActive(true);

        Long id = categoryDao.save(category);
        category.setId(id);
        return category;
    }

    /**
     * Creates a new campus location.
     */
    public CampusLocation createLocation(String name, String zone, String description) {
        CampusLocation location = new CampusLocation();
        location.setName(name);
        location.setZone(zone);
        location.setDescription(description);
        location.setActive(true);

        Long id = locationDao.save(location);
        location.setId(id);
        return location;
    }

    /**
     * Toggles a category's active status.
     */
    public void toggleCategory(Long id) {
        categoryDao.toggleActive(id);
    }

    /**
     * Toggles a location's active status.
     */
    public void toggleLocation(Long id) {
        locationDao.toggleActive(id);
    }

    public boolean deleteCategory(Long id) {
        return categoryDao.delete(id);
    }

    public boolean deleteLocation(Long id) {
        return locationDao.delete(id);
    }

    public boolean deleteUser(Long id) {
        return userDao.delete(id);
    }

    /**
     * Logs an administrative action for auditing purposes.
     */
    public void logAction(Long adminId, String actionType, String targetEntity,
                          Long targetId, String details) {
        AdminAction action = new AdminAction();
        action.setAdminId(adminId);
        action.setActionType(actionType);
        action.setTargetEntity(targetEntity);
        action.setTargetId(targetId);
        action.setDetails(details);
        action.setCreatedAt(LocalDateTime.now());
        adminActionDao.save(action);
    }

    /**
     * Gets the most recent audit log entries.
     */
    public List<AdminAction> getRecentAuditLogs() {
        return adminActionDao.findRecent(50);
    }
}
