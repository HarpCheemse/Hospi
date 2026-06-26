-- =============================================================================
-- Seed Test Data for Hospi
-- =============================================================================

BEGIN;

-- ────────────────────────────────────────────────────────────────────────────
-- Cleanup
-- ────────────────────────────────────────────────────────────────────────────
DELETE
FROM reviews;
DELETE
FROM payments;
DELETE
FROM room_assignments;
DELETE
FROM staying_guests;
DELETE
FROM reservation_details;
DELETE
FROM reservations;
DELETE
FROM rooms;
DELETE
FROM room_type_pictures;
DELETE
FROM room_types;
DELETE
FROM hotel_pictures;
DELETE
FROM hotels;
DELETE
FROM system_configs;
DELETE
FROM staffs;
DELETE
FROM otp_challenges;

-- ────────────────────────────────────────────────────────────────────────────
-- 1. Staffs
-- ────────────────────────────────────────────────────────────────────────────
INSERT INTO staffs (full_name, email, password_hash, phone, role, status, created_at)
VALUES ('Alice Admin', 'admin@gmail.com', '123', '+84 901 000 001', 'ADMIN', 'ACTIVE', NOW()),
       ('Leo Leader', 'leader@gmail.com', '123', '+84 901 000 002', 'LEADER', 'ACTIVE', NOW()),
       ('Rita Receptionist', 'receptionist@gmail.com', '123', '+84 901 000 003', 'RECEPTIONIST', 'ACTIVE', NOW()),
       ('Mike Manager', 'manager@gmail.com', '123', '+84 901 000 004', 'MANAGER', 'ACTIVE', NOW());

-- ────────────────────────────────────────────────────────────────────────────
-- 2. Hotel
-- ────────────────────────────────────────────────────────────────────────────
INSERT INTO hotels (name, description, address, phone, email,
                    average_rating, review_count, features, check_in_time, check_out_time,
                    floor_count, status, term_of_service, created_at, updated_at)
VALUES ('Hospi Grand Hotel',
        'A modern boutique hotel in the heart of the city, offering luxurious rooms and exceptional service.',
        '123 Nguyen Hue Street, District 1, Ho Chi Minh City',
        '+84 28 3822 1234',
        'info@hospigrand.com',
        4.7, 128,
        'Free WiFi, Pool, Gym, Restaurant, Bar, 24/7 Room Service, Parking, Airport Shuttle',
        '14:00', '12:00',
        10, 'ACTIVE',
        'Check-in from 14:00. Check-out before 12:00. Photo ID required upon check-in.',
        NOW(), NOW());

-- ────────────────────────────────────────────────────────────────────────────
-- 3. System Config
-- ────────────────────────────────────────────────────────────────────────────
INSERT INTO system_configs (hotel_id, tax_rate, default_deposit_percentage,
                            late_checkout_fee, extra_guest_fee, pending_booking_expiry_minutes,
                            cancellation_hours_before_checkin, maximum_booking_days, maximum_room_per_book,
                            refund_percentage, full_refund_window_hours)
VALUES (1, 0.10, 30.00, 50.00, 25.00, 30, 24, 30, 5, 100.00, 48);

-- ────────────────────────────────────────────────────────────────────────────
-- 4. Room Types (all using allowed tier values: BASIC, SUPERIOR, DELUXE)
-- ────────────────────────────────────────────────────────────────────────────
INSERT INTO room_types (name, max_occupancy, description, features, bed_type,
                        area, base_price, category, tier, is_active, created_at, updated_at)
VALUES        ('BASIC DOUBLE', 2, 'Comfortable room with essential amenities',
        'TV, Desk, Mini Bar, Air Conditioning, Ensuite Bathroom',
        'DOUBLE', 28, 85.00, 'DOUBLE', 'BASIC', true, NOW(), NOW()),
       ('DELUXE DOUBLE', 2, 'Superior room with premium furnishings',
        'TV, Desk, Mini Bar, Air Conditioning, Ensuite Bathroom, Bathtub, Safe',
        'QUEEN', 35, 145.00, 'DOUBLE', 'DELUXE', true, NOW(), NOW()),
       ('SUPERIOR DOUBLE', 3, 'Spacious suite with separate living area',
        'TV, Desk, Mini Bar, Air Conditioning, Ensuite Bathroom, Bathtub, Safe, Sofa, City View',
        'KING', 55, 280.00, 'DOUBLE', 'SUPERIOR', true, NOW(), NOW()),
       ('BASIC FAMILY', 4, 'Large room ideal for families',
        'TV, Desk, Mini Bar, Air Conditioning, Ensuite Bathroom, Two Beds, Play Area',
        'TWIN', 48, 220.00, 'FAMILY', 'BASIC', true, NOW(), NOW()),
       ('DELUXE SUITE', 4, 'Luxurious suite with panoramic views and premium amenities',
        'TV, Desk, Mini Bar, Air Conditioning, Ensuite Bathroom, Bathtub, Safe, Sofa, City View, Balcony, Kitchenette',
        'KING', 65, 450.00, 'SUITE', 'DELUXE', true, NOW(), NOW());

