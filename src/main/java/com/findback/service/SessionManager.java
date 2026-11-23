package com.findback.service;

import com.findback.model.User;
import com.findback.enums.Role;

/**
 * Manages the current logged-in user session as an in-memory singleton.
 */
public class SessionManager {

    private static SessionManager instance;
    private User currentUser;

    private SessionManager() {
        // Private constructor for singleton
    }

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void login(User user) {
        this.currentUser = user;
    }

    public void logout() {
        this.currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean isAdmin() {
        return isLoggedIn() && currentUser.getRole() == Role.ROLE_ADMIN;
    }

    public boolean isStudentOrHigher() {
        return isLoggedIn();
    }
}
