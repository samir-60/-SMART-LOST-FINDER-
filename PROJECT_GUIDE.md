# FindBack – Smart Lost & Found Portal
## Comprehensive Project Architecture & Technical Guide

Welcome to the complete architectural and operational guide for **FindBack**, a pure Java 21 Swing desktop application developed for campus lost and found item recovery at Metropolitan University.

---

## 1. System Overview & Technology Stack

- **Platform:** Pure Java 21 Desktop Application (Standalone, zero web framework overhead).
- **GUI Engine:** Java Swing with FlatLaf (Modern macOS/Windows 11 inspired Flat Light theme, custom glassmorphism, 60 FPS animated typography).
- **Database:** MySQL 8 with automated fallback to Embedded H2 in MySQL compatibility mode (`jdbc:h2:./data/findback_db;MODE=MySQL;AUTO_SERVER=TRUE`).
- **Connection Pool:** HikariCP for connection pooling.
- **Security:** OpenBSD BCrypt for cryptographic password hashing with 10 salt rounds.
- **Reporting & Visuals:** JFreeChart for administrative analytics and recovery statistics.
- **Build System:** Apache Maven.

---

## 2. Directory Structure

```text
Smart Lost & Found Portal/
├── database/
│   ├── schema.sql           # Table DDL, relational constraints, and indexes
│   ├── seed.sql             # Demo categories, locations, users, and reports
│   └── findback.sql         # Combined complete SQL setup for MySQL 8
├── src/main/java/com/findback/
│   ├── Main.java            # Application entrypoint & FlatLaf bootstrap
│   ├── config/
│   │   └── DatabaseConnection.java  # Dual-mode Hikari connection pool (MySQL + H2)
│   ├── dao/
│   │   ├── AdminActionDao.java
│   │   ├── CategoryDao.java
│   │   ├── ClaimDao.java
│   │   ├── HandoverDao.java
│   │   ├── LocationDao.java
│   │   ├── MatchDao.java
│   │   ├── NotificationDao.java
│   │   ├── ReportDao.java
│   │   └── UserDao.java
│   ├── enums/
│   │   ├── ClaimStatus.java
│   │   ├── CustodyStatus.java
│   │   ├── MatchStatus.java
│   │   ├── NotificationType.java
│   │   ├── ReportStatus.java
│   │   ├── ReportType.java
│   │   └── Role.java
│   ├── model/
│   │   ├── AdminAction.java
│   │   ├── CampusLocation.java
│   │   ├── Category.java
│   │   ├── Claim.java
│   │   ├── Handover.java
│   │   ├── ItemReport.java   # Abstract/base report model
│   │   ├── Match.java        # Match calculation details and scores
│   │   ├── Notification.java
│   │   └── User.java
│   ├── service/
│   │   ├── AdminService.java
│   │   ├── AuthService.java
│   │   ├── ClaimService.java
│   │   ├── MatchingService.java # 100-point explainable AI rule engine
│   │   ├── ReportService.java
│   │   └── SessionManager.java
│   ├── ui/
│   │   ├── FindBackTheme.java   # Colors, fonts, and styled buttons
│   │   ├── LoginDialog.java
│   │   ├── MainFrame.java       # Student / Admin main host frame
│   │   ├── RegisterDialog.java
│   │   ├── SidebarPanel.java    # Dynamic navigation sidebar
│   │   ├── WelcomeFrame.java    # Animated Metropolitan University welcome screen
│   │   ├── admin/
│   │   │   ├── AdminAnalyticsPanel.java
│   │   │   ├── AdminCategoriesPanel.java
│   │   │   ├── AdminClaimsPanel.java
│   │   │   ├── AdminDashboardPanel.java
│   │   │   ├── AdminHandoversPanel.java
│   │   │   ├── AdminLocationsPanel.java
│   │   │   ├── AdminReportsPanel.java
│   │   │   └── AdminUsersPanel.java
│   │   ├── components/
│   │   │   ├── ModernButton.java
│   │   │   ├── RoundedPanel.java
│   │   │   └── StatsCard.java
│   │   └── student/
│   │       ├── BrowsePanel.java
│   │       ├── ClaimsPanel.java
│   │       ├── DashboardPanel.java
│   │       ├── FoundReportPanel.java
│   │       ├── LostReportPanel.java
│   │       ├── MatchesPanel.java
│   │       ├── NotificationsPanel.java
│   │       └── ProfilePanel.java
│   └── util/
│       ├── DateComparisonUtil.java
│       ├── ImageUtil.java
│       └── TextSimilarityUtil.java
└── pom.xml
```

