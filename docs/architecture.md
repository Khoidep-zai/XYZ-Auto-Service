# TÀI LIỆU KIẾN TRÚC HỆ THỐNG (SYSTEM ARCHITECTURE SPECIFICATION)
## XYZ Auto Service — Workshop Operating System

---

## 1. TỔNG QUAN PHONG CÁCH KIẾN TRÚC

Hệ thống XYZ Auto Service được thiết kế theo phong cách **Kiến trúc phân tầng sạch (Clean Layered Architecture)** kết hợp với mô hình **Single-Source-of-Truth Full-Stack Monolith**:

```text
[ Trình duyệt Khách hàng / Nhân viên / Quản lý ]
                      │
           HTTP / JSON / Same-Origin
                      ▼
┌─────────────────────────────────────────────────────────────┐
│                   SPRING BOOT CONTAINER                     │
│                                                             │
│  [ Spring Security & CSRF Filter Chain ]                   │
│         │                                                   │
│         ▼                                                   │
│  [ Presentation Layer: WorkshopController ]                 │
│         │ (DTO Validation with Jakarta Bean Validation)     │
│         ▼                                                   │
│  [ Domain / Business Logic Layer: WorkshopService ]         │
│         │ (ACID Transactions & Pessimistic Concurrency Lock)│
│         ▼                                                   │
│  [ Persistence Layer: Spring Data JPA Repositories ]        │
│         │ (Hibernate ORM 7.x)                               │
│         ▼                                                   │
│  [ Domain Entities: WorkshopDomain (3NF Normalized) ]       │
└─────────────────────────────┬───────────────────────────────┘
                              │
               JDBC / Hikari Connection Pool
                              ▼
        ┌───────────────────────────────────────────┐
        │        POSTGRESQL 14+ / H2 IN-MEMORY      │
        │  (5 Bảng chuẩn hóa 3NF, UTF-8 Vietnamese) │
        └───────────────────────────────────────────┘
```

---

## 2. PHÂN TÍCH CÁC TẦNG HỆ THỐNG (SYSTEM LAYERS)

### 2.1. Tầng Giao diện người dùng (Frontend Presentation Layer)
* **Vị trí**: Đóng gói trực tiếp trong thư mục `src/main/resources/static/`.
* **Công nghệ**:
  * HTML5 Semantics, Tailwind CSS (Design tokens: Slate, Indigo, Amber, Emerald, Crimson).
  * Vanilla Modern JavaScript (ES6+ Asynchronous `async/await`, Fetch API).
  * FontAwesome Icons phục vụ trực tiếp nội bộ (`/vendor/fontawesome/`).
* **Đặc tính kỹ thuật**:
  * Tải trang ban đầu dưới dạng Monolithic Static Assets từ Spring Boot, sau đó tương tác hoàn toàn dưới dạng **Single Page Application (SPA)** thông qua API `/api/state`.
  * Client State tự động đồng bộ mỗi 30 giây khi tab hoạt động, hoặc tức thời sau mỗi hành động đặt lịch/duyệt lịch.
  * Tự động xử lý CSRF token (`refreshCsrf`) cho mọi thao tác đột biến dữ liệu (Mutation).