-- ────────────────────────────────────────────────────────────────────────────
-- 5. Rooms (120 total — 24 per room type)
-- ────────────────────────────────────────────────────────────────────────────

-- Standard Rooms (24)
INSERT INTO rooms (room_number, floor_number, is_active, occupancy_status, condition_status, room_type_id)
SELECT n, floor_num, true, 'VACANT', 'CLEAN', rt.id
FROM (VALUES ('S201', 2),
             ('S202', 2),
             ('S203', 2),
             ('S204', 2),
             ('S205', 2),
             ('S206', 2),
             ('S207', 2),
             ('S208', 3),
             ('S209', 3),
             ('S210', 3),
             ('S211', 3),
             ('S212', 5),
             ('S213', 5),
             ('S214', 5),
             ('S215', 5),
             ('S216', 6),
             ('S217', 6),
             ('S218', 6),
             ('S219', 6),
             ('S220', 8),
             ('S221', 8),
             ('S222', 8),
             ('S223', 8),
             ('S224', 8)) AS nums(n, floor_num),
     room_types rt
WHERE rt.name = 'BASIC DOUBLE';

-- Deluxe Rooms (24)
INSERT INTO rooms (room_number, floor_number, is_active, occupancy_status, condition_status, room_type_id)
SELECT n, floor_num, true, 'VACANT', 'CLEAN', rt.id
FROM (VALUES ('D401', 4),
             ('D402', 4),
             ('D403', 4),
             ('D404', 4),
             ('D405', 4),
             ('D406', 4),
             ('D407', 4),
             ('D408', 5),
             ('D409', 5),
             ('D410', 5),
             ('D411', 5),
             ('D412', 6),
             ('D413', 6),
             ('D414', 6),
             ('D415', 6),
             ('D416', 7),
             ('D417', 7),
             ('D418', 7),
             ('D419', 7),
             ('D420', 8),
             ('D421', 8),
             ('D422', 8),
             ('D423', 8),
             ('D424', 8)) AS nums(n, floor_num),
     room_types rt
WHERE rt.name = 'DELUXE DOUBLE';

-- Executive Suites (24)
INSERT INTO rooms (room_number, floor_number, is_active, occupancy_status, condition_status, room_type_id)
SELECT n, floor_num, true, 'VACANT', 'CLEAN', rt.id
FROM (VALUES ('E701', 7),
             ('E702', 7),
             ('E703', 7),
             ('E704', 7),
             ('E705', 7),
             ('E706', 7),
             ('E707', 7),
             ('E708', 8),
             ('E709', 8),
             ('E710', 8),
             ('E711', 8),
             ('E712', 9),
             ('E713', 9),
             ('E714', 9),
             ('E715', 9),
             ('E716', 10),
             ('E717', 10),
             ('E718', 10),
             ('E719', 10),
             ('E720', 5),
             ('E721', 6),
             ('E722', 7),
             ('E723', 8),
             ('E724', 9)) AS nums(n, floor_num),
     room_types rt
WHERE rt.name = 'SUPERIOR DOUBLE';

-- Family Rooms (24)
INSERT INTO rooms (room_number, floor_number, is_active, occupancy_status, condition_status, room_type_id)
SELECT n, floor_num, true, 'VACANT', 'CLEAN', rt.id
FROM (VALUES ('F301', 3),
             ('F302', 3),
             ('F303', 3),
             ('F304', 3),
             ('F305', 3),
             ('F306', 3),
             ('F307', 3),
             ('F308', 4),
             ('F309', 4),
             ('F310', 4),
             ('F311', 4),
             ('F312', 5),
             ('F313', 5),
             ('F314', 5),
             ('F315', 5),
             ('F316', 6),
             ('F317', 6),
             ('F318', 6),
             ('F319', 6),
             ('F320', 7),
             ('F321', 7),
             ('F322', 7),
             ('F323', 7),
             ('F324', 7)) AS nums(n, floor_num),
     room_types rt
WHERE rt.name = 'BASIC FAMILY';