---

## 3. Core Classes, Responsibilities & Key Methods

### A. Configuration & Connectivity
- **`DatabaseConnection.java`**: Manages the centralized HikariCP datasource. Attempts connecting to MySQL 8 on port 3306 first; if MySQL is unavailable or offline, it gracefully falls back to an embedded H2 database in MySQL mode. Executes database scripts via a quote-aware and comment-aware lexical SQL parser (`splitSqlStatements`).

### B. Security & Authentication
- **`AuthService.java`**: Hashes passwords using BCrypt (`BCrypt.hashpw(raw, BCrypt.gensalt(10))`), verifies entered credentials against stored hashes, enforces unique email constraints, and returns authenticated `User` objects.
- **`SessionManager.java`**: Thread-safe singleton storing the currently logged-in `User` and notifying listeners when user sessions change.

### C. Smart Matching Engine
- **`MatchingService.java`**: The core AI matching engine. Compares open LOST reports with open FOUND reports without requiring external cloud APIs.
  - **Category (25 points):** Exact ID match yields 25 pts.
  - **Location (25 points):** Exact campus location yields 25 pts.
  - **Date (20 points):** Calculated via `DateComparisonUtil` (100% for same day, decays linearly within 14 days).
  - **Color (15 points):** Exact or substring color overlap yields 15 pts.
  - **Description (15 points):** Tokenized Jaccard text similarity with stop-word filtering via `TextSimilarityUtil`.
  - **Thresholds:** Scores $\ge 70\%$ create `Match` records; scores $\ge 85\%$ trigger high-confidence notification alerts.

---

## 4. Frequently Asked Questions (FAQ)

#### Q1: What is FindBack?
**A:** FindBack is a Java Swing desktop application designed for universities to help students and staff report, locate, match, and claim lost and found belongings on campus.

#### Q2: What technologies are used in this project?
**A:** Pure Java 21, Java Swing, FlatLaf UI, MySQL 8 / Embedded H2, HikariCP, BCrypt, and JFreeChart.

#### Q3: Does FindBack require an internet connection or paid AI API keys?
**A:** No. The AI matching algorithm runs 100% locally on your machine using an explainable mathematical multi-factor scoring model.

#### Q4: What happens if MySQL is not running on my computer?
**A:** FindBack detects the connection failure within milliseconds and automatically switches to embedded H2 database running in MySQL compatibility mode. All features work out of the box with zero setup required.

#### Q5: How do I run the application?
**A:** Run `mvn clean package` followed by `java -jar target/findback-1.0.0.jar`, or click "Run" in your IDE.

#### Q6: How does password security work?
**A:** Passwords are never stored as plain text. They are hashed using BCrypt with a cost factor of 10.

#### Q7: What roles exist in the system?
**A:** `ROLE_STUDENT`, `ROLE_ADMIN`, and `ROLE_MODERATOR`.

#### Q8: Can students access administrative panels?
**A:** No. The sidebar menu and navigation dynamically hide admin tabs unless the logged-in user possesses `ROLE_ADMIN` or `ROLE_MODERATOR`.

#### Q9: How are images stored when an item photograph is uploaded?
**A:** Uploaded images are copied to the `uploads/` directory with a unique UUID-based filename.

#### Q10: What image formats are supported?
**A:** JPG, JPEG, and PNG images up to 5MB.

#### Q11: How does the Smart Matching Engine calculate total score?
**A:** It computes a weighted sum out of 100 points: Category (25), Location (25), Date Chronology (20), Color (15), and Description (15).

#### Q12: Why are two lost reports never compared?
**A:** Lost reports represent items missing from owners; they cannot match other lost items. Matches only make sense between a lost report and a found report.

#### Q13: Can users trigger the AI matching engine manually?
**A:** Yes. Under the "Smart Matches" screen, users can click the "✨ Run AI Match Engine" button to instantly scan all open reports.

#### Q14: How does a student claim a matched item?
**A:** Students select a match and click "Send Request to Get Item". They provide confidential proof of ownership (e.g. serial number, lock code, inside markings) which is submitted to staff for review.

#### Q15: Can a student claim an item from the Browse screen?
**A:** Yes. Clicking on any FOUND item in the Browse screen allows clicking "Send Claim / Request Item".