### 2.2. Tầng Bảo mật (Security & Authorization Layer)
* **Vị trí**: [`SecurityConfig.java`](file:///c:/O%20E/Qu%E1%BA%A3n%20l%C3%BD%20d%E1%BB%B1%20%C3%A1n%20ph%E1%BA%A7n%20m%E1%BB%81m/%C4%90%E1%BB%93%20%C3%A1n%20m%C3%B4n%20h%E1%BB%8Dc/demo/src/main/java/com/example/demo/SecurityConfig.java).
* **Cơ chế**:
  * Xác thực qua form đăng nhập tùy biến tại `/api/login`, nhận tham số `phone` và `password`.
  * Quản lý phiên bằng `HttpSession` có cờ `HttpOnly = true`, `SameSite = Lax` chống tấn công đánh cắp Cookie qua XSS.
  * Phân quyền ở mức Controller và Method:
    * Khách truy cập công khai: Các tệp tĩnh, `/api/csrf`, `/api/state`, `/api/register`.
    * Đặt lịch / Đổi lịch / Hủy lịch: Yêu cầu đăng nhập (`authenticated`).
    * Quản lý dịch vụ, cấu hình công suất, tạo tài khoản nội bộ: `@PreAuthorize("hasRole('MANAGER')")`.
    * Duyệt lịch, gán khoang, phân công thợ: Yêu cầu quyền `STAFF` hoặc `MANAGER`.

### 2.3. Tầng Điều khiển API (Controller Layer)
* **Vị trí**: [`WorkshopController.java`](file:///c:/O%20E/Qu%E1%BA%A3n%20l%C3%BD%20d%E1%BB%B1%20%C3%A1n%20ph%E1%BA%A7n%20m%E1%BB%81m/%C4%90%E1%BB%93%20%C3%A1n%20m%C3%B4n%20h%E1%BB%8Dc/demo/src/main/java/com/example/demo/WorkshopController.java).
* **Trách nhiệm**:
  * Tiếp nhận yêu cầu HTTP, kiểm tra tính hợp lệ của DTO qua `@Valid` và `jakarta.validation.constraints`.
  * Trích xuất danh tính người dùng hiện tại từ `java.security.Principal`.
  * Bắt lỗi tập trung qua `@RestControllerAdvice` (`WorkshopController.Errors`): Chuyển đổi các ngoại lệ `ResponseStatusException`, `MethodArgumentNotValidException`, `DataIntegrityViolationException` thành phản hồi JSON có thông điệp tiếng Việt thân thiện.

### 2.4. Tầng Dịch vụ & Xử lý Nghiệp vụ (Business Logic Layer)
* **Vị trí**: [`WorkshopService.java`](file:///c:/O%20E/Qu%E1%BA%A3n%20l%C3%BD%20d%E1%BB%B1%20%C3%A1n%20ph%E1%BA%A7n%20m%E1%BB%81m/%C4%90%E1%BB%93%20%C3%A1n%20m%C3%B4n%20h%E1%BB%8Dc/demo/src/main/java/com/example/demo/WorkshopService.java).
* **Trách nhiệm**:
  * Thực thi toàn bộ 7 quy tắc nghiệp vụ (BR-01 đến BR-07).
  * Bảo đảm tính nguyên tử với `@Transactional` (Read-Write cho thao tác sửa đổi, `readOnly = true` cho truy vấn).
  * Snapshotting giá dịch vụ và thời lượng tại thời điểm đặt lịch.

### 2.5. Tầng Truy cập Dữ liệu & CSDL (Persistence Layer)
* **Vị trí**: [`WorkshopDomain.java`](file:///c:/O%20E/Qu%E1%BA%A3n%20l%C3%BD%20d%E1%BB%B1%20%C3%A1n%20ph%E1%BA%A7n%20m%E1%BB%81m/%C4%90%E1%BB%93%20%C3%A1n%20m%C3%B4n%20h%E1%BB%8Dc/demo/src/main/java/com/example/demo/WorkshopDomain.java) và các Repository.
* **Cơ sở dữ liệu**:
  * Chuẩn hóa 3NF gồm 5 bảng: `workshop_settings`, `workshop_accounts`, `workshop_services`, `workshop_appointments`, `workshop_appointment_services`.
  * Đánh chỉ mục hiệu năng cao (Database Indexes):
    * `idx_appointment_slot`: Trên các cột `(booking_date, slot, status)` tối ưu truy vấn đếm công suất theo thời gian thực.
    * `idx_appointment_customer`: Trên cột `(customer_id)` tối ưu truy vấn danh sách lịch của từng khách hàng.

---

## 3. CƠ CHẾ KIỂM SOÁT ĐỒNG THỜI (CONCURRENCY CONTROL)

Một trong những bài toán hóc búa nhất của hệ thống đặt lịch xưởng dịch vụ là **xung đột tranh chấp chỗ trống khi nhiều khách hàng cùng bấm đặt lịch vào đúng 1 mili-giây cuối cùng**.

### Giải pháp Khóa Bi Quan Cấp Hàng (Pessimistic Write Locking):
1. Khi khách hàng gửi yêu cầu đặt lịch (`POST /api/appointments`) hoặc đổi lịch:
   ```java
   @Transactional
   public Long book(Principal principal, ApiModels.BookingInput input) {
       var actor = current(principal);
       var config = lockSettings(); // KHÓA BI QUAN BẢNG SETTINGS (id = 1)
       ...
       validateSpace(config, appointment.date, appointment.slot, appointment.licensePlate, null);
       ...
       return appointments.saveAndFlush(appointment).id;
   }
   ```
2. Phương thức `lockSettings()` thực thi câu lệnh SQL:
   ```sql
   SELECT id, capacity FROM workshop_settings WHERE id = 1 FOR UPDATE;
   ```
3. **Ý nghĩa kỹ thuật**:
   * Câu lệnh `FOR UPDATE` buộc cơ sở dữ liệu khóa hàng cấu hình trung tâm trong suốt thời gian diễn ra transaction.
   * Mọi transaction đặt lịch song song khác buộc phải xếp hàng (Queue).
   * Transaction đang chạy sẽ kiểm tra chính xác số chỗ còn lại (`occupied.size() < config.capacity`), lưu lịch mới và nhả khóa.
   * Transaction tiếp theo sau đó mới đọc dữ liệu cập nhật, phát hiện slot đã đầy và trả về `409 Conflict` an toàn, ngăn chặn 100% tình trạng vượt công suất.

---

## 4. CHIẾN LƯỢC QUẢN LÝ MÔI TRƯỜNG & PROFILE DEPLOYMENT

Hệ thống hỗ trợ 2 profile chạy ứng dụng độc lập:

1. **Profile `dev` (Default - Khuyên dùng cho Demo & Chấm đồ án)**:
   * Kích hoạt mặc định: Không cần cài đặt trước PostgreSQL hay cấu hình mật khẩu.
   * Sử dụng CSDL In-Memory H2 cấu hình giả lập chuẩn PostgreSQL (`MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE`).
   * Tự động tạo bảng và nạp 8 dịch vụ chuẩn kèm 3 tài khoản mặc định.
   * Chạy tức thời trong 2 giây khi bấm nút Run trên IntelliJ IDEA.
2. **Profile `postgres` (Dành cho Môi trường Triển khai Thực tế)**:
   * Kết nối trực tiếp vào PostgreSQL 14+ trên cổng `5433` (hoặc cổng `5432` qua Docker Compose).
   * Dữ liệu được lưu trữ bền vững vĩnh viễn trên ổ đĩa.
   * Ràng buộc toàn vẹn khóa ngoại (Foreign Keys) và Indexes được kiểm soát chặt chẽ ở cấp độ CSDL.