-- Premium Suites (24)
INSERT INTO rooms (room_number, floor_number, is_active, occupancy_status, condition_status, room_type_id)
SELECT n, floor_num, true, 'VACANT', 'CLEAN', rt.id
FROM (VALUES ('P801', 8),
             ('P802', 8),
             ('P803', 8),
             ('P804', 8),
             ('P805', 8),
             ('P806', 8),
             ('P807', 8),
             ('P808', 9),
             ('P809', 9),
             ('P810', 9),
             ('P811', 9),
             ('P812', 9),
             ('P813', 10),
             ('P814', 10),
             ('P815', 10),
             ('P816', 10),
             ('P817', 10),
             ('P818', 7),
             ('P819', 7),
             ('P820', 7),
             ('P821', 7),
             ('P822', 9),
             ('P823', 10),
             ('P824', 8)) AS nums(n, floor_num),
     room_types rt
WHERE rt.name = 'DELUXE SUITE';

-- ────────────────────────────────────────────────────────────────────────────
-- 6. Update room statuses for realistic testing
-- ────────────────────────────────────────────────────────────────────────────
UPDATE rooms
SET occupancy_status = 'OCCUPIED'
WHERE room_number IN ('S208', 'S212', 'D409', 'D413', 'E708', 'E712', 'F309', 'F313', 'P802', 'P806');

UPDATE rooms
SET condition_status = 'DIRTY'
WHERE room_number IN ('S211', 'S216', 'D411', 'D416', 'E711', 'E716', 'F311', 'F316', 'P805', 'P810');

UPDATE rooms
SET condition_status = 'MAINTENANCE',
    is_active        = false
WHERE room_number IN ('S214', 'S220', 'D414', 'D420', 'E714', 'E720', 'F314', 'F320', 'P809', 'P815');

UPDATE rooms
SET occupancy_status = 'VACANT'
WHERE room_number IN ('S207', 'S215', 'D407', 'D415', 'E707', 'E715', 'F307', 'F315', 'P801', 'P812');

-- ────────────────────────────────────────────────────────────────────────────
-- 7. Reservations (50 total)
-- ────────────────────────────────────────────────────────────────────────────

-- CHECKED_IN (15)
INSERT INTO reservations (guest_name, guest_email, guest_phone, guest_date_of_birth,
                          guest_nationality, check_in_at, check_out_at, status, source, confirmation_code,
                          total_price, checked_in_at, checked_in_by, created_at, updated_at)