#### Q16: Who reviews and approves claims?
**A:** Administrators and moderators review submitted evidence in the Admin Claims Panel and either approve or reject the claim.

#### Q17: What is a handover record?
**A:** Once an ownership claim is verified and approved, physical handover of the item to the student is recorded with student ID, campus official signature/ID, and timestamp.

#### Q18: Can reports be closed or deleted?
**A:** Yes. Users can close their reports when found. Administrators can permanently delete reports or mark them as duplicates.

#### Q19: What categories are supported out of the box?
**A:** Electronics, Bags, Wallets, Documents, Keys, Clothing, Books, Accessories, and Others.

#### Q20: Can administrators add or remove categories?
**A:** Yes. The Category Management panel includes "+ Add New Category", "Toggle Active / Inactive", and "Remove Category" buttons.

#### Q21: Can administrators add or remove campus locations?
**A:** Yes. The Campus Location Management panel includes "+ Add Campus Location", "Toggle Active / Inactive", and "Remove Location" buttons.

#### Q22: Can administrators manage users?
**A:** Yes. The User Management panel allows changing roles (Student, Moderator, Admin), suspending/activating accounts, and removing users.

#### Q23: What is the Welcome Screen animation?
**A:** The Welcome Screen features a 60 FPS character typewriter reveal and a continuous living gradient shimmer for "WELCOME TO METROPOLITAN UNIVERSITY" overlaid on a campus photograph.

#### Q24: What sample credentials can be used for testing?
**A:**
- Admin: `admin@findback.edu` / `Admin@123`
- Student: `tanvir@student.findback.edu` / `Student@123`
- Moderator: `mod@findback.edu` / `Mod@123`

#### Q25: Where is the database schema located?
**A:** `database/schema.sql` and `database/findback.sql`.

#### Q26: Does FindBack support exporting data?
**A:** Yes. Admin reports and handover receipts can be exported or printed as text/CSV records.

#### Q27: How does date proximity scoring work?
**A:** If both lost and found events occurred on the exact same date, the score is 20/20. The score decreases linearly by 1.4 points per day difference up to 14 days.

#### Q28: How does text similarity work for item descriptions?
**A:** Descriptions are cleaned, lowercased, stripped of punctuation and stop words, and compared using the Jaccard similarity index: $|A \cap B| / |A \cup B|$.

#### Q29: What happens when a match score is greater than 85%?
**A:** The match is classified as a "Strong Potential Match" and notifications are automatically sent to both the owner and the finder.

#### Q30: How are notifications displayed?
**A:** Students have a dedicated "Notifications" tab in the sidebar showing unread alerts with time stamps.

#### Q31: Is the UI responsive to screen resizing?
**A:** Yes. Layouts utilize GridBagLayout, BorderLayout, and responsive column weights to adapt smoothly between standard resolutions and 4K displays.

#### Q32: What fonts are used in the application?
**A:** High-readability geometric sans-serif fonts including Segoe UI, Century Gothic, and system native UI fonts.

#### Q33: How does the application prevent duplicate matches?
**A:** The `matches` table enforces a unique constraint on `(lost_report_id, found_report_id)`, and `MatchingService` checks `matchDao.exists(...)` before scoring.

#### Q34: What prevents duplicate email registration?
**A:** A unique index on `users.email` and validation inside `AuthService.register()`.

#### Q35: Where are H2 database files saved when MySQL is offline?
**A:** In the `./data/` directory relative to the application working directory.

#### Q36: How does concurrent database access work with H2?
**A:** The JDBC URL includes `;AUTO_SERVER=TRUE` allowing multiple JVM processes or test runners to safely share the database file without lock collisions.

#### Q37: Can students edit their profile information?
**A:** Yes. In the "My Profile" tab, students can update their full name, phone number, and department.

#### Q38: What logging framework is used?
**A:** SLF4J with Logback for clear, structured console and file logging.

#### Q39: How is the visual theme configured?
**A:** `FindBackTheme.java` defines primary `#2563EB`, background `#F5F7FB`, card `#FFFFFF`, text `#172B4D`, success `#16A34A`, warning `#F59E0B`, and danger `#DC2626`.

#### Q40: Can FindBack be packaged into a standalone runnable JAR?
**A:** Yes. Running `mvn clean package` generates `target/findback-1.0.0.jar` with all dependencies packaged by `maven-shade-plugin`.
