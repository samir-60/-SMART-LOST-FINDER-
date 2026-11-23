package com.findback.service;

import com.findback.dao.UserDao;
import com.findback.model.User;
import com.findback.enums.Role;
import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Handles user authentication — login and registration with BCrypt password hashing.
 */
public class AuthService {

    private final UserDao userDao;

    public AuthService() {
        this.userDao = new UserDao();
    }

    /**
     * Authenticates a user by email and password.
     *
     * @param email    the user's email
     * @param password the plaintext password
     * @return the authenticated User
     * @throws IllegalArgumentException if credentials are invalid
     */
    public User login(String email, String password) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required.");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required.");
        }

        Optional<User> userOpt = userDao.findByEmail(email.trim().toLowerCase());
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        User user = userOpt.get();

        if (!user.isActive()) {
            throw new IllegalArgumentException("Your account has been deactivated. Please contact an administrator.");
        }

        if (!verifyPassword(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        return user;
    }

    /**
     * Registers a new user with ROLE_STUDENT role.
     *
     * @param fullName    the user's full name
     * @param email       the user's email
     * @param password    the plaintext password
     * @param studentId   the student ID
     * @param department  the department
     * @param phoneNumber the phone number
     * @return the newly created User
     * @throws IllegalArgumentException if validation fails
     */
    public User register(String fullName, String email, String password,
                         String studentId, String department, String phoneNumber) {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name is required.");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required.");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters.");
        }

        String normalizedEmail = email.trim().toLowerCase();

        Optional<User> existing = userDao.findByEmail(normalizedEmail);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }

        User user = new User();
        user.setFullName(fullName.trim());
        user.setEmail(normalizedEmail);
        user.setPasswordHash(hashPassword(password));
        user.setStudentId((studentId != null && !studentId.trim().isEmpty()) ? studentId.trim() : "N/A");
        user.setDepartment((department != null && !department.trim().isEmpty()) ? department.trim() : "General");
        user.setPhoneNumber((phoneNumber != null && !phoneNumber.trim().isEmpty()) ? phoneNumber.trim() : "");
        user.setRole(Role.ROLE_STUDENT);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());

        Long id = userDao.save(user);
        if (id == null) {
            throw new IllegalStateException("Failed to register user: database returned null ID.");
        }
        user.setId(id);

        return user;
    }

    /**
     * Hashes a plaintext password using BCrypt.
     */
    public static String hashPassword(String plaintext) {
        return BCrypt.hashpw(plaintext, BCrypt.gensalt(10));
    }

    /**
     * Verifies a plaintext password against a BCrypt hash.
     */
    public static boolean verifyPassword(String plaintext, String hash) {
        return BCrypt.checkpw(plaintext, hash);
    }
}
