package com.findback.service;

import com.findback.enums.Role;
import com.findback.model.ItemReport;
import com.findback.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

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
                "01711999999");

        assertNotNull(registered);
        assertNotNull(registered.getId(), "User ID must not be null after registration");
        assertEquals(email.toLowerCase(), registered.getEmail());

        // Login with same credentials
        User loggedIn = authService.login(email, rawPassword);
        assertNotNull(loggedIn, "User should be able to login with registered credentials");
        assertEquals(registered.getId(), loggedIn.getId());
        assertEquals(Role.ROLE_STUDENT, loggedIn.getRole());
    }

    @Test
    @DisplayName("Duplicate email registration is prevented")
    void testDuplicateEmailRegistrationFails() {
        AuthService authService = new AuthService();
        long ts = System.currentTimeMillis();
        String email = "dup" + ts + "@findback.edu";

        authService.register("User One", email, "Secret123", "STD-1", "CSE", "01711000000");

        // Attempt second registration with same email
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.register("User Two", email, "Secret456", "STD-2", "EEE", "01711000001");
        });

        assertTrue(exception.getMessage().contains("already exists"), "Should inform user that email already exists");
    }

    @Test
    @DisplayName("Login with invalid password throws IllegalArgumentException")
    void testLoginWithInvalidPassword() {
        AuthService authService = new AuthService();
        long ts = System.currentTimeMillis();
        String email = "login_test" + ts + "@findback.edu";

        authService.register("Login Test", email, "CorrectPassword", "STD-3", "SWE", "01711000002");

        assertThrows(IllegalArgumentException.class, () -> {
            authService.login(email, "WrongPassword");
        });
    }

    @Test
    @DisplayName("Create lost report and trigger matching with Groq AI engine")
    void testCreateLostReportAndMatch() {
        try (java.sql.Connection conn = com.findback.config.DatabaseConnection.getInstance().getConnection()) {
            com.findback.config.DatabaseConnection.getInstance().executeSqlScript(conn, "database/schema.sql");
            com.findback.config.DatabaseConnection.getInstance().executeSqlScript(conn, "database/seed.sql");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        ReportService reportService = new ReportService();
        MatchingService matchingService = new MatchingService();

        ItemReport report = reportService.createLostReport(
                3L,
                1L, // Electronics
                1L, // Library
                "Test Laptop",
                "Black Dell Inspiron laptop left on table",
                "Black",
                "Desk 12",
                LocalDate.now(),
                null);

        assertNotNull(report, "Created report should not be null");
        assertNotNull(report.getId(), "Created report ID should not be null");

        assertDoesNotThrow(() -> matchingService.processReportMatches(report.getId()));
    }

    @Test
    @DisplayName("Groq AI Service semantic evaluation operates without crashing")
    void testGroqAiEvaluation() {
        GroqAiService ai = new GroqAiService();
        ItemReport lost = new ItemReport();
        lost.setId(100L);
        lost.setTitle("Casio scientific calculator fx-991EX");
        lost.setCategoryName("Electronics");
        lost.setLocationName("Academic Building B");
        lost.setColor("Black");
        lost.setEventDate(LocalDate.now());
        lost.setDescription("Black Casio calculator left in Room 204");

        ItemReport found = new ItemReport();
        found.setId(101L);
        found.setTitle("Casio fx-991EX calculator");
        found.setCategoryName("Electronics");
        found.setLocationName("Academic Building B");
        found.setColor("Black");
        found.setEventDate(LocalDate.now());
        found.setDescription("Found scientific calculator on desk in Room 204");

        GroqAiService.AiMatchAssessment result = ai.evaluateMatch(lost, found);
        if (result != null) {
            assertTrue(result.totalScore >= 70, "Groq AI should recognize strong similarity between these calculators");
            assertNotNull(result.explanation, "Groq AI should provide human explanation");
            System.out.println("Groq AI Score: " + result.totalScore);
            System.out.println("Groq AI Explanation: " + result.explanation);
        }
    }
}
