# FindBack – Smart Lost & Found Portal
### University Java Swing Desktop Application | Metropolitan University, Sylhet, Bangladesh

FindBack is an enterprise-grade, university-focused **Java Swing desktop application** designed for students, faculty, and administrative campus staff at Metropolitan University, Sylhet. It features an explainable, rule-based **Smart Matching Engine** (0–100 score) that automatically connects lost belongings with found items across campus.

---

## 🚀 Key Features

1. **Modern SaaS UI with FlatLaf**:
   - Custom Navy Blue & White palette (`#142D4E`, `#2563EB`, `#F5F7FB`, `#FFFFFF`).
   - Rounded cards, smooth buttons, custom typography, and icons.
   - Welcome/landing hero screen featuring lost-and-found items (Backpack, Wallet, Keys, Headphones, Phone, Books, ID Card) with **zero human figures**.
   - Responsive multi-screen navigation using `CardLayout` and persistent modern sidebar.

2. **Smart Matching Engine (`MatchingService.java`)**:
   - **Explainable Weighted Scoring (0–100 points)**:
     - **Category Match**: 25 points
     - **Campus Location Proximity**: 25 points
     - **Date Chronological Proximity**: 20 points
     - **Color Similarity (exact & compatible groups)**: 15 points
     - **Description Text Similarity (Jaccard NLP token overlap)**: 15 points
   - Generates transparent, human-readable breakdown explanations for every potential match.
   - Automatically alerts students when matches $\ge 70\%$ are detected.

3. **Student Workflow**:
   - **Report Lost Item**: Title, category, location, specific location, date, color, description, and photo upload.
   - **Report Found Item**: Public item title, category, location, custody status (`WITH_FINDER`, `SECURITY_DESK`, `DEPARTMENT_OFFICE`, etc.).
   - **Browse & Search**: Filter by keyword, category, lost/found type, campus location, and color.
   - **Smart Matches**: Review computed pairs side-by-side and submit ownership claims.
   - **Claims Tracking**: View claim verification status and private ownership evidence.
   - **Notifications**: In-portal real-time notifications for matches and decisions.
   - **Profile**: Student credentials, department, and contact details.

4. **Administrator & Security Control Center**:
   - **Executive Dashboard**: Key metrics (total users, lost/found counts, potential matches, pending claims, recovered items).
   - **JFreeChart Analytics**: Interactive pie charts for category distribution and bar charts for campus location hot-spots.
   - **Report Supervision**: Close duplicates and review report details.
   - **Claim Verification**: Inspect confidential evidence submitted by claimants and approve/reject claims.
   - **Official Handover System**: Execute item handovers, generate tamper-evident receipt codes (`FB-RCV-XXXXXX-MU`), and notify parties.
   - **User & Category/Location Management**: Toggle account status, manage roles, and customize university categories and locations.
   - **Audit Logs**: Comprehensive administrative audit tracking.

5. **Robust Database Layer (MySQL 8 + Embedded Fallback)**:
   - Centralized `DatabaseConnection.java` with HikariCP connection pooling.
   - Fully supports **MySQL 8** out-of-the-box (`database/schema.sql` and `database/seed.sql`).
   - Automatically falls back to embedded **H2 in MySQL compatibility mode** if local MySQL is offline, guaranteeing immediate zero-configuration execution.

---

## 🛠 Technology Stack

- **Language**: Java 21
- **UI Toolkit**: Java Swing with FlatLaf (`com.formdev:flatlaf`)
- **Database**: MySQL 8 & H2 (Dual-mode JDBC via HikariCP)
- **Charts & Analytics**: JFreeChart (`org.jfree:jfreechart`)
- **Security**: BCrypt (`org.mindrot:jbcrypt`)
- **Build System**: Maven (`pom.xml`)
- **Testing**: JUnit 5 & Mockito

---

## 💻 Running the Application

