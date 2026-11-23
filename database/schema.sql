-- ====================================================================
-- FindBack: Smart Lost & Found Portal Database Schema (MySQL 8 / ANSI)
-- Metropolitan University, Sylhet, Bangladesh
-- ====================================================================

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    student_id VARCHAR(50),
    department VARCHAR(100),
    phone_number VARCHAR(30),
    role VARCHAR(30) NOT NULL DEFAULT 'STUDENT',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    icon_name VARCHAR(50) DEFAULT 'tag',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS campus_locations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(120) NOT NULL UNIQUE,
    zone VARCHAR(100),
    description VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS item_reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    location_id BIGINT NOT NULL,
    report_type VARCHAR(20) NOT NULL,
    title VARCHAR(150) NOT NULL,
    description TEXT NOT NULL,
    color VARCHAR(60) NOT NULL,
    specific_location VARCHAR(200),
    event_date DATE NOT NULL,
    image_path VARCHAR(255),
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    custody_status VARCHAR(40) DEFAULT 'WITH_FINDER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id),
    FOREIGN KEY (location_id) REFERENCES campus_locations(id)
);

CREATE TABLE IF NOT EXISTS item_images (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_id BIGINT NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_size BIGINT,
    mime_type VARCHAR(100),
    is_primary BOOLEAN DEFAULT FALSE,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (report_id) REFERENCES item_reports(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS matches (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lost_report_id BIGINT NOT NULL,
    found_report_id BIGINT NOT NULL,
    total_score DOUBLE NOT NULL,
    category_score DOUBLE NOT NULL,
    location_score DOUBLE NOT NULL,
    date_score DOUBLE NOT NULL,
    color_score DOUBLE NOT NULL,
    description_score DOUBLE NOT NULL,
    explanation TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'POTENTIAL',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE (lost_report_id, found_report_id),
    FOREIGN KEY (lost_report_id) REFERENCES item_reports(id) ON DELETE CASCADE,
    FOREIGN KEY (found_report_id) REFERENCES item_reports(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS claims (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    claimant_id BIGINT NOT NULL,
    lost_report_id BIGINT NOT NULL,
    found_report_id BIGINT NOT NULL,
    match_id BIGINT,
    proof_description TEXT NOT NULL,
    proof_image_path VARCHAR(255),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    admin_remarks TEXT,
    reviewed_by_id BIGINT,
    reviewed_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (claimant_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (lost_report_id) REFERENCES item_reports(id) ON DELETE CASCADE,
    FOREIGN KEY (found_report_id) REFERENCES item_reports(id) ON DELETE CASCADE,
    FOREIGN KEY (match_id) REFERENCES matches(id) ON DELETE SET NULL,
    FOREIGN KEY (reviewed_by_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,
    target_url VARCHAR(255),
    type VARCHAR(50) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS handovers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    claim_id BIGINT NOT NULL,
    lost_report_id BIGINT NOT NULL,
    found_report_id BIGINT NOT NULL,
    handed_by_id BIGINT,
    received_by_id BIGINT NOT NULL,
    staff_witness_id BIGINT NOT NULL,
    handover_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    venue VARCHAR(150) NOT NULL,
    remarks TEXT,
    receipt_code VARCHAR(60) NOT NULL UNIQUE,
    completed BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (claim_id) REFERENCES claims(id) ON DELETE CASCADE,
    FOREIGN KEY (lost_report_id) REFERENCES item_reports(id),
    FOREIGN KEY (found_report_id) REFERENCES item_reports(id),
    FOREIGN KEY (handed_by_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (received_by_id) REFERENCES users(id),
    FOREIGN KEY (staff_witness_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS admin_actions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    admin_id BIGINT NOT NULL,
    action_type VARCHAR(60) NOT NULL,
    target_entity VARCHAR(60) NOT NULL,
    target_id BIGINT,
    details TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (admin_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Standalone Indexes (Compatible with MySQL 8 and H2)
CREATE INDEX IF NOT EXISTS idx_user_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_user_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_category_name ON categories(name);
CREATE INDEX IF NOT EXISTS idx_location_name ON campus_locations(name);
CREATE INDEX IF NOT EXISTS idx_report_type_status ON item_reports(report_type, status);
CREATE INDEX IF NOT EXISTS idx_report_category ON item_reports(category_id);
CREATE INDEX IF NOT EXISTS idx_report_location ON item_reports(location_id);
CREATE INDEX IF NOT EXISTS idx_report_date ON item_reports(event_date);
CREATE INDEX IF NOT EXISTS idx_match_score ON matches(total_score);
CREATE INDEX IF NOT EXISTS idx_claim_status ON claims(status);
CREATE INDEX IF NOT EXISTS idx_notif_user_read ON notifications(user_id, is_read);
CREATE INDEX IF NOT EXISTS idx_handover_code ON handovers(receipt_code);
CREATE INDEX IF NOT EXISTS idx_action_type ON admin_actions(action_type);
