package com.findback.service;

import com.findback.enums.Role;
import com.findback.model.ItemReport;
import com.findback.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    @Test
    @DisplayName("BCrypt hashes passwords securely and verifies correctly")
    void testBCryptPasswordHashing() {
        String rawPassword = "SecurePassword@123";
        String hash = AuthService.hashPassword(rawPassword);

        assertNotNull(hash);
        assertNotEquals(rawPassword, hash);
        assertTrue(hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$"));

        assertTrue(AuthService.verifyPassword(rawPassword, hash));
        assertFalse(AuthService.verifyPassword("WrongPassword", hash));

        System.out.println("HASH_ADMIN: " + AuthService.hashPassword("Admin@123"));
        System.out.println("HASH_MOD: " + AuthService.hashPassword("Mod@123"));
        System.out.println("HASH_STUDENT: " + AuthService.hashPassword("Student@123"));
    }

    @Test
    @DisplayName("Register a new user and login successfully")
    void testRegisterAndLogin() {
        AuthService authService = new AuthService();
        long ts = System.currentTimeMillis();
        String email = "student" + ts + "@findback.edu";
        String rawPassword = "Password@123";

        User registered = authService.register(
                "Test Student " + ts,
                email,
                rawPassword,
                "STD-" + ts,
                "Computer Science",
                "01711999999"
        );

        assertNotNull(registered);
        assertNotNull(registered.getId(), "User ID must not be null after registration");
        assertEquals(email.toLowerCase(), registered.getEmail());

        // Now test login with same credentials
        User loggedIn = authService.login(email, rawPassword);
        assertNotNull(loggedIn, "User should be able to login with registered credentials");
        assertEquals(registered.getId(), loggedIn.getId());
        assertEquals(Role.ROLE_STUDENT, loggedIn.getRole());
    }

    @Test
    @DisplayName("Create lost report and trigger matching")
    void testCreateLostReportAndMatch() {
        try (java.sql.Connection conn = com.findback.config.DatabaseConnection.getInstance().getConnection()) {
            com.findback.config.DatabaseConnection.getInstance().executeSqlScript(conn, "database/schema.sql");
            com.findback.config.DatabaseConnection.getInstance().executeSqlScript(conn, "database/seed.sql");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        ReportService reportService = new ReportService();
        MatchingService matchingService = new MatchingService();

        // Use seed user 3 (Tanvir Ahmed) or 1
        ItemReport report = reportService.createLostReport(
                3L,
                1L, // Electronics
                1L, // Library
                "Test Laptop",
                "Black Dell Inspiron laptop left on table",
                "Black",
                "Desk 12",
                java.time.LocalDate.now(),
                null
        );

        assertNotNull(report, "Created report should not be null");
        assertNotNull(report.getId(), "Created report ID should not be null");

        assertDoesNotThrow(() -> matchingService.processReportMatches(report.getId()));
    }
}

