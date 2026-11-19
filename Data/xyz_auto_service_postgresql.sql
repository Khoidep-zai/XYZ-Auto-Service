-- ============================================================================
-- ĐỒ ÁN MÔN HỌC: QUẢN LÝ DỰ ÁN PHẦN MỀM
-- HỆ THỐNG QUẢN LÝ ĐẶT LỊCH BẢO DƯỠNG & SỬA CHỮA Ô TÔ TRỰC TUYẾN (XYZ AUTO SERVICE)
-- CƠ SỞ DỮ LIỆU: POSTGRESQL (Chuẩn 3NF, UTF-8, Cloud & Docker Compatible)
-- ============================================================================

DROP TABLE IF EXISTS workshop_appointment_services CASCADE;
DROP TABLE IF EXISTS workshop_appointments CASCADE;
DROP TABLE IF EXISTS workshop_services CASCADE;
DROP TABLE IF EXISTS workshop_accounts CASCADE;
DROP TABLE IF EXISTS workshop_settings CASCADE;

-- 1. Bảng cấu hình hệ thống (Năng lực tiếp nhận tối đa)
CREATE TABLE IF NOT EXISTS workshop_settings (
    id BIGINT PRIMARY KEY DEFAULT 1,
    capacity INT NOT NULL DEFAULT 3
);

INSERT INTO workshop_settings (id, capacity) 
VALUES (1, 3)
ON CONFLICT (id) DO UPDATE SET capacity = EXCLUDED.capacity;

-- 2. Bảng tài khoản người dùng và phân quyền (F09: Khách hàng / Nhân viên / Quản lý)
CREATE TABLE IF NOT EXISTS workshop_accounts (
    id BIGSERIAL PRIMARY KEY,
    phone VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(16) NOT NULL CHECK (role IN ('CUSTOMER', 'STAFF', 'MANAGER')),
    meta VARCHAR(200) NOT NULL DEFAULT ''
);

-- Mật khẩu mặc định: 12345678 (BCrypt hash)
INSERT INTO workshop_accounts (id, phone, name, password_hash, role, meta) VALUES
(1, '0901234567', 'Nguyễn Quản Lý', '$2a$10$VbhdpL6NcK/4qDC5UBm3NOHtYwwW7AGKswRGQ28Bm3KDgc/dgK1Mi', 'MANAGER', 'Quản lý trung tâm XYZ'),
(2, '0912345678', 'Trần Tiếp Nhận', '$2a$10$VbhdpL6NcK/4qDC5UBm3NOHtYwwW7AGKswRGQ28Bm3KDgc/dgK1Mi', 'STAFF', 'Nhân viên tiếp nhận xưởng'),
(3, '0988888888', 'Lê Khách Hàng', '$2a$10$VbhdpL6NcK/4qDC5UBm3NOHtYwwW7AGKswRGQ28Bm3KDgc/dgK1Mi', 'CUSTOMER', 'Khách hàng VIP')
ON CONFLICT (id) DO UPDATE SET
    phone = EXCLUDED.phone,
    name = EXCLUDED.name,
    password_hash = EXCLUDED.password_hash,
    role = EXCLUDED.role,
    meta = EXCLUDED.meta;