### Option 1: Run the Pre-Built Executable JAR
```bash
java -jar target/findback-1.0.0.jar
```

### Option 2: Build and Run via Maven
```bash
# Compile and run unit tests
mvn clean test

# Package shaded uber-jar
mvn package

# Run
java -jar target/findback-1.0.0.jar
```

---

## 🔑 Demonstration Accounts

All demo accounts come pre-seeded with BCrypt hashed passwords:

| Role | Email | Password | Full Name / Description |
|---|---|---|---|
| **Admin** | `admin@findback.edu` | `Admin@123` | Prof. Enamul Kabir (Campus Proctor) |
| **Moderator** | `moderator@findback.edu` | `Mod@123` | Sazzad Hossain (Security Staff) |
| **Student 1** | `student@findback.edu` | `Student@123` | Tanvir Ahmed (CSE Dept) |
| **Student 2** | `samir@findback.edu` | `Student@123` | Samir Qureshi (SWE Dept) |
| **Student 3** | `nusrat@findback.edu` | `Student@123` | Nusrat Jahan (EEE Dept) |

---

## 📁 Project Architecture (MVC + DAO + Service)

```
src/main/java/com/findback/
├── Main.java                         # Application entrypoint
├── config/
│   ├── AppConfig.java                # Application constants & upload paths
│   └── DatabaseConnection.java       # Centralized JDBC connection pool & fallback
├── dao/                              # JDBC Data Access Objects
│   ├── AdminActionDao.java
│   ├── CategoryDao.java
│   ├── ClaimDao.java
│   ├── HandoverDao.java
│   ├── LocationDao.java
│   ├── MatchDao.java
│   ├── NotificationDao.java
│   ├── ReportDao.java
│   └── UserDao.java
├── enums/                            # Strongly typed domain enums
│   ├── ClaimStatus.java
│   ├── CustodyStatus.java
│   ├── MatchStatus.java
│   ├── NotificationType.java
│   ├── ReportStatus.java
│   ├── ReportType.java
│   └── Role.java
├── model/                            # Clean domain POJOs
│   ├── AdminAction.java
│   ├── CampusLocation.java
│   ├── Category.java
│   ├── Claim.java
│   ├── Handover.java
│   ├── ItemReport.java
│   ├── Match.java
│   ├── Notification.java
│   └── User.java
├── service/                          # Pure Java business logic
│   ├── AdminService.java
│   ├── AuthService.java              # BCrypt authentication & registration
│   ├── ClaimService.java
│   ├── FileStorageService.java
│   ├── HandoverService.java
│   ├── MatchingService.java          # Core 0-100 Smart Matching Engine
│   ├── NotificationService.java
│   ├── ReportService.java
│   └── SessionManager.java           # Active session state
├── ui/                               # Modern Java Swing interface (FlatLaf)
│   ├── FindBackTheme.java            # SaaS theme, custom fonts & buttons
│   ├── LoginDialog.java
│   ├── MainFrame.java                # CardLayout navigation shell
│   ├── RegisterDialog.java
│   ├── SidebarPanel.java             # Navigation sidebar with badges
│   ├── WelcomeFrame.java             # Landing screen with lost-item graphics
│   ├── admin/                        # 8 Administrator Panels & JFreeChart analytics
│   ├── components/                   # Custom StatsCard, RoundedPanel, ModernButton
│   └── student/                      # 8 Student Panels (Dashboard, Reports, Browse, etc.)
└── util/
    ├── DateComparisonUtil.java       # Proximity date scoring
    └── TextSimilarityUtil.java       # Jaccard text similarity & color groups
```

---

## 🧪 Unit Tests

Run test suite:
```bash
mvn test
```
Includes comprehensive tests for:
- 100% weighted score calculations in `MatchingServiceTest`.
- Proximity date decay in `DateComparisonUtil`.
- Jaccard similarity and color compatibility in `TextSimilarityUtil`.
- BCrypt password encryption and validation in `AuthServiceTest`.
