-- =============================================================================
-- Seed Test Data for Hospi
-- =============================================================================

BEGIN;

-- ────────────────────────────────────────────────────────────────────────────
-- Cleanup
-- ────────────────────────────────────────────────────────────────────────────
DELETE
FROM invoice_items;
DELETE
FROM invoices;
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
INSERT INTO staffs (full_name, email, password_hash, phone, role, active, created_at)
VALUES ('Alice Admin', 'admin@gmail.com', '123', '+84 901 000 001', 'ADMIN', true, NOW()),
       ('Leo Leader', 'leader@gmail.com', '123', '+84 901 000 002', 'LEADER', true, NOW()),
       ('Rita Receptionist', 'receptionist@gmail.com', '123', '+84 901 000 003', 'RECEPTIONIST', true, NOW()),
       ('Mike Manager', 'manager@gmail.com', '123', '+84 901 000 004', 'MANAGER', true, NOW());

-- ────────────────────────────────────────────────────────────────────────────
-- 2. Hotel
-- ────────────────────────────────────────────────────────────────────────────
INSERT INTO hotels (id, name, description, address, phone, email,
                    average_rating, review_count, features, check_in_time, check_out_time,
                    floor_count, status, created_at, updated_at)
VALUES (1, 'Hospi Grand Hotel',
        'A modern boutique hotel in the heart of the city, offering luxurious rooms and exceptional service.',
        '123 Nguyen Hue Street, District 1, Ho Chi Minh City',
        '+84 28 3822 1234',
        'info@hospigrand.com',
        4.7, 128,
        'Free WiFi, Pool, Gym, Restaurant, Bar, 24/7 Room Service, Parking, Airport Shuttle',
        '14:00', '12:00',
        10, 'ACTIVE',
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

-- BASIC DOUBLE (24)
INSERT INTO rooms (room_number, floor_number, is_active, occupancy_status, condition_status, room_type_id)
SELECT n, floor_num, true, 'VACANT', 'CLEAN', rt.id
FROM (VALUES ('101', 1), ('102', 1), ('103', 1), ('104', 1),
             ('105', 1), ('106', 1), ('107', 1), ('108', 1),
             ('109', 1), ('110', 1), ('111', 1), ('112', 1),
             ('113', 1), ('114', 1), ('115', 1), ('116', 1),
             ('117', 1), ('118', 1), ('119', 1), ('120', 1),
             ('121', 1), ('122', 1), ('123', 1), ('124', 1)) AS nums(n, floor_num),
     room_types rt
WHERE rt.name = 'BASIC DOUBLE';

-- DELUXE DOUBLE (24)
INSERT INTO rooms (room_number, floor_number, is_active, occupancy_status, condition_status, room_type_id)
SELECT n, floor_num, true, 'VACANT', 'CLEAN', rt.id
FROM (VALUES ('201', 2), ('202', 2), ('203', 2), ('204', 2),
             ('205', 2), ('206', 2), ('207', 2), ('208', 2),
             ('209', 2), ('210', 2), ('211', 2), ('212', 2),
             ('213', 2), ('214', 2), ('215', 2), ('216', 2),
             ('217', 2), ('218', 2), ('219', 2), ('220', 2),
             ('221', 2), ('222', 2), ('223', 2), ('224', 2)) AS nums(n, floor_num),
     room_types rt
WHERE rt.name = 'DELUXE DOUBLE';

-- SUPERIOR DOUBLE (24)
INSERT INTO rooms (room_number, floor_number, is_active, occupancy_status, condition_status, room_type_id)
SELECT n, floor_num, true, 'VACANT', 'CLEAN', rt.id
FROM (VALUES ('301', 3), ('302', 3), ('303', 3), ('304', 3),
             ('305', 3), ('306', 3), ('307', 3), ('308', 3),
             ('309', 3), ('310', 3), ('311', 3), ('312', 3),
             ('313', 3), ('314', 3), ('315', 3), ('316', 3),
             ('317', 3), ('318', 3), ('319', 3), ('320', 3),
             ('321', 3), ('322', 3), ('323', 3), ('324', 3)) AS nums(n, floor_num),
     room_types rt
WHERE rt.name = 'SUPERIOR DOUBLE';

-- BASIC FAMILY (24)
INSERT INTO rooms (room_number, floor_number, is_active, occupancy_status, condition_status, room_type_id)
SELECT n, floor_num, true, 'VACANT', 'CLEAN', rt.id
FROM (VALUES ('401', 4), ('402', 4), ('403', 4), ('404', 4),
             ('405', 4), ('406', 4), ('407', 4), ('408', 4),
             ('409', 4), ('410', 4), ('411', 4), ('412', 4),
             ('413', 4), ('414', 4), ('415', 4), ('416', 4),
             ('417', 4), ('418', 4), ('419', 4), ('420', 4),
             ('421', 4), ('422', 4), ('423', 4), ('424', 4)) AS nums(n, floor_num),
     room_types rt
WHERE rt.name = 'BASIC FAMILY';

-- DELUXE SUITE (24)
INSERT INTO rooms (room_number, floor_number, is_active, occupancy_status, condition_status, room_type_id)
SELECT n, floor_num, true, 'VACANT', 'CLEAN', rt.id
FROM (VALUES ('501', 5), ('502', 5), ('503', 5), ('504', 5),
             ('505', 5), ('506', 5), ('507', 5), ('508', 5),
             ('509', 5), ('510', 5), ('511', 5), ('512', 5),
             ('513', 5), ('514', 5), ('515', 5), ('516', 5),
             ('517', 5), ('518', 5), ('519', 5), ('520', 5),
             ('521', 5), ('522', 5), ('523', 5), ('524', 5)) AS nums(n, floor_num),
     room_types rt
WHERE rt.name = 'DELUXE SUITE';

-- ────────────────────────────────────────────────────────────────────────────
-- 6. Update room statuses for realistic testing
-- ────────────────────────────────────────────────────────────────────────────
UPDATE rooms
SET occupancy_status = 'OCCUPIED'
WHERE room_number IN ('108', '112', '209', '213', '308', '312', '409', '413', '502', '506');

UPDATE rooms
SET condition_status = 'DIRTY'
WHERE room_number IN ('111', '116', '211', '216', '311', '316', '411', '416', '505', '510');

UPDATE rooms
SET condition_status = 'MAINTENANCE',
    is_active        = false
WHERE room_number IN ('114', '120', '214', '220', '314', '320', '414', '420', '509', '515');

UPDATE rooms
SET occupancy_status = 'VACANT'
WHERE room_number IN ('107', '115', '207', '215', '307', '315', '407', '415', '501', '512');

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
INSERT INTO payments (reservation_id, amount, payment_method, confirmed_at, confirmed_by, order_id)
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
           END,
       CASE WHEN r.source = 'ONLINE' THEN COALESCE(r.confirmation_code, 'HSP-' || UPPER(SUBSTR(MD5(r.id::TEXT || 'pay'), 1, 8))) ELSE NULL END
FROM reservations r
WHERE r.status IN ('CHECKED_IN', 'CONFIRMED', 'CHECKED_OUT');

-- ────────────────────────────────────────────────────────────────────────────
-- 12. Reviews
-- ────────────────────────────────────────────────────────────────────────────
INSERT INTO reviews (reservation_id, rating, created_at)
SELECT r.id, 4 + (r.id % 2), NOW()
FROM reservations r
WHERE r.status = 'CHECKED_OUT';

-- ────────────────────────────────────────────────────────────────────────────
-- 13. Invoices (one per CHECKED_OUT reservation for revenue demo)
-- ────────────────────────────────────────────────────────────────────────────
DO
$$
    DECLARE
        inv_id      bigint;
        r           RECORD;
        month_shift INT := 0;
    BEGIN
        FOR r IN SELECT id, total_price, source
                 FROM reservations
                 WHERE status = 'CHECKED_OUT'
                 ORDER BY id
            LOOP
                INSERT INTO invoices (booking_id, subtotal, tax_amount, deposit_used,
                                      total_amount, balance_due, status, created_at)
                VALUES (r.id,
                        r.total_price,
                        ROUND(r.total_price * 0.10, 2),
                        CASE WHEN r.source = 'ONLINE' THEN ROUND(r.total_price * 0.30, 2) ELSE 0 END,
                        r.total_price,
                        0,
                        'PAID',
                        NOW() - make_interval(months => month_shift))
                RETURNING id INTO inv_id;

                INSERT INTO invoice_items (invoice_id, item_type, description, quantity, unit_price, amount)
                VALUES (inv_id, 'ROOM', 'BASIC FAMILY', 1, r.total_price, r.total_price);

                INSERT INTO invoice_items (invoice_id, item_type, description, quantity, unit_price, amount)
                VALUES (inv_id, 'TAX', 'Tax (10%)', 1, ROUND(r.total_price * 0.10, 2),
                        ROUND(r.total_price * 0.10, 2));

                IF r.source = 'ONLINE' THEN
                    INSERT INTO invoice_items (invoice_id, item_type, description, quantity, unit_price, amount)
                    VALUES (inv_id, 'DISCOUNT', 'Deposit applied', 1, ROUND(r.total_price * 0.30, 2),
                            ROUND(r.total_price * 0.30, 2));
                END IF;

                month_shift := month_shift + 1;
            END LOOP;
    END
$$;

-- ────────────────────────────────────────────────────────────────────────────
-- 14. Additional seed data: 125 reservations spanning 36 months
-- ────────────────────────────────────────────────────────────────────────────
DO
$$
    DECLARE
        names       TEXT[] := ARRAY['Liam Garcia', 'Emma Chen', 'Noah Kim', 'Olivia Patel',
            'James Wilson', 'Sophia Lee', 'Benjamin Park', 'Isabella Wang', 'Lucas Nguyen',
            'Mia Tanaka', 'Ethan Brown', 'Charlotte Davis', 'Mason Miller', 'Amelia Wilson',
            'Logan Moore', 'Harper Taylor', 'Oliver Anderson', 'Evelyn Thomas', 'Elijah Jackson',
            'Abigail White', 'Aiden Martin', 'Ella Johnson', 'Caden Lewis', 'Avery Walker',
            'Jackson Hall'];
        emails      TEXT[] := ARRAY['liam.g@email.com', 'emma.c@email.com', 'noah.k@email.com',
            'olivia.p@email.com', 'james.w@email.com', 'sophia.l@email.com', 'ben.p@email.com',
            'isabella.w@email.com', 'lucas.n@email.com', 'mia.t@email.com', 'ethan.b@email.com',
            'charlotte.d@email.com', 'mason.m@email.com', 'amelia.w@email.com', 'logan.m@email.com',
            'harper.t@email.com', 'oliver.a@email.com', 'evelyn.t@email.com', 'elijah.j@email.com',
            'abigail.w@email.com', 'aiden.m@email.com', 'ella.j@email.com', 'caden.l@email.com',
            'avery.w@email.com', 'jackson.h@email.com'];
        phones      TEXT[] := ARRAY['+1 555 100 001', '+1 555 100 002', '+1 555 100 003',
            '+1 555 100 004', '+1 555 100 005', '+1 555 100 006', '+1 555 100 007',
            '+1 555 100 008', '+1 555 100 009', '+1 555 100 010', '+1 555 100 011',
            '+1 555 100 012', '+1 555 100 013', '+1 555 100 014', '+1 555 100 015',
            '+1 555 100 016', '+1 555 100 017', '+1 555 100 018', '+1 555 100 019',
            '+1 555 100 020', '+1 555 100 021', '+1 555 100 022', '+1 555 100 023',
            '+1 555 100 024', '+1 555 100 025'];
        dobs        DATE[] := ARRAY['1985-06-15', '1990-03-22', '1982-11-08', '1993-09-01',
            '1978-12-25', '1995-04-18', '1987-07-30', '1991-01-14', '1984-08-05', '1996-10-28',
            '1981-05-12', '1989-02-19', '1976-11-03', '1994-07-21', '1983-09-09', '1992-04-07',
            '1986-12-15', '1997-01-28', '1980-06-22', '1991-08-11', '1988-03-05', '1995-10-30',
            '1982-07-17', '1993-12-03', '1987-05-20'];
        nationalities TEXT[] := ARRAY['Vietnamese', 'American', 'British', 'Japanese', 'Korean',
            'French', 'German', 'Spanish', 'Italian', 'Indian', 'Australian', 'Chinese', 'Filipino',
            'Brazilian', 'Canadian'];

        rt_ids      BIGINT[];
        rt_names    TEXT[] := ARRAY['BASIC DOUBLE', 'DELUXE DOUBLE', 'SUPERIOR DOUBLE',
            'BASIC FAMILY', 'DELUXE SUITE'];
        rt_prices   NUMERIC[] := ARRAY[85.00, 145.00, 280.00, 220.00, 450.00];

        inv_id      BIGINT;
        r_id        BIGINT;
        rt_idx      INT;
        nights      INT;
        source      TEXT;
        pay_method  TEXT;
        total_price NUMERIC;
        tax_amt     NUMERIC;
        deposit     NUMERIC;
        month_off   INT;
        day_off     INT;
        check_in    DATE;
        check_out   DATE;
        guest_idx   INT;
        nat_idx     INT;
    BEGIN
        FOR j IN 1..5 LOOP
            rt_ids := rt_ids || (SELECT id FROM room_types WHERE name = rt_names[j]);
        END LOOP;

        -- ====================================================================
        -- 100 CHECKED_OUT reservations spanning 36 months
        -- ====================================================================
        FOR i IN 1..100 LOOP
            rt_idx := ((i - 1) % 5) + 1;
            nights := 2 + ((i * 3) % 4);
            source := CASE WHEN i % 2 = 0 THEN 'ONLINE' ELSE 'OFFLINE' END;
            pay_method := CASE
                              WHEN source = 'ONLINE' THEN 'PAYPAL'
                              WHEN i % 3 = 0 THEN 'CASH'
                              ELSE 'CARD' END;

            month_off := FLOOR((i - 1) * 36.0 / 100.0);
            day_off := ((i * 7) % 25);
            check_in := (DATE_TRUNC('month', CURRENT_DATE)::DATE
                             - make_interval(months => month_off))::DATE + day_off;
            check_out := check_in + nights;

            total_price := rt_prices[rt_idx] * nights;
            tax_amt := ROUND(total_price * 0.10, 2);
            deposit := CASE WHEN source = 'ONLINE' THEN ROUND(total_price * 0.30, 2) ELSE 0 END;
            guest_idx := ((i - 1) % 25) + 1;
            nat_idx := ((i - 1) % 15) + 1;

            INSERT INTO reservations (guest_name, guest_email, guest_phone, guest_date_of_birth,
                                      guest_nationality, check_in_at, check_out_at, status, source,
                                      confirmation_code, total_price, checked_in_at, checked_in_by,
                                      created_at, updated_at)
            VALUES (names[guest_idx], emails[guest_idx], phones[guest_idx], dobs[guest_idx],
                    nationalities[nat_idx], check_in, check_out, 'CHECKED_OUT', source,
                    CASE WHEN source = 'ONLINE'
                             THEN 'HSP-' || UPPER(SUBSTR(MD5(i::TEXT || 'S'), 1, 6))
                         ELSE NULL END,
                    total_price,
                    check_in::TIMESTAMP, 'Rita Receptionist',
                    check_in::TIMESTAMP - INTERVAL '30 days',
                    check_in::TIMESTAMP - INTERVAL '30 days')
            RETURNING id INTO r_id;

            INSERT INTO reservation_details (reservation_id, room_type_id, room_count, base_price, total_price)
            VALUES (r_id, rt_ids[rt_idx], 1, rt_prices[rt_idx], total_price);

            INSERT INTO payments (reservation_id, amount, payment_method, confirmed_at, confirmed_by, order_id)
            VALUES (r_id, CASE WHEN source = 'ONLINE' THEN deposit ELSE total_price END,
                    pay_method, check_in::TIMESTAMP, 'Rita Receptionist',
                    CASE WHEN source = 'ONLINE' THEN 'HSP-' || UPPER(SUBSTR(MD5(i::TEXT || 'S'), 1, 8)) ELSE NULL END);

            INSERT INTO staying_guests (reservation_id, guest_name, date_of_birth, nationality, created_at)
            VALUES (r_id, names[guest_idx], dobs[guest_idx], nationalities[nat_idx],
                    check_in::TIMESTAMP - INTERVAL '30 days');

            INSERT INTO invoices (booking_id, subtotal, tax_amount, deposit_used,
                                  total_amount, balance_due, status, created_at)
            VALUES (r_id, total_price, tax_amt, deposit, total_price, 0, 'PAID',
                    check_out::TIMESTAMP)
            RETURNING id INTO inv_id;

            INSERT INTO invoice_items (invoice_id, item_type, description, quantity, unit_price, amount)
            VALUES (inv_id, 'ROOM', rt_names[rt_idx] || ' x1', nights, rt_prices[rt_idx], total_price);

            INSERT INTO invoice_items (invoice_id, item_type, description, quantity, unit_price, amount)
            VALUES (inv_id, 'TAX', 'Tax (10%)', 1, tax_amt, tax_amt);

            IF source = 'ONLINE' THEN
                INSERT INTO invoice_items (invoice_id, item_type, description, quantity, unit_price, amount)
                VALUES (inv_id, 'DISCOUNT', 'Deposit applied', 1, deposit, deposit);
            END IF;

            INSERT INTO reviews (reservation_id, rating, created_at)
            VALUES (r_id, 3 + (i % 3), check_out::TIMESTAMP);
        END LOOP;

        -- ====================================================================
        -- 10 CHECKED_IN (current stays)
        -- ====================================================================
        FOR i IN 101..110 LOOP
            rt_idx := ((i - 1) % 5) + 1;
            nights := 3 + ((i * 2) % 3);
            source := CASE WHEN i % 3 = 0 THEN 'ONLINE' ELSE 'OFFLINE' END;
            pay_method := CASE
                              WHEN source = 'ONLINE' THEN 'PAYPAL'
                              WHEN i % 2 = 0 THEN 'CASH'
                              ELSE 'CARD' END;
            check_in := CURRENT_DATE - (5 + (i % 10));
            check_out := check_in + nights;

            total_price := rt_prices[rt_idx] * nights;
            deposit := CASE WHEN source = 'ONLINE' THEN ROUND(total_price * 0.30, 2) ELSE total_price END;
            guest_idx := ((i - 1) % 25) + 1;
            nat_idx := ((i - 1) % 15) + 1;

            INSERT INTO reservations (guest_name, guest_email, guest_phone, guest_date_of_birth,
                                      guest_nationality, check_in_at, check_out_at, status, source,
                                      confirmation_code, total_price, checked_in_at, checked_in_by,
                                      created_at, updated_at)
            VALUES (names[guest_idx], emails[guest_idx], phones[guest_idx], dobs[guest_idx],
                    nationalities[nat_idx], check_in, check_out, 'CHECKED_IN', source,
                    CASE WHEN source = 'ONLINE'
                             THEN 'HSP-' || UPPER(SUBSTR(MD5(i::TEXT || 'C'), 1, 6))
                         ELSE NULL END,
                    total_price,
                    check_in::TIMESTAMP, 'Rita Receptionist', NOW() - INTERVAL '1 day',
                    NOW() - INTERVAL '1 day')
            RETURNING id INTO r_id;

            INSERT INTO reservation_details (reservation_id, room_type_id, room_count, base_price, total_price)
            VALUES (r_id, rt_ids[rt_idx], 1, rt_prices[rt_idx], total_price);

            INSERT INTO payments (reservation_id, amount, payment_method, confirmed_at, confirmed_by, order_id)
            VALUES (r_id, deposit, pay_method, check_in::TIMESTAMP, 'Rita Receptionist',
                    CASE WHEN source = 'ONLINE' THEN 'HSP-' || UPPER(SUBSTR(MD5(i::TEXT || 'C'), 1, 8)) ELSE NULL END);

            INSERT INTO staying_guests (reservation_id, guest_name, date_of_birth, nationality, created_at)
            VALUES (r_id, names[guest_idx], dobs[guest_idx], nationalities[nat_idx], NOW());

            INSERT INTO room_assignments (reservation_id, room_id, assigned_at)
            SELECT r_id, rm.id, check_in::TIMESTAMP
            FROM rooms rm
            WHERE rm.room_type_id = rt_ids[rt_idx]
              AND rm.is_active = true
            ORDER BY rm.id
            LIMIT 1 OFFSET (r_id % 5);
        END LOOP;

        -- ====================================================================
        -- 10 CONFIRMED (upcoming bookings)
        -- ====================================================================
        FOR i IN 111..120 LOOP
            rt_idx := ((i - 1) % 5) + 1;
            nights := 2 + ((i * 3) % 3);
            source := CASE WHEN i % 2 = 0 THEN 'ONLINE' ELSE 'OFFLINE' END;
            pay_method := CASE
                              WHEN source = 'ONLINE' THEN 'PAYPAL'
                              WHEN i % 3 = 0 THEN 'CASH'
                              ELSE 'CARD' END;
            check_in := CURRENT_DATE + (1 + (i % 14));
            check_out := check_in + nights;

            total_price := rt_prices[rt_idx] * nights;
            deposit := CASE WHEN source = 'ONLINE' THEN ROUND(total_price * 0.30, 2) ELSE total_price END;
            guest_idx := ((i - 1) % 25) + 1;
            nat_idx := ((i - 1) % 15) + 1;

            INSERT INTO reservations (guest_name, guest_email, guest_phone, guest_date_of_birth,
                                      guest_nationality, check_in_at, check_out_at, status, source,
                                      confirmation_code, total_price, created_at, updated_at)
            VALUES (names[guest_idx], emails[guest_idx], phones[guest_idx], dobs[guest_idx],
                    nationalities[nat_idx], check_in, check_out, 'CONFIRMED', source,
                    CASE WHEN source = 'ONLINE'
                             THEN 'HSP-' || UPPER(SUBSTR(MD5(i::TEXT || 'F'), 1, 6))
                         ELSE NULL END,
                    total_price, NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days')
            RETURNING id INTO r_id;

            INSERT INTO reservation_details (reservation_id, room_type_id, room_count, base_price, total_price)
            VALUES (r_id, rt_ids[rt_idx], 1, rt_prices[rt_idx], total_price);

            INSERT INTO payments (reservation_id, amount, payment_method, confirmed_at, confirmed_by, order_id)
            VALUES (r_id, deposit, pay_method, NOW() - INTERVAL '2 days', 'Rita Receptionist',
                    CASE WHEN source = 'ONLINE' THEN 'HSP-' || UPPER(SUBSTR(MD5(i::TEXT || 'F'), 1, 8)) ELSE NULL END);
        END LOOP;

        -- ====================================================================
        -- 5 PENDING
        -- ====================================================================
        FOR i IN 121..125 LOOP
            nights := 2 + ((i * 2) % 3);
            check_in := CURRENT_DATE + (5 + ((i - 121) * 5));
            check_out := check_in + nights;

            total_price := 85.00 * nights;
            guest_idx := ((i - 1) % 25) + 1;
            nat_idx := ((i - 1) % 15) + 1;

            INSERT INTO reservations (guest_name, guest_email, guest_phone, guest_date_of_birth,
                                      guest_nationality, check_in_at, check_out_at, status, source,
                                      confirmation_code, total_price, created_at, updated_at)
            VALUES (names[guest_idx], emails[guest_idx], phones[guest_idx], dobs[guest_idx],
                    nationalities[nat_idx], check_in, check_out, 'PENDING', 'OFFLINE', NULL,
                    total_price, NOW(), NOW())
            RETURNING id INTO r_id;

            INSERT INTO reservation_details (reservation_id, room_type_id, room_count, base_price, total_price)
            VALUES (r_id, rt_ids[1], 1, 85.00, total_price);
        END LOOP;
    END
$$;

-- ────────────────────────────────────────────────────────────────────────────
-- 15. Cancelled reservation with payment (for refund test scenario)
-- ────────────────────────────────────────────────────────────────────────────
DO
$$
    DECLARE
        cid  BIGINT;
        rt   BIGINT;
    BEGIN
        SELECT id INTO rt FROM room_types WHERE name = 'BASIC DOUBLE' LIMIT 1;

        INSERT INTO reservations (guest_name, guest_email, guest_phone, guest_date_of_birth,
                                  guest_nationality, check_in_at, check_out_at, status, source,
                                  confirmation_code, total_price, created_at, updated_at)
        VALUES ('Sarah Cancelled', 'sarah.c@email.com', '+1 555 999 001', '1990-03-15', 'American',
                CURRENT_DATE + 5, CURRENT_DATE + 8, 'CONFIRMED', 'ONLINE', 'HSP-CXL-TST1',
                255.00, NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days')
        RETURNING id INTO cid;

        INSERT INTO reservation_details (reservation_id, room_type_id, room_count, base_price, total_price)
        VALUES (cid, rt, 1, 85.00, 255.00);

        INSERT INTO payments (reservation_id, amount, payment_method, confirmed_at, confirmed_by, order_id)
        VALUES (cid, 76.50, 'PAYPAL', NOW() - INTERVAL '2 days', 'Rita Receptionist', 'HSP-CXL-TST1');

        UPDATE reservations SET status = 'CANCELLED' WHERE id = cid;
    END
$$;

COMMIT;