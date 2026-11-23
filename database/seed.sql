-- ====================================================================
-- FindBack: Seed Demonstration Data
-- Metropolitan University, Sylhet, Bangladesh
-- All passwords are BCrypt hashed:
-- 'Admin@123' -> $2a$10$X8L2wFvMhK.s6W7P0Hj0bOuW8Fq3fK5bK9q1n4m2j8x1w0k9e8a1q
-- 'Mod@123'   -> $2a$10$Y1K3vGvMhL.t7X8Q1Ik1cPvX9Gr4gL6cL0r2o5n3k9y2x1l0f9b2r
-- 'Student@123' -> $2a$10$8.4k9/66aE6rW9d5R2.xfe8fFw8w.8lPqE.1p1eQe/4KqM2z9V7dK
-- ====================================================================

-- 1. Users
INSERT INTO users (id, full_name, email, password_hash, student_id, department, phone_number, role, active)
VALUES 
(1, 'Prof. Enamul Kabir (Proctor)', 'admin@findback.edu', '$2a$10$8hNNt7TrTc8x2NQBR95Yzulk.a4/8uSjAxxK15cPAQUsvuzMnB2X2', 'ADMIN-001', 'Campus Proctor & Security', '+8801711000001', 'ADMIN', TRUE),
(2, 'Sazzad Hossain (Staff)', 'moderator@findback.edu', '$2a$10$4OQ1vtZ74.lkKm9qYXdp2uZrsSseeGFyCl6hJIPKoqUmJ9UxFuDQ.', 'STAFF-042', 'Central Security Office', '+8801711000002', 'MODERATOR', TRUE),
(3, 'Tanvir Ahmed', 'student@findback.edu', '$2a$10$GXVT8xGG4IklXsrg5uIBI.sIG1riXbIjFshFhvDNO89klCbIBhXlO', '211-115-045', 'Computer Science & Engineering', '+8801711000003', 'STUDENT', TRUE),
(4, 'Samir Qureshi', 'samir@findback.edu', '$2a$10$GXVT8xGG4IklXsrg5uIBI.sIG1riXbIjFshFhvDNO89klCbIBhXlO', '212-115-089', 'Software Engineering', '+8801711000004', 'STUDENT', TRUE),
(5, 'Nusrat Jahan', 'nusrat@findback.edu', '$2a$10$GXVT8xGG4IklXsrg5uIBI.sIG1riXbIjFshFhvDNO89klCbIBhXlO', '213-115-102', 'Electrical & Electronic Engineering', '+8801711000005', 'STUDENT', TRUE);

-- 2. Categories
INSERT INTO categories (id, name, description, icon_name, active) VALUES
(1, 'Electronics', 'Laptops, smartphones, chargers, headphones, calculators, USB drives', 'laptop', TRUE),
(2, 'Wallet', 'Leather wallets, coin purses, cardholders, and cash pouches', 'wallet', TRUE),
(3, 'Documents', 'University student ID cards, library cards, national IDs, licenses', 'file-text', TRUE),
(4, 'Keys', 'Dorm keys, motorcycle keys, departmental access cards, keychains', 'key', TRUE),
(5, 'Bags', 'Laptop bags, university backpacks, tote bags, pouches', 'briefcase', TRUE),
(6, 'Books', 'Textbooks, engineering drawing sheets, lecture spiral notebooks', 'book', TRUE),
(7, 'Clothing', 'Jackets, watches, university hoodies, caps, umbrellas', 'tag', TRUE),
(8, 'Accessories', 'Prescription glasses, sunglasses, water bottles, pen boxes', 'glasses', TRUE),
(9, 'Other', 'Miscellaneous campus belongings and unclaimed articles', 'package', TRUE);

-- 3. Campus Locations (Metropolitan University, Sylhet)
INSERT INTO campus_locations (id, name, zone, description, active) VALUES
(1, 'Library', 'Academic Zone A', '3rd Floor Main Silent Reading Section & Circulation Desk', TRUE),
(2, 'Computer Lab', 'Academic Zone A', '4th Floor CSE Advanced Programming & AI Lab 402', TRUE),
(3, 'Classroom', 'Academic Zone B', '2nd Floor Lecture Room 204 & Engineering Hallway', TRUE),
(4, 'Cafeteria', 'Student Amenities Zone', 'Ground Floor Student Canteen & Patio', TRUE),
(5, 'Administrative Building', 'Administrative Zone', 'Registrar Wing, Proctor Office, & Accounts Desk', TRUE),
(6, 'Auditorium', 'Cultural Complex', 'Prof. M. Habibur Rahman Auditorium Main Hall & Sound Booth', TRUE),
(7, 'Main Gate', 'Perimeter Zone', 'Entrance Security Gate 1 & Visitor Waiting Lounge', TRUE),
(8, 'Campus Ground', 'Outdoor Sports Complex', 'Pavilion, Cricket Ground, & Basketball Court', TRUE);

-- 4. Demonstration Reports
-- Lost Item 1 (by Tanvir)
INSERT INTO item_reports (id, user_id, category_id, location_id, report_type, title, description, color, specific_location, event_date, image_path, status, custody_status)
VALUES (1, 3, 2, 1, 'LOST', 'Black Leather Wallet', 'Lost black bi-fold leather wallet. Contains Metropolitan University student ID card and cash. Lost near the 3rd floor quiet study desks.', 'Black', '3rd Floor reading desk #24', CURRENT_DATE - 1, 'uploads/items/wallet_lost.svg', 'OPEN', 'WITH_FINDER');