-- 3. Bảng danh mục dịch vụ bảo dưỡng & sửa chữa (F01, F02)
CREATE TABLE IF NOT EXISTS workshop_services (
    id VARCHAR(32) PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    category VARCHAR(100) NOT NULL,
    duration INT NOT NULL, -- Thời lượng (phút)
    price BIGINT NOT NULL, -- Giá dịch vụ (VNĐ)
    warranty VARCHAR(100) NOT NULL DEFAULT '',
    description VARCHAR(1000) NOT NULL DEFAULT '',
    checklist VARCHAR(2000) NOT NULL DEFAULT '',
    active BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT INTO workshop_services (id, name, category, duration, price, warranty, description, checklist, active) VALUES
('SRV-01', 'Bảo dưỡng định kỳ cấp 10.000km', 'Bảo dưỡng định kỳ', 60, 1450000, '6 tháng / 10.000km', 'Gói bảo dưỡng tiêu chuẩn định kỳ xe ô tô', 'Thay dầu máy; Vệ sinh lọc gió động cơ; Vệ sinh lọc gió điều hòa; Kiểm tra hệ thống phanh 4 bánh; Đảo lốp; Quét chẩn đoán lỗi OBD-II', TRUE),
('SRV-02', 'Thay dầu động cơ Tổng hợp & Lọc dầu', 'Thay dầu & Chất lỏng', 45, 1150000, '10.000km', 'Dầu động cơ cao cấp Fully Synthetic + lọc nhớt chính hãng', 'Xả nhớt cũ đáy cacte; Thay lọc dầu nhớt mới; Châm 4L dầu tổng hợp Castrol Edge; Kiểm tra rò rỉ que thăm nhớt', TRUE),
('SRV-03', 'Bảo dưỡng Hệ thống Phanh ABS 4 bánh', 'Hệ thống phanh & Gầm', 60, 850000, '6 tháng', 'Vệ sinh và kiểm tra hệ thống an toàn phanh ABS', 'Tháo vệ sinh 4 cụm phanh đĩa; Đo độ dày má phanh; Tra mỡ ắc phanh chuyên dụng; Kiểm tra cảm biến ABS và dây dầu', TRUE),
('SRV-04', 'Chẩn đoán & Sửa chữa Động cơ', 'Sửa chữa động cơ', 120, 2400000, '12 tháng', 'Kiểm tra lỗi rung giật, hụt ga, tiêuaho nhiên liệu', 'Đọc mã lỗi chuyên sâu OBD; Đo áp suất buồng đốt; Kiểm tra cuộn đánh lửa bugi; Vệ sinh họng ga và kim phun điện tử', TRUE),
('SRV-05', 'Cân chỉnh Thước lái & Độ chụm 3D', 'Hệ thống phanh & Gầm', 60, 750000, '3 tháng', 'Cân chỉnh độ chụm bánh xe bằng máy laser 3D hiện đại', 'Kiểm tra khớp rô-tuyn lái; Đo góc đặt bánh xe camber/caster/toe; Cân chỉnh góc đặt bánh lái laser; Chạy thử cân bằng tay lái', TRUE),
('SRV-06', 'Vệ sinh Điều hòa Nội soi & Khử khuẩn Cabin', 'Điện & Điều hòa', 75, 950000, '6 tháng', 'Vệ sinh giàn lạnh bằng camera nội soi không cần tháo taplo', 'Đo nhiệt độ cửa gió; Luồn camera nội soi giàn lạnh; Phun dung dịch tẩy rửa sinh học; Xông khử khuẩn ozon khoang xe', TRUE),
('SRV-07', 'Kiểm tra & Thay Ắc quy Ô tô Chính hãng', 'Hệ thống Điện & Ắc quy', 30, 1850000, '12 tháng', 'Thay ắc quy khô miễn bảo dưỡng GS/Varta', 'Đo dòng nạp máy phát điện; Đo điện áp tĩnh ắc quy cũ; Lắp đặt ắc quy mới; Bôi mỡ bảo vệ cực ắc quy', TRUE),
('SRV-08', 'Kiểm tra Tổng quát An toàn Xe Trước Chuyến Đi', 'Kiểm định & An toàn', 45, 500000, '1 tháng', 'Kiểm tra 32 hạng mục an toàn vận hành xe', 'Kiểm tra lốp, áp suất lốp; Kiểm tra mức nước làm mát, nước rửa kính; Kiểm tra toàn bộ đèn chiếu sáng; Kiểm tra dây đai an toàn', TRUE)
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    category = EXCLUDED.category,
    duration = EXCLUDED.duration,
    price = EXCLUDED.price,
    warranty = EXCLUDED.warranty,
    description = EXCLUDED.description,
    checklist = EXCLUDED.checklist,
    active = EXCLUDED.active;

-- 4. Bảng lịch hẹn đặt bảo dưỡng & sửa chữa (F03, F04, F05, F06, F07)
CREATE TABLE IF NOT EXISTS workshop_appointments (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT REFERENCES workshop_accounts(id) ON DELETE SET NULL,
    customer_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    license_plate VARCHAR(30) NOT NULL,
    car_model VARCHAR(100) NOT NULL,
    booking_date DATE NOT NULL,
    slot VARCHAR(5) NOT NULL, -- '08:00', '09:00', '10:00', '13:00', '14:00', '15:00', '16:00'
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'CONFIRMED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'REJECTED', 'NO_SHOW')),
    total_duration INT NOT NULL,
    total_price BIGINT NOT NULL,
    bay VARCHAR(120) NOT NULL DEFAULT 'Chờ phân khoang',
    technician VARCHAR(120) NOT NULL DEFAULT 'Chờ phân công',
    note VARCHAR(1000) NOT NULL DEFAULT '',
    staff_note VARCHAR(1000) NOT NULL DEFAULT ''
);

CREATE INDEX IF NOT EXISTS idx_appointment_slot ON workshop_appointments(booking_date, slot, status);
CREATE INDEX IF NOT EXISTS idx_appointment_customer ON workshop_appointments(customer_id);
CREATE INDEX IF NOT EXISTS idx_appointment_phone ON workshop_appointments(phone);
CREATE INDEX IF NOT EXISTS idx_appointment_license_plate ON workshop_appointments(license_plate);
CREATE INDEX IF NOT EXISTS idx_appointment_status ON workshop_appointments(status);
CREATE INDEX IF NOT EXISTS idx_appointment_customer_date ON workshop_appointments(customer_id, booking_date DESC, id DESC);

-- Partial Unique Index (CDTN1): Chống race-condition đặt trùng biển số trong cùng một slot
CREATE UNIQUE INDEX IF NOT EXISTS uq_appointment_slot_plate ON workshop_appointments (booking_date, slot, license_plate) 
WHERE status NOT IN ('CANCELLED', 'REJECTED', 'NO_SHOW');

-- Dữ liệu lịch hẹn mẫu để trình diễn (Câu 8)
INSERT INTO workshop_appointments (id, customer_id, customer_name, phone, license_plate, car_model, booking_date, slot, status, total_duration, total_price, bay, technician, note, staff_note) VALUES
(1, 3, 'Lê Khách Hàng', '0988888888', '51K-888.88', 'Mazda CX-5 2022', CURRENT_DATE + INTERVAL '1 day', '08:00', 'CONFIRMED', 105, 2600000, 'Khoang 1 - Cầu nâng 2 trụ', 'KTV. Hoàng Tuấn', 'Xe đi đường trường về cần kiểm tra kỹ phanh', 'Đã liên hệ xác nhận, phụ tùng đã chuẩn bị sẵn'),
(2, 3, 'Lê Khách Hàng', '0988888888', '51K-888.88', 'Mazda CX-5 2022', CURRENT_DATE + INTERVAL '2 day', '09:00', 'PENDING', 60, 850000, 'Chờ phân khoang', 'Chờ phân công', 'Khách hẹn buổi sáng', ''),
(3, NULL, 'Vũ Văn B', '0977112233', '30F-999.99', 'Toyota Vios 2020', CURRENT_DATE, '10:00', 'COMPLETED', 45, 1150000, 'Khoang 2 - Nhanh', 'KTV. Trần Nam', 'Khách thay dầu định kỳ', 'Đã bàn giao xe và thanh toán')
ON CONFLICT (id) DO NOTHING;

-- 5. Bảng chi tiết dịch vụ trong từng lịch hẹn
CREATE TABLE IF NOT EXISTS workshop_appointment_services (
    appointment_id BIGINT NOT NULL REFERENCES workshop_appointments(id) ON DELETE CASCADE,
    service_code VARCHAR(32) NOT NULL,
    service_name VARCHAR(150) NOT NULL,
    service_price BIGINT NOT NULL,
    service_duration INT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_appointment_services_appt_id ON workshop_appointment_services(appointment_id);

INSERT INTO workshop_appointment_services (appointment_id, service_code, service_name, service_price, service_duration)
SELECT v.appointment_id, v.service_code, v.service_name, v.service_price, v.service_duration
FROM (VALUES
    (1::BIGINT, 'SRV-01'::VARCHAR(32), 'Bảo dưỡng định kỳ cấp 10.000km'::VARCHAR(150), 1450000::BIGINT, 60::INT),
    (1::BIGINT, 'SRV-02'::VARCHAR(32), 'Thay dầu động cơ Tổng hợp & Lọc dầu'::VARCHAR(150), 1150000::BIGINT, 45::INT),
    (2::BIGINT, 'SRV-03'::VARCHAR(32), 'Bảo dưỡng Hệ thống Phanh ABS 4 bánh'::VARCHAR(150), 850000::BIGINT, 60::INT),
    (3::BIGINT, 'SRV-02'::VARCHAR(32), 'Thay dầu động cơ Tổng hợp & Lọc dầu'::VARCHAR(150), 1150000::BIGINT, 45::INT)
) AS v(appointment_id, service_code, service_name, service_price, service_duration)
WHERE NOT EXISTS (
    SELECT 1 FROM workshop_appointment_services was 
    WHERE was.appointment_id = v.appointment_id AND was.service_code = v.service_code
);

-- Cập nhật sequence an toàn và chuẩn xác (idempotent, hỗ trợ Cloud PostgreSQL)
SELECT setval(
    COALESCE(pg_get_serial_sequence('workshop_accounts', 'id'), 'workshop_accounts_id_seq'),
    GREATEST((SELECT COALESCE(MAX(id), 0) FROM workshop_accounts), 1),
    (SELECT MAX(id) IS NOT NULL FROM workshop_accounts)
);

SELECT setval(
    COALESCE(pg_get_serial_sequence('workshop_appointments', 'id'), 'workshop_appointments_id_seq'),
    GREATEST((SELECT COALESCE(MAX(id), 0) FROM workshop_appointments), 1),
    (SELECT MAX(id) IS NOT NULL FROM workshop_appointments)
);
