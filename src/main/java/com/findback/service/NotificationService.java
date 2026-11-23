package com.findback.service;

import com.findback.dao.NotificationDao;
import com.findback.enums.NotificationType;
import com.findback.model.Notification;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for creating and managing user notifications.
 */
public class NotificationService {

    private final NotificationDao notificationDao;

    public NotificationService() {
        this.notificationDao = new NotificationDao();
    }

    /**
     * Creates and saves a new notification for a user.
     */
    public void createNotification(Long userId, String title, String message,
                                   String targetUrl, NotificationType type) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setTargetUrl(targetUrl);
        notification.setType(type);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        notificationDao.save(notification);
    }

    /**
     * Gets all notifications for a user (read and unread).
     */
    public List<Notification> getForUser(Long userId) {
        return notificationDao.findByUserId(userId);
    }

    /**
     * Gets only unread notifications for a user.
     */
    public List<Notification> getUnreadForUser(Long userId) {
        return notificationDao.findUnreadByUserId(userId);
    }

    /**
     * Marks a single notification as read.
     */
    public void markRead(Long notifId) {
        notificationDao.markRead(notifId);
    }

    /**
     * Marks all notifications as read for a user.
     */
    public void markAllRead(Long userId) {
        notificationDao.markAllReadForUser(userId);
    }

    /**
     * Gets the count of unread notifications for a user.
     */
    public long getUnreadCount(Long userId) {
        return notificationDao.countUnread(userId);
    }
}