VALUES ('Nguyen Van An', 'an.nguyen@email.com', '+84 912 100 001', '1985-03-12', 'Vietnamese',
        CURRENT_DATE - 3, CURRENT_DATE + 1, 'CHECKED_IN', 'OFFLINE', NULL,
        0, CURRENT_TIMESTAMP - INTERVAL '3 days', 'Rita Receptionist', NOW(), NOW()),
       ('Tran Thi Binh', 'binh.tran@email.com', '+84 912 100 002', '1990-07-25', 'Vietnamese',
        CURRENT_DATE - 2, CURRENT_DATE + 2, 'CHECKED_IN', 'OFFLINE', NULL,
        0, CURRENT_TIMESTAMP - INTERVAL '2 days', 'Rita Receptionist', NOW(), NOW()),
       ('John Smith', 'john.smith@email.com', '+1 555 010 003', '1982-11-05', 'British',
        CURRENT_DATE - 1, CURRENT_DATE + 3, 'CHECKED_IN', 'ONLINE', 'HSP-A7K2M9',
        0, CURRENT_TIMESTAMP - INTERVAL '1 day', 'Rita Receptionist', NOW(), NOW()),
       ('Tanaka Yuki', 'yuki.tanaka@email.com', '+81 90 1234 0004', '1988-02-14', 'Japanese',
        CURRENT_DATE - 4, CURRENT_DATE + 0, 'CHECKED_IN', 'ONLINE', 'HSP-B3R8N1',
        0, CURRENT_TIMESTAMP - INTERVAL '4 days', 'Rita Receptionist', NOW(), NOW()),
       ('Pham Minh Chau', 'chau.pham@email.com', '+84 912 100 005', '1992-09-30', 'Vietnamese',
        CURRENT_DATE - 5, CURRENT_DATE - 1, 'CHECKED_IN', 'OFFLINE', NULL,
        0, CURRENT_TIMESTAMP - INTERVAL '5 days', 'Leo Leader', NOW(), NOW()),
       ('Emily Johnson', 'emily.j@email.com', '+1 555 010 006', '1995-06-18', 'American',
        CURRENT_DATE - 2, CURRENT_DATE + 4, 'CHECKED_IN', 'ONLINE', 'HSP-C5X1P7',
        0, CURRENT_TIMESTAMP - INTERVAL '2 days', 'Rita Receptionist', NOW(), NOW()),
       ('Le Hoang Nam', 'nam.le@email.com', '+84 912 100 007', '1980-01-22', 'Vietnamese',
        CURRENT_DATE - 6, CURRENT_DATE - 2, 'CHECKED_IN', 'OFFLINE', NULL,
        0, CURRENT_TIMESTAMP - INTERVAL '6 days', 'Rita Receptionist', NOW(), NOW()),
       ('Maria Garcia', 'maria.garcia@email.com', '+34 612 345 008', '1987-08-09', 'Spanish',
        CURRENT_DATE - 3, CURRENT_DATE + 1, 'CHECKED_IN', 'ONLINE', 'HSP-D9W4K2',
        0, CURRENT_TIMESTAMP - INTERVAL '3 days', 'Rita Receptionist', NOW(), NOW()),
       ('Doan Thi Lan', 'lan.doan@email.com', '+84 912 100 009', '1993-12-01', 'Vietnamese',
        CURRENT_DATE - 7, CURRENT_DATE - 3, 'CHECKED_IN', 'OFFLINE', NULL,
        0, CURRENT_TIMESTAMP - INTERVAL '7 days', 'Rita Receptionist', NOW(), NOW()),
       ('Chen Wei', 'wei.chen@email.com', '+86 138 0011 010', '1984-04-17', 'Chinese',
        CURRENT_DATE - 1, CURRENT_DATE + 5, 'CHECKED_IN', 'ONLINE', 'HSP-E2T5V8',
        0, CURRENT_TIMESTAMP - INTERVAL '1 day', 'Rita Receptionist', NOW(), NOW()),
       ('Bui Thanh Tung', 'tung.bui@email.com', '+84 912 100 011', '1991-05-28', 'Vietnamese',
        CURRENT_DATE - 8, CURRENT_DATE - 4, 'CHECKED_IN', 'OFFLINE', NULL,
        0, CURRENT_TIMESTAMP - INTERVAL '8 days', 'Leo Leader', NOW(), NOW()),
       ('Sarah Williams', 'sarah.w@email.com', '+44 7700 900012', '1979-10-03', 'British',
        CURRENT_DATE - 4, CURRENT_DATE + 0, 'CHECKED_IN', 'ONLINE', 'HSP-F1U3C9',
        0, CURRENT_TIMESTAMP - INTERVAL '4 days', 'Rita Receptionist', NOW(), NOW()),
       ('Hoang Minh Duc', 'duc.hoang@email.com', '+84 912 100 013', '1986-07-15', 'Vietnamese',
        CURRENT_DATE - 9, CURRENT_DATE - 5, 'CHECKED_IN', 'OFFLINE', NULL,
        0, CURRENT_TIMESTAMP - INTERVAL '9 days', 'Rita Receptionist', NOW(), NOW()),
       ('Kim Soo-Jin', 'soojin.kim@email.com', '+82 10 8765 0014', '1994-03-22', 'Korean',
        CURRENT_DATE - 5, CURRENT_DATE - 1, 'CHECKED_IN', 'ONLINE', 'HSP-G4H7W3',
        0, CURRENT_TIMESTAMP - INTERVAL '5 days', 'Rita Receptionist', NOW(), NOW()),
       ('Vu Thi Hong', 'hong.vu@email.com', '+84 912 100 015', '1983-11-08', 'Vietnamese',
        CURRENT_DATE - 2, CURRENT_DATE + 2, 'CHECKED_IN', 'OFFLINE', NULL,
        0, CURRENT_TIMESTAMP - INTERVAL '2 days', 'Rita Receptionist', NOW(), NOW());

-- CONFIRMED (15)
INSERT INTO reservations (guest_name, guest_email, guest_phone, guest_date_of_birth,
                          guest_nationality, check_in_at, check_out_at, status, source, confirmation_code,
                          total_price, created_at, updated_at)
