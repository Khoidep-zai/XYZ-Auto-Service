# HƯỚNG DẪN THIẾT LẬP CƠ SỞ DỮ LIỆU CLOUD POSTGRESQL (DATABASE CLOUD GUIDE)
## Hệ Thống Quản Lý Đặt Lịch Bảo Dưỡng & Sửa Chữa Ô Tô (XYZ Auto Service — WorkshopOS)

Tài liệu này cung cấp hướng dẫn toàn diện về kiến trúc cơ sở dữ liệu, kịch bản di chuyển (migration), và quy trình kết nối hệ thống XYZ Auto Service với các nền tảng **Cloud PostgreSQL hiện đại** (Neon, Supabase, Render, Railway, Aiven, AWS RDS).

---

## 1. TỔNG QUAN VÀ KHẢ NĂNG TƯƠNG THÍCH CLOUD POSTGRESQL

Hệ thống XYZ Auto Service sử dụng cơ sở dữ liệu PostgreSQL chuẩn 3NF, hỗ trợ UTF-8 toàn diện. Khi triển khai lên môi trường Cloud, các nhà cung cấp áp dụng các cơ chế bảo mật và quản lý tài nguyên nghiêm ngặt:

1. **Quyền hạn không đặc quyền (Unprivileged Role)**:
   * Trên Neon, Supabase, Render hay Railway, người dùng được cấp tài khoản quản trị ứng dụng (ví dụ: `neondb_owner`, `postgres`, `app_user`), **không có quyền `SUPERUSER`**.
   * Các câu lệnh hạ tầng như `CREATE DATABASE`, `ALTER SYSTEM`, `CREATE TABLESPACE`, `ALTER ROLE ... SUPERUSER` sẽ bị từ chối (`ERROR: permission denied`).
   * Script khởi tạo [`Data/cloud_postgres_init.sql`](file:///c:/O%20E/Qu%E1%BA%A3n%20l%C3%BD%20d%E1%BB%B1%20%C3%A1n%20ph%E1%BA%A7n%20m%E1%BB%81m/%C4%90%E1%BB%93%20%C3%A1n%20m%C3%B4n%20h%E1%BB%8Dc/demo/Data/cloud_postgres_init.sql) được thiết kế **100% unprivileged-safe**, loại bỏ hoàn toàn các câu lệnh quản trị hệ thống.

2. **Tính lũy thừa (Idempotency)**:
   * Kịch bản sử dụng `CREATE TABLE IF NOT EXISTS`, `CREATE INDEX IF NOT EXISTS`, và `ON CONFLICT (...) DO UPDATE / DO NOTHING`.
   * Người quản trị có thể thực thi lại script nhiều lần (ví dụ khi cập nhật giá dịch vụ, kiểm thử lại hoặc chạy trong pipeline CI/CD) mà không làm gián đoạn hay trùng lặp dữ liệu.

3. **Bắt buộc mã hóa đường truyền (SSL/TLS Mandatory)**:
   * Tất cả các dịch vụ Cloud PostgreSQL đều yêu cầu kết nối an toàn qua SSL.
   * Tham số JDBC bắt buộc: `?sslmode=require`.

4. **Đồng bộ Sequence an toàn (Sequence Reset Safety)**:
   * Khi chèn dữ liệu mẫu có sẵn khóa chính (explicit IDs: 1, 2, 3), PostgreSQL không tự động nâng giá trị của `sequence`. Nếu không đồng bộ, khi người dùng mới đăng ký hoặc đặt lịch hẹn mới qua JPA (`GenerationType.IDENTITY`), PostgreSQL sẽ báo lỗi:
     ```text
     ERROR: duplicate key value violates unique constraint "workshop_accounts_pkey"
     ```
   * Kịch bản sử dụng hàm chuẩn `pg_get_serial_sequence()` để tự động phát hiện và đồng bộ chính xác con trỏ sequence lên giá trị lớn nhất hiện có.

---

## 2. HƯỚNG DẪN CHUYỂN ĐỔI CHUỖI KẾT NỐI (CONNECTION STRING CONVERSION)

### 2.1. Cấu Trúc URI Cloud vs. Định Dạng Spring Boot JDBC

Hầu hết các nền tảng đám mây cung cấp URL kết nối theo chuẩn **RFC 3986** (dùng cho Node.js, Python, psql, Go):

```text
postgres://[USER]:[PASSWORD]@[HOST]:[PORT]/[DATABASE]?[PARAMETERS]
# hoặc:
postgresql://[USER]:[PASSWORD]@[HOST]:[PORT]/[DATABASE]?[PARAMETERS]
```

Tuy nhiên, **Spring Boot** sử dụng Java Database Connectivity (JDBC) kết hợp HikariCP connection pool, yêu cầu tiền tố giao thức bắt buộc là **`jdbc:postgresql://`**.

### 2.2. Bảng Đối Chiếu Thành Phần (Mapping Matrix)

| Thành phần URI Cloud | Ví dụ Cloud URI | Biến môi trường Spring Boot tương ứng | Giá trị nạp vào Spring Boot |
| :--- | :--- | :--- | :--- |
| **Giao thức (Protocol)** | `postgres://` hoặc `postgresql://` | Tiền tố của `DB_URL` | Đổi thành `jdbc:postgresql://` |
| **Máy chủ (Host)** | `ep-cool-fog.neon.tech` | Nằm trong `DB_URL` | Giữ nguyên Host |
| **Cổng (Port)** | `5432` hoặc `6543` | Nằm trong `DB_URL` | `:5432` (hoặc cổng pooler) |
| **Tên CSDL (Database)** | `neondb` hoặc `postgres` | Nằm trong `DB_URL` | `/<DATABASE>` |
| **Tham số SSL** | `sslmode=require` | Cuối chuỗi `DB_URL` | `?sslmode=require` |
| **Tài khoản (Username)** | `alex` hoặc `postgres` | **`DB_USER`** | Đặt riêng biến môi trường |
| **Mật khẩu (Password)** | `MySecretPassword123` | **`DB_PASSWORD`** | Đặt riêng biến môi trường |

---

### 2.3. Ví Dụ Chuyển Đổi Thực Tế Cho Từng Nhà Cung Cấp

#### [A] NEON POSTGRESQL (Khuyên Dùng — Serverless & Miễn Phí)
* **URL từ Neon Console**:
  ```text
  postgresql://alex_owner:npg_x7ABc123@ep-proud-water-a1b2c3d4.ap-southeast-1.aws.neon.tech/neondb?sslmode=require
  ```
* **Cấu hình biến môi trường Spring Boot**:
  ```bash
  SPRING_PROFILES_ACTIVE=postgres
  DB_URL=jdbc:postgresql://ep-proud-water-a1b2c3d4.ap-southeast-1.aws.neon.tech:5432/neondb?sslmode=require
  DB_USER=alex_owner
  DB_PASSWORD=npg_x7ABc123
  ```

#### [B] SUPABASE
Supabase cung cấp 2 chế độ kết nối trong mục **Project Settings → Database**:
1. **Direct Connection (Cổng 5432 - Khuyên dùng cho Spring Boot HikariCP)**:
   * URL Supabase:
     ```text
     postgresql://postgres:MySecurePass456@db.vxyzwkqrsmtnov.supabase.co:5432/postgres
     ```
   * Cấu hình Spring Boot:
     ```bash
     SPRING_PROFILES_ACTIVE=postgres
     DB_URL=jdbc:postgresql://db.vxyzwkqrsmtnov.supabase.co:5432/postgres?sslmode=require
     DB_USER=postgres
     DB_PASSWORD=MySecurePass456
     ```
2. **Transaction Pooler Connection (Cổng 6543 - PgBouncer)**:
   * URL Supabase:
     ```text
     postgresql://postgres.vxyzwkqrsmtnov:MySecurePass456@aws-0-ap-southeast-1.pooler.supabase.com:6543/postgres
     ```
   * Cấu hình Spring Boot:
     ```bash
     SPRING_PROFILES_ACTIVE=postgres
     DB_URL=jdbc:postgresql://aws-0-ap-southeast-1.pooler.supabase.com:6543/postgres?sslmode=require&prepareThreshold=0
     DB_USER=postgres.vxyzwkqrsmtnov
     DB_PASSWORD=MySecurePass456
     ```
   *(Lưu ý: Khi dùng qua Transaction Pooler PgBouncer, thêm `&prepareThreshold=0` để tắt cache server-prepared statements).*

#### [C] RENDER POSTGRESQL
* **URL từ Render Dashboard (External Connection String)**:
  ```text
  postgres://xyz_db_user:sUpErP@ss789@dpg-c1234567890abcdef-a.singapore-postgres.render.com/xyz_auto_service
  ```
* **Cấu hình Spring Boot**:
  ```bash
  SPRING_PROFILES_ACTIVE=postgres
  DB_URL=jdbc:postgresql://dpg-c1234567890abcdef-a.singapore-postgres.render.com:5432/xyz_auto_service?sslmode=require
  DB_USER=xyz_db_user
  DB_PASSWORD=sUpErP@ss789
  ```

#### [D] RAILWAY POSTGRESQL
* **URL từ Railway Variables (`DATABASE_URL`)**:
  ```text
  postgresql://postgres:randomrailwaypass@roundhouse.proxy.rlwy.net:45678/railway
  ```
* **Cấu hình Spring Boot**:
  ```bash
  SPRING_PROFILES_ACTIVE=postgres
  DB_URL=jdbc:postgresql://roundhouse.proxy.rlwy.net:45678/railway?sslmode=require
  DB_USER=postgres
  DB_PASSWORD=randomrailwaypass
  ```

---

## 3. HƯỚNG DẪN TỪNG BƯỚC KHỞI TẠO CƠ SỞ DỮ LIỆU TRÊN CLOUD

### Quy Trình 1: Khởi Tạo Trên NEON (Khuyên dùng - Nhanh nhất)

1. Truy cập [https://console.neon.tech](https://console.neon.tech) và đăng nhập bằng tài khoản GitHub hoặc Google.
2. Bấm **New Project**:
   * **Project name**: `xyz-auto-service`
   * **Postgres version**: `16` (hoặc mới nhất)
   * **Region**: Chọn khu vực gần người dùng nhất (ví dụ: `Singapore / Asia Pacific (Singapore)`).
   * Bấm **Create Project**.
3. Tại thanh menu bên trái, chọn **SQL Editor**:
   * Mở tệp [`Data/cloud_postgres_init.sql`](file:///c:/O%20E/Qu%E1%BA%A3n%20l%C3%BD%20d%E1%BB%B1%20%C3%A1n%20ph%E1%BA%A7n%20m%E1%BB%81m/%C4%90%E1%BB%93%20%C3%A1n%20m%C3%B4n%20h%E1%BB%8Dc/demo/Data/cloud_postgres_init.sql) trên máy.
   * Sao chép toàn bộ nội dung và dán vào cửa sổ SQL Editor của Neon.
   * Bấm nút **Run (Ctrl + Enter)**.
4. Kiểm tra kết quả hiển thị bảng tóm tắt:
   * `workshop_settings`: 1 dòng
   * `workshop_accounts`: 3 dòng
   * `workshop_services`: 8 dòng
   * `workshop_appointments`: 3 dòng
   * `workshop_appointment_services`: 4 dòng
5. Lấy chuỗi kết nối:
   * Quay lại mục **Dashboard**.
   * Tại thẻ **Connection Details**, chọn mục hiển thị **Parameters only** hoặc sao chép URL và áp dụng công thức chuyển đổi ở Mục 2.3[A].

---

### Quy Trình 2: Khởi Tạo Trên SUPABASE

1. Truy cập [https://supabase.com/dashboard](https://supabase.com/dashboard) và đăng nhập.
2. Bấm **New project**:
   * **Organization**: Chọn tổ chức của bạn.
   * **Name**: `xyz-auto-service`
   * **Database Password**: Nhập mật khẩu mạnh (lưu lại mật khẩu này để điền vào `DB_PASSWORD`).
   * **Region**: `Southeast Asia (Singapore)`.
   * Bấm **Create new project** (chờ 1–2 phút để cơ sở dữ liệu sẵn sàng).
3. Tại menu bên trái, chọn biểu tượng **SQL Editor** (`>_`):
   * Bấm **New query**.
   * Dán toàn bộ nội dung tệp [`Data/cloud_postgres_init.sql`](file:///c:/O%20E/Qu%E1%BA%A3n%20l%C3%BD%20d%E1%BB%B1%20%C3%A1n%20ph%E1%BA%A7n%20m%E1%BB%81m/%C4%90%E1%BB%93%20%C3%A1n%20m%C3%B4n%20h%E1%BB%8Dc/demo/Data/cloud_postgres_init.sql).
   * Bấm nút **Run**.
4. Lấy thông tin kết nối:
   * Vào **Project Settings (biểu tượng bánh răng) → Database**.
   * Kéo xuống mục **Connection string → URI**.
   * Sao chép chuỗi kết nối và áp dụng công thức ở Mục 2.3[B].

---

## 4. CHI TIẾT CẤU TRÚC BẢNG & RÀNG BUỘC TOÀN VẸN (SCHEMA DESIGN)

```mermaid
erDiagram
    workshop_settings {
        bigint id PK
        int capacity
    }
    workshop_accounts {
        bigserial id PK
        varchar phone UK
        varchar name
        varchar password_hash
        varchar role
        varchar meta
    }
    workshop_services {
        varchar id PK
        varchar name
        varchar category
        int duration
        bigint price
        varchar warranty
        varchar description
        varchar checklist
        boolean active
    }
    workshop_appointments {
        bigserial id PK
        bigint customer_id FK
        varchar customer_name
        varchar phone
        varchar license_plate
        varchar car_model
        date booking_date
        varchar slot
        varchar status
        int total_duration
        bigint total_price
        varchar bay
        varchar technician
        varchar note
        varchar staff_note
    }
    workshop_appointment_services {
        bigint appointment_id FK
        varchar service_code
        varchar service_name
        bigint service_price
        int service_duration
    }

    workshop_accounts ||--o{ workshop_appointments : "books"
    workshop_appointments ||--|{ workshop_appointment_services : "contains"
```

### Điểm nhấn thiết kế kỹ thuật:
1. **Chống Race-Condition Đặt Lịch (Partial Unique Index)**:
   ```sql
   CREATE UNIQUE INDEX IF NOT EXISTS uq_appointment_slot_plate 
   ON workshop_appointments (booking_date, slot, license_plate) 
   WHERE status NOT IN ('CANCELLED', 'REJECTED', 'NO_SHOW');
   ```
   Ràng buộc này ngăn chặn triệt để tình trạng 2 giao dịch đồng thời đặt trùng một xe vào cùng một khung giờ, nhưng vẫn cho phép đặt lại nếu lịch hẹn trước đó đã bị hủy (`CANCELLED`) hoặc từ chối (`REJECTED`).

2. **Bảo tồn lịch sử giá (Snapshot Pattern)**:
   * Bảng `workshop_appointment_services` lưu lại bản sao của dịch vụ (`service_name`, `service_price`, `service_duration`) tại thời điểm đặt lịch. Khi người quản lý điều chỉnh bảng giá dịch vụ trong `workshop_services`, toàn bộ doanh thu và lịch sử đặt hẹn trong quá khứ không bị ảnh hưởng sai lệch.

---

## 5. DANH MỤC TÀI KHOẢN MẪU KHỞI TẠO (SEED DATA)

Sau khi chạy `cloud_postgres_init.sql`, hệ thống sẵn sàng với 3 tài khoản thử nghiệm tương ứng 3 vai trò:

| Vai trò (Role) | Số điện thoại | Mật khẩu mặc định | Mô tả |
| :--- | :---: | :---: | :--- |
| **Quản lý (`MANAGER`)** | `0901234567` | `12345678` | Quản trị viên, cấu hình công suất tiếp nhận, quản lý bảng giá, xem báo cáo doanh thu |
| **Nhân viên (`STAFF`)** | `0912345678` | `12345678` | Tiếp nhận xe tại xưởng, duyệt lịch hẹn, xếp khoang (Bay 1–4), phân công kỹ thuật viên |
| **Khách hàng (`CUSTOMER`)** | `0988888888` | `12345678` | Khách hàng VIP có sẵn 2 lịch hẹn mẫu để kiểm thử tiến độ và đổi/hủy lịch |

---

## 6. XÁC MINH VÀ XỬ LÝ SỰ CỐ KẾT NỐI (TROUBLESHOOTING)

### 1. Lỗi: `FATAL: no pg_hba.conf entry for host ..., SSL off`
* **Nguyên nhân**: Máy chủ Cloud PostgreSQL từ chối các kết nối không được mã hóa.
* **Khắc phục**: Thêm tham số `?sslmode=require` vào cuối chuỗi `DB_URL`.

### 2. Lỗi: `PSQLException: Connection to ...:5432 refused`
* **Nguyên nhân**: Cổng hoặc địa chỉ Host không chính xác, hoặc mạng cục bộ chặn cổng 5432.
* **Khắc phục**: 
  * Kiểm tra lại chính xác Hostname từ bảng điều khiển nhà cung cấp Cloud.
  * Nếu dùng Supabase, hãy thử chuyển sang Transaction Pooler cổng `6543`.

### 3. Lỗi: `duplicate key value violates unique constraint "workshop_accounts_pkey"`
* **Nguyên nhân**: Chèn bản ghi mới khi sequence ID chưa được tăng tương ứng sau khi nạp seed data.
* **Khắc phục**: Chạy khối lệnh đồng bộ sequence trong `cloud_postgres_init.sql`:
  ```sql
  SELECT setval(
      COALESCE(pg_get_serial_sequence('workshop_accounts', 'id'), 'workshop_accounts_id_seq'),
      GREATEST((SELECT COALESCE(MAX(id), 0) FROM workshop_accounts), 1),
      (SELECT MAX(id) IS NOT NULL FROM workshop_accounts)
  );
  ```