-- Found Item 1 (by Samir) - Matches Lost Item 1
INSERT INTO item_reports (id, user_id, category_id, location_id, report_type, title, description, color, specific_location, event_date, image_path, status, custody_status)
VALUES (2, 4, 2, 1, 'FOUND', 'Black Leather Wallet Found Near Desks', 'Found a black leather wallet on a study desk in the central library. Handed over to the library security desk for safekeeping.', 'Black', 'Library 3rd Floor study section', CURRENT_DATE - 1, 'uploads/items/wallet_found.svg', 'OPEN', 'SECURITY_DESK');

-- Lost Item 2 (by Nusrat)
INSERT INTO item_reports (id, user_id, category_id, location_id, report_type, title, description, color, specific_location, event_date, image_path, status, custody_status)
VALUES (3, 5, 5, 2, 'LOST', 'Navy Blue Laptop Backpack', 'Navy blue water-resistant laptop backpack containing Dell laptop charger, notebook, and blue pen pouch. Left behind after CSE lab session.', 'Navy Blue', 'Lab 402, row 3 PC workstation', CURRENT_DATE - 2, 'uploads/items/backpack_found.svg', 'OPEN', 'WITH_FINDER');

-- Found Item 2 (by Tanvir) - Matches Lost Item 2
INSERT INTO item_reports (id, user_id, category_id, location_id, report_type, title, description, color, specific_location, event_date, image_path, status, custody_status)
VALUES (4, 3, 5, 2, 'FOUND', 'Blue Laptop Backpack in Lab', 'Found a blue laptop bag in Computer Software Lab 402 after evening classes. Safely kept with the lab attendant.', 'Blue', 'Lab 402 PC 14', CURRENT_DATE - 1, 'uploads/items/backpack_found.svg', 'OPEN', 'DEPARTMENT_OFFICE');

-- Lost Item 3 (by Samir)
INSERT INTO item_reports (id, user_id, category_id, location_id, report_type, title, description, color, specific_location, event_date, image_path, status, custody_status)
VALUES (5, 4, 1, 3, 'LOST', 'Casio fx-991EX Scientific Calculator', 'Black and white scientific calculator with small scratch on the solar panel. Needed urgently for midterm exam.', 'Black', 'Room 204 back row bench', CURRENT_DATE - 3, 'uploads/items/calc_lost.svg', 'OPEN', 'WITH_FINDER');

-- Found Item 3 (by Nusrat) - Matches Lost Item 3
INSERT INTO item_reports (id, user_id, category_id, location_id, report_type, title, description, color, specific_location, event_date, image_path, status, custody_status)
VALUES (6, 5, 1, 3, 'FOUND', 'Casio Calculator fx-991EX', 'Found a Casio scientific calculator on a classroom bench in Academic Building B Room 204. Currently with me.', 'Black', 'Classroom 204 middle desk', CURRENT_DATE - 2, 'uploads/items/calc_lost.svg', 'OPEN', 'WITH_FINDER');

-- 5. Smart Matches Generated
INSERT INTO matches (id, lost_report_id, found_report_id, total_score, category_score, location_score, date_score, color_score, description_score, explanation, status)
VALUES
(1, 1, 2, 95.0, 25.0, 25.0, 20.0, 15.0, 10.0, 'Category match (25/25 pts); Location match (25/25 pts); Date proximity: 0 days difference (20/20 pts); Exact color match (15/15 pts); Description similarity: 42% (10/15 pts)', 'POTENTIAL'),
(2, 3, 4, 91.0, 25.0, 25.0, 18.0, 13.0, 10.0, 'Category match (25/25 pts); Location match (25/25 pts); Date proximity: 1 day difference (18/20 pts); Compatible color match (13/15 pts); Description similarity: 38% (10/15 pts)', 'POTENTIAL'),
(3, 5, 6, 92.0, 25.0, 25.0, 18.0, 15.0, 9.0, 'Category match (25/25 pts); Location match (25/25 pts); Date proximity: 1 day difference (18/20 pts); Exact color match (15/15 pts); Description similarity: 35% (9/15 pts)', 'POTENTIAL');

-- 6. Demonstration Claims
INSERT INTO claims (id, claimant_id, lost_report_id, found_report_id, match_id, proof_description, proof_image_path, status, admin_remarks, reviewed_by_id, reviewed_at)
VALUES 
(1, 3, 1, 2, 1, 'My Metropolitan University ID card has student number 211-115-045 and is inside the middle card slot. Inside there is also a 500 BDT note.', 'uploads/items/wallet_lost.svg', 'APPROVED', 'Verified with library circulation records and student ID database.', 1, CURRENT_TIMESTAMP);

-- 7. Demonstration Handover
INSERT INTO handovers (id, claim_id, lost_report_id, found_report_id, handed_by_id, received_by_id, staff_witness_id, handover_date, venue, remarks, receipt_code, completed)
VALUES 
(1, 1, 1, 2, 4, 3, 1, CURRENT_TIMESTAMP, 'Central Campus Security & Proctor Office, Ground Floor', 'Returned to verified student Tanvir Ahmed after identity validation.', 'FB-RCV-839201-MU', TRUE);

-- 8. Notifications
INSERT INTO notifications (user_id, title, message, target_url, type, is_read)
VALUES
(3, 'High Confidence Smart Match Found!', 'A found Black Leather Wallet reported at Central University Library matches your lost report with 95% confidence.', 'MATCH:1', 'MATCH_FOUND', FALSE),
(5, 'Smart Match Detected', 'A Navy Blue Laptop Backpack found in Computer Lab matches your lost item with 91% confidence.', 'MATCH:2', 'MATCH_FOUND', FALSE);

-- 9. Admin Audit Actions
INSERT INTO admin_actions (admin_id, action_type, target_entity, target_id, details)
VALUES
(1, 'SYSTEM_INIT', 'SYSTEM', 1, 'FindBack Smart Lost & Found Portal initialized for Metropolitan University.');