VALUES ('David Brown', 'david.b@email.com', '+1 555 010 101', '1975-08-20', 'American',
        CURRENT_DATE + 1, CURRENT_DATE + 4, 'CONFIRMED', 'ONLINE', 'HSP-H8J1R5', 0, NOW(), NOW()),
       ('Nguyen Thi Mai', 'mai.nguyen@email.com', '+84 912 100 102', '1989-12-12', 'Vietnamese',
        CURRENT_DATE + 0, CURRENT_DATE + 3, 'CONFIRMED', 'OFFLINE', NULL, 0, NOW(), NOW()),
       ('Alexandre Dupont', 'alex.d@email.com', '+33 6 12 34 103', '1981-04-05', 'French',
        CURRENT_DATE + 2, CURRENT_DATE + 7, 'CONFIRMED', 'ONLINE', 'HSP-J9K3T7', 0, NOW(), NOW()),
       ('Ly Thi Ca', 'ca.ly@email.com', '+84 912 100 104', '1996-09-19', 'Vietnamese',
        CURRENT_DATE + 0, CURRENT_DATE + 2, 'CONFIRMED', 'OFFLINE', NULL, 0, NOW(), NOW()),
       ('James Wilson', 'james.w@email.com', '+61 400 123 105', '1970-02-28', 'Australian',
        CURRENT_DATE + 3, CURRENT_DATE + 6, 'CONFIRMED', 'ONLINE', 'HSP-K1L5M9', 0, NOW(), NOW()),
       ('Truong Van Hieu', 'hieu.truong@email.com', '+84 912 100 106', '1987-10-10', 'Vietnamese',
        CURRENT_DATE + 1, CURRENT_DATE + 5, 'CONFIRMED', 'OFFLINE', NULL, 0, NOW(), NOW()),
       ('Lisa Anderson', 'lisa.a@email.com', '+46 70 123 0107', '1992-06-15', 'Swedish',
        CURRENT_DATE + 4, CURRENT_DATE + 8, 'CONFIRMED', 'ONLINE', 'HSP-L2M3N8', 0, NOW(), NOW()),
       ('Dang Hoang Phuc', 'phuc.dang@email.com', '+84 912 100 108', '1984-01-30', 'Vietnamese',
        CURRENT_DATE + 0, CURRENT_DATE + 4, 'CONFIRMED', 'OFFLINE', NULL, 0, NOW(), NOW()),
       ('Rachel Green', 'rachel.g@email.com', '+1 555 010 109', '1993-05-22', 'American',
        CURRENT_DATE + 5, CURRENT_DATE + 7, 'CONFIRMED', 'ONLINE', 'HSP-M3N4P9', 0, NOW(), NOW()),
       ('Ngo Bao Khanh', 'khanh.ngo@email.com', '+84 912 100 110', '1990-08-14', 'Vietnamese',
        CURRENT_DATE + 2, CURRENT_DATE + 6, 'CONFIRMED', 'OFFLINE', NULL, 0, NOW(), NOW()),
       ('Peter Mueller', 'peter.m@email.com', '+49 170 123 111', '1978-11-03', 'German',
        CURRENT_DATE + 6, CURRENT_DATE + 10, 'CONFIRMED', 'ONLINE', 'HSP-N4O5Q1', 0, NOW(), NOW()),
       ('Phung Thi Hue', 'hue.phung@email.com', '+84 912 100 112', '1995-02-07', 'Vietnamese',
        CURRENT_DATE + 0, CURRENT_DATE + 3, 'CONFIRMED', 'OFFLINE', NULL, 0, NOW(), NOW()),
       ('Sofia Rossi', 'sofia.r@email.com', '+39 320 123 113', '1986-07-19', 'Italian',
        CURRENT_DATE + 3, CURRENT_DATE + 8, 'CONFIRMED', 'ONLINE', 'HSP-O5P6R2', 0, NOW(), NOW()),
       ('Ha Anh Tuan', 'tuan.ha@email.com', '+84 912 100 114', '1982-12-25', 'Vietnamese',
        CURRENT_DATE + 1, CURRENT_DATE + 2, 'CONFIRMED', 'OFFLINE', NULL, 0, NOW(), NOW()),
       ('Olivia Taylor', 'olivia.t@email.com', '+44 7700 900115', '1991-04-11', 'British',
        CURRENT_DATE + 7, CURRENT_DATE + 11, 'CONFIRMED', 'ONLINE', 'HSP-P6Q7S3', 0, NOW(), NOW());

-- PENDING (10)
INSERT INTO reservations (guest_name, guest_email, guest_phone, guest_date_of_birth,
                          guest_nationality, check_in_at, check_out_at, status, source, confirmation_code,
                          total_price, created_at, updated_at)
VALUES ('Mai Thanh Thuy', 'thuy.mai@email.com', '+84 912 100 201', '1994-03-17', 'Vietnamese',
        CURRENT_DATE + 5, CURRENT_DATE + 8, 'PENDING', 'OFFLINE', NULL, 0, NOW(), NOW()),
       ('Tom Harris', 'tom.h@email.com', '+1 555 010 202', '1983-09-02', 'American',
        CURRENT_DATE + 8, CURRENT_DATE + 10, 'PENDING', 'ONLINE', 'HSP-Q7R8T4', 0, NOW(), NOW()),
       ('Luong Van Minh', 'minh.luong@email.com', '+84 912 100 203', '1988-06-21', 'Vietnamese',
        CURRENT_DATE + 3, CURRENT_DATE + 7, 'PENDING', 'OFFLINE', NULL, 0, NOW(), NOW()),
       ('Natalie Kumar', 'natalie.k@email.com', '+91 98765 4204', '1990-10-30', 'Indian',
        CURRENT_DATE + 10, CURRENT_DATE + 14, 'PENDING', 'ONLINE', 'HSP-R8S9U5', 0, NOW(), NOW()),
       ('Trinh Quoc Bao', 'bao.trinh@email.com', '+84 912 100 205', '1985-05-09', 'Vietnamese',
        CURRENT_DATE + 2, CURRENT_DATE + 4, 'PENDING', 'OFFLINE', NULL, 0, NOW(), NOW()),
       ('Emma Wilson', 'emma.w@email.com', '+61 400 234 206', '1996-12-15', 'Australian',
        CURRENT_DATE + 6, CURRENT_DATE + 9, 'PENDING', 'ONLINE', 'HSP-S9T1V6', 0, NOW(), NOW()),
       ('Dao Thi Huong', 'huong.dao@email.com', '+84 912 100 207', '1992-08-25', 'Vietnamese',
        CURRENT_DATE + 4, CURRENT_DATE + 8, 'PENDING', 'OFFLINE', NULL, 0, NOW(), NOW()),
       ('Carlos Mendez', 'carlos.m@email.com', '+52 55 1234 208', '1980-01-12', 'Mexican',
        CURRENT_DATE + 9, CURRENT_DATE + 13, 'PENDING', 'ONLINE', 'HSP-T1U2W7', 0, NOW(), NOW()),
       ('La Thi Gam', 'gam.la@email.com', '+84 912 100 209', '1987-04-03', 'Vietnamese',
        CURRENT_DATE + 1, CURRENT_DATE + 4, 'PENDING', 'OFFLINE', NULL, 0, NOW(), NOW()),
       ('Daniel Lee', 'daniel.l@email.com', '+82 10 9876 210', '1993-11-28', 'Korean',
        CURRENT_DATE + 11, CURRENT_DATE + 15, 'PENDING', 'ONLINE', 'HSP-U2V3X8', 0, NOW(), NOW());

-- CHECKED_OUT (5)
INSERT INTO reservations (guest_name, guest_email, guest_phone, guest_date_of_birth,
                          guest_nationality, check_in_at, check_out_at, status, source, confirmation_code,
                          total_price, checked_in_at, checked_in_by, created_at, updated_at)
VALUES ('Robert Johnson', 'robert.j@email.com', '+1 555 010 301', '1976-06-14', 'American',
        CURRENT_DATE - 14, CURRENT_DATE - 10, 'CHECKED_OUT', 'ONLINE', 'HSP-V3W4Y9',
        0, CURRENT_TIMESTAMP - INTERVAL '14 days', 'Rita Receptionist', NOW(), NOW()),
       ('Tran Tien Dung', 'dung.tran@email.com', '+84 912 100 302', '1988-08-22', 'Vietnamese',
        CURRENT_DATE - 12, CURRENT_DATE - 8, 'CHECKED_OUT', 'OFFLINE', NULL,
        0, CURRENT_TIMESTAMP - INTERVAL '12 days', 'Rita Receptionist', NOW(), NOW()),
       ('Yoshida Haruki', 'haruki.y@email.com', '+81 80 1234 303', '1991-01-09', 'Japanese',
        CURRENT_DATE - 10, CURRENT_DATE - 6, 'CHECKED_OUT', 'ONLINE', 'HSP-W4X5Z1',
        0, CURRENT_TIMESTAMP - INTERVAL '10 days', 'Rita Receptionist', NOW(), NOW()),
       ('Pham Hoang Long', 'long.pham@email.com', '+84 912 100 304', '1983-05-30', 'Vietnamese',
        CURRENT_DATE - 16, CURRENT_DATE - 12, 'CHECKED_OUT', 'OFFLINE', NULL,
        0, CURRENT_TIMESTAMP - INTERVAL '16 days', 'Leo Leader', NOW(), NOW()),
       ('Kate Brown', 'kate.b@email.com', '+44 7700 900305', '1985-09-17', 'British',
        CURRENT_DATE - 8, CURRENT_DATE - 4, 'CHECKED_OUT', 'ONLINE', 'HSP-X5Y6A2',
        0, CURRENT_TIMESTAMP - INTERVAL '8 days', 'Rita Receptionist', NOW(), NOW());

-- CANCELLED (5)
INSERT INTO reservations (guest_name, guest_email, guest_phone, guest_date_of_birth,
                          guest_nationality, check_in_at, check_out_at, status, source, confirmation_code,
                          total_price, created_at, updated_at)
VALUES ('Ahmed Hassan', 'ahmed.h@email.com', '+20 100 123 401', '1978-12-20', 'Egyptian',
        CURRENT_DATE + 2, CURRENT_DATE + 5, 'CANCELLED', 'ONLINE', 'HSP-Y6Z7B3', 0, NOW(), NOW()),
       ('Vu Kim Ngan', 'ngan.vu@email.com', '+84 912 100 402', '1995-07-13', 'Vietnamese',
        CURRENT_DATE + 0, CURRENT_DATE + 3, 'CANCELLED', 'OFFLINE', NULL, 0, NOW(), NOW()),
       ('Michael Scott', 'michael.s@email.com', '+1 555 010 403', '1965-03-09', 'American',
        CURRENT_DATE + 4, CURRENT_DATE + 8, 'CANCELLED', 'ONLINE', 'HSP-Z7A8C4', 0, NOW(), NOW()),
       ('Ly Minh Tri', 'tri.ly@email.com', '+84 912 100 404', '1989-10-05', 'Vietnamese',
        CURRENT_DATE + 1, CURRENT_DATE + 6, 'CANCELLED', 'OFFLINE', NULL, 0, NOW(), NOW()),
       ('Priya Sharma', 'priya.s@email.com', '+91 98765 43405', '1992-04-28', 'Indian',
        CURRENT_DATE + 6, CURRENT_DATE + 10, 'CANCELLED', 'ONLINE', 'HSP-A8B9D5', 0, NOW(), NOW());

-- ────────────────────────────────────────────────────────────────────────────
-- 8. Reservation Details
-- ────────────────────────────────────────────────────────────────────────────
DO
$$
    DECLARE
        r      RECORD;
        idx    INT := 0;
        std_id bigint; del_id bigint; exc_id bigint; fam_id bigint; pre_id bigint;
    BEGIN
        SELECT id INTO std_id FROM room_types WHERE name = 'BASIC DOUBLE';
        SELECT id INTO del_id FROM room_types WHERE name = 'DELUXE DOUBLE';
        SELECT id INTO exc_id FROM room_types WHERE name = 'SUPERIOR DOUBLE';
        SELECT id INTO fam_id FROM room_types WHERE name = 'BASIC FAMILY';
        SELECT id INTO pre_id FROM room_types WHERE name = 'DELUXE SUITE';

        FOR r IN SELECT id, status FROM reservations ORDER BY id
            LOOP
                idx := idx + 1;

                IF r.status = 'CHECKED_IN' THEN
                    INSERT INTO reservation_details (reservation_id, room_type_id, room_count, base_price, total_price)
                    VALUES (r.id, std_id, 1, 85.00, 85.00);
                    IF idx % 3 = 0 THEN
                        INSERT INTO reservation_details (reservation_id, room_type_id, room_count, base_price, total_price)
                        VALUES (r.id, del_id, 1, 145.00, 145.00);
                    END IF;

                ELSIF r.status = 'CONFIRMED' THEN
                    INSERT INTO reservation_details (reservation_id, room_type_id, room_count, base_price, total_price)
                    VALUES (r.id, del_id, 1, 145.00, 145.00);
                    IF idx % 2 = 0 THEN
                        INSERT INTO reservation_details (reservation_id, room_type_id, room_count, base_price, total_price)
                        VALUES (r.id, exc_id, 1, 280.00, 280.00);
                    END IF;

                ELSIF r.status = 'PENDING' THEN
                    INSERT INTO reservation_details (reservation_id, room_type_id, room_count, base_price, total_price)
                    VALUES (r.id, std_id, 2, 85.00, 170.00);

                ELSIF r.status = 'CHECKED_OUT' THEN
                    INSERT INTO reservation_details (reservation_id, room_type_id, room_count, base_price, total_price)
                    VALUES (r.id, fam_id, 1, 220.00, 220.00);

                ELSE
                    INSERT INTO reservation_details (reservation_id, room_type_id, room_count, base_price, total_price)
                    VALUES (r.id, std_id, 1, 85.00, 85.00);
                END IF;
            END LOOP;
    END
$$;

-- Update reservation total_price
UPDATE reservations r
SET total_price = COALESCE((SELECT SUM(d.total_price) FROM reservation_details d WHERE d.reservation_id = r.id), 0);

-- ────────────────────────────────────────────────────────────────────────────
-- 9. Staying Guests
-- ────────────────────────────────────────────────────────────────────────────
INSERT INTO staying_guests (reservation_id, guest_name, date_of_birth, nationality, created_at)
SELECT r.id, r.guest_name, r.guest_date_of_birth, r.guest_nationality, NOW()
FROM reservations r
WHERE r.status IN ('CHECKED_IN', 'CHECKED_OUT');

INSERT INTO staying_guests (reservation_id, guest_name, date_of_birth, nationality, created_at)
SELECT r.id, 'Anna Smith', '2018-03-22', 'British', NOW()
FROM reservations r
WHERE r.guest_name = 'John Smith'
  AND r.status = 'CHECKED_IN';

INSERT INTO staying_guests (reservation_id, guest_name, date_of_birth, nationality, created_at)
SELECT r.id, 'Sakura Tanaka', '2020-01-05', 'Japanese', NOW()
FROM reservations r
WHERE r.guest_name = 'Tanaka Yuki'
  AND r.status = 'CHECKED_IN';

INSERT INTO staying_guests (reservation_id, guest_name, date_of_birth, nationality, created_at)
SELECT r.id, n, '2015-06-10', 'Vietnamese', NOW()
FROM reservations r
         CROSS JOIN (VALUES ('Minh Anh'), ('Bao Linh')) AS guests(n)
WHERE r.guest_name = 'Pham Minh Chau'
  AND r.status = 'CHECKED_IN';

INSERT INTO staying_guests (reservation_id, guest_name, date_of_birth, nationality, created_at)
SELECT r.id, 'Tommy Johnson', '2016-11-03', 'American', NOW()
FROM reservations r
WHERE r.guest_name = 'Emily Johnson'
  AND r.status = 'CHECKED_IN';

INSERT INTO staying_guests (reservation_id, guest_name, date_of_birth, nationality, created_at)
SELECT r.id, 'Jack Brown', '2010-07-14', 'American', NOW()
FROM reservations r
WHERE r.guest_name = 'Robert Johnson'
  AND r.status = 'CHECKED_OUT';

-- ────────────────────────────────────────────────────────────────────────────
-- 10. Room Assignments
-- ────────────────────────────────────────────────────────────────────────────
INSERT INTO room_assignments (reservation_id, room_id, assigned_at)
SELECT DISTINCT ON (r.id) r.id,
                          rm.id,
                          NOW()
FROM reservations r
         JOIN reservation_details rd ON rd.reservation_id = r.id
         JOIN room_types rt ON rt.id = rd.room_type_id
         JOIN LATERAL (
    SELECT r2.id
    FROM rooms r2
    WHERE r2.room_type_id = rt.id
    ORDER BY r2.id
    LIMIT 1 OFFSET (r.id % 5)
    ) rm ON true
WHERE r.status = 'CHECKED_IN';

-- ────────────────────────────────────────────────────────────────────────────
-- 11. Payments
-- ────────────────────────────────────────────────────────────────────────────
INSERT INTO payments (reservation_id, amount, payment_method, confirmed_at, confirmed_by)
SELECT r.id,
       CASE
           WHEN r.source = 'ONLINE' THEN ROUND(r.total_price * 0.3, 2)
           ELSE r.total_price
           END,
       CASE
           WHEN r.source = 'ONLINE' THEN 'PAYPAL'
           WHEN r.id % 3 = 0 THEN 'CASH'
           ELSE 'CARD'
           END,
       CURRENT_TIMESTAMP - INTERVAL '1 hour' * (r.id % 48),
       CASE
           WHEN r.status IN ('CHECKED_IN', 'CHECKED_OUT')
               THEN r.checked_in_by
           ELSE r.guest_email
           END
FROM reservations r
WHERE r.status IN ('CHECKED_IN', 'CONFIRMED', 'CHECKED_OUT');

-- ────────────────────────────────────────────────────────────────────────────
-- 12. Reviews
-- ────────────────────────────────────────────────────────────────────────────
INSERT INTO reviews (reservation_id, rating, created_at)
SELECT r.id, 4 + (r.id % 2), NOW()
FROM reservations r
WHERE r.status = 'CHECKED_OUT';

COMMIT;