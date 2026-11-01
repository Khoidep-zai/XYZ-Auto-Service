# XYZ Auto Service — Hệ thống Quản lý Đặt lịch Dịch vụ Ô tô XYZ

Ứng dụng web đặt lịch bảo dưỡng, quản lý công suất xưởng và điều phối tiếp nhận xe ô tô trực tuyến, phục vụ đồ án môn học **Quản lý dự án phần mềm (71ITSE41503)**.

---

## Hướng dẫn triển khai và phiên bản phần mềm có thể trình diễn (Câu 8)

### 1. Thông tin phiên bản trình diễn
- **Mã phiên bản**: `v1.0.0-RELEASE (Capstone Production Candidate)`
- **Kiến trúc phần mềm**: Clean Architecture (Controller → Service → Repository → JPA Entity)
- **Công nghệ Backend**: Java 21, Spring Boot 4.1.2, Spring Security, Spring Data JPA, Hibernate, Validation, PostgreSQL Driver (kèm MySQL fallback).
- **Công nghệ Frontend**: Server-side Rendering với Thymeleaf, Tailwind CSS, Responsive Design (Desktop & Mobile).
- **Hệ quản trị CSDL**: PostgreSQL 14+ (chuẩn hóa 3NF, hỗ trợ UTF-8).
- **Cổng dịch vụ mặc định**: `http://localhost:8080`

---

### 2. Hướng dẫn triển khai từ máy trắng (Step-by-Step Deployment)

#### Bước 1: Chuẩn bị môi trường
- Cài đặt **JDK 21** (hoặc OpenJDK 21) và kiểm tra: `java -version`
- Cài đặt **PostgreSQL 14+** và khởi động dịch vụ cơ sở dữ liệu.

#### Bước 2: Tạo và khởi tạo Cơ sở dữ liệu PostgreSQL
Mở terminal hoặc pgAdmin và thực thi các lệnh sau:

```sql
-- Tạo cơ sở dữ liệu với mã hóa UTF-8
CREATE DATABASE xyz_auto_service WITH ENCODING 'UTF8';
```

Nạp cấu trúc bảng, ràng buộc và dữ liệu mẫu có sẵn qua tệp `xyz_auto_service_postgresql.sql`:
```powershell
# Thực thi qua công cụ psql
psql -U postgres -d xyz_auto_service -f xyz_auto_service_postgresql.sql
```

#### Bước 3: Thiết lập biến môi trường và chạy ứng dụng
Trong thư mục `demo`, mở PowerShell và chạy:

```powershell
# 1. Cấu hình biến môi trường kết nối PostgreSQL
$env:DB_URL = 'jdbc:postgresql://localhost:5432/xyz_auto_service'
$env:DB_USER = 'postgres'
$env:DB_PASSWORD = 'mat_khau_postgresql_cua_ban'

# 2. Khởi chạy ứng dụng qua Maven Wrapper
.\mvnw.cmd spring-boot:run
```
*(Trên Linux/macOS: Dùng `./mvnw spring-boot:run`)*.

Ứng dụng sẽ khởi động và sẵn sàng nhận kết nối tại: **`http://localhost:8080`**.

---

### 3. Danh sách tài khoản thử nghiệm có sẵn

| Vai trò | Tên đăng nhập / SĐT | Mật khẩu | Mục đích kiểm thử / Trình diễn |
| :--- | :--- | :--- | :--- |
| **Khách hàng mẫu** | `0912345678` | `password123` | Đặt lịch, xem danh sách lịch hẹn cá nhân (Xe: 51F-12345) |
| **Nhân viên Lễ tân** | `letan` | `password123` | Tiếp nhận xe tại xưởng, duyệt lịch hẹn, gán khoang dịch vụ |
| **Quản lý Xưởng** | `admin` | `password123` | Xem Dashboard công suất, doanh thu, điều phối kỹ thuật viên |

*(Khách hàng mới có thể tự đăng ký tài khoản trực tiếp ngay trên giao diện web).*

---

### 4. Kịch bản trình diễn luồng nghiệp vụ chính (Câu 8 - 6 bước chuẩn)

Hệ thống hỗ trợ hoàn hảo luồng nghiệp vụ khép kín theo đúng yêu cầu đề bài:

```text
Khách hàng xem dịch vụ → Chọn thời gian → Đặt lịch → Hệ thống kiểm tra điều kiện → Lưu lịch hẹn → Nhân viên xác nhận lịch
```

1. **Bước 1: Khách hàng xem danh mục dịch vụ**
   - Truy cập `http://localhost:8080`.
   - Xem 8 gói dịch vụ bảo dưỡng tiêu chuẩn với thời gian ước tính và bảng giá niêm yết rõ ràng.
2. **Bước 2: Chọn ngày và khung giờ hẹn**
   - Bấm nút **"Đặt lịch ngay"**.
   - Chọn ngày hẹn trong tương lai (từ thứ Hai đến thứ Bảy).
   - Chọn 1 trong 8 khung giờ (08:00–11:00 và 13:00–16:00). Hệ thống hiển thị trực quan số lượng slot còn trống (tối đa 3 xe/giờ).
3. **Bước 3: Nhập thông tin phương tiện**
   - Nhập Biển số xe (ví dụ: `51K-999.88`), Hãng xe, Dòng xe và Ghi chú tình trạng xe.
4. **Bước 4: Hệ thống tự động kiểm tra điều kiện nghiệp vụ**
   - **Quy tắc 1**: Kiểm tra công suất trạm không vượt quá 3 xe trong cùng khung giờ.
   - **Quy tắc 2**: Kiểm tra biển số xe chưa có lịch hẹn khác đang chờ trong cùng ngày.
   - **Quy tắc 3**: Cơ chế khóa dòng CSDL (`SELECT ... FOR UPDATE`) bảo vệ chống tranh chấp đồng thời khi nhiều khách hàng bấm đặt lịch cùng một giây.
5. **Bước 5: Lưu lịch hẹn thành công**
   - Hệ thống tự sinh mã lịch hẹn duy nhất dạng: `APT-YYYYMMDD-XXXX`.
   - Lưu trữ an toàn vào CSDL PostgreSQL với trạng thái ban đầu là `PENDING`.
   - Hiển thị pop-up thông báo thành công và điều hướng sang trang chi tiết lịch hẹn.
6. **Bước 6: Nhân viên xác nhận lịch hẹn tại xưởng**
   - Đăng xuất hoặc mở tab ẩn danh, đăng nhập bằng tài khoản Lễ tân (`letan` / `password123`).
   - Vào mục **"Lịch hẹn xưởng"**, lọc theo ngày hẹn vừa tạo.
   - Bấm nút **"Tiếp nhận / Xác nhận"** (`CONFIRMED`).
   - Gán khoang dịch vụ (Khoang 1–4) và phân công Kỹ thuật viên phụ trách.

---

### 5. Minh chứng kiểm thử tự động

Hệ thống tích hợp sẵn các bộ kiểm thử tự động (Unit Test & Integration Test) bao phủ luồng phân quyền, kiểm tra công suất, chống trùng lịch và luồng trạng thái xưởng:

```powershell
# Chạy toàn bộ kiểm thử
.\mvnw.cmd test
```

**Kết quả kiểm thử thực tế**:
```text
[INFO] Running com.xyz.autoservice.DemoApplicationTests
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.842 s
[INFO] Running com.xyz.autoservice.WorkshopFlowTests
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.115 s
[INFO] Results:
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

### 6. Cấu trúc thư mục mã nguồn

```text
demo/
├── pom.xml                                <-- Cấu hình dependencies (Spring Boot, PostgreSQL, Lombok,...)
├── README.md                              <-- Hướng dẫn triển khai và tài liệu vận hành
├── xyz_auto_service_postgresql.sql        <-- Script DDL và dữ liệu demo PostgreSQL
├── src/main/java/com/xyz/autoservice/
│   ├── config/                            <-- Cấu hình Spring Security, DataSource
│   ├── controller/                        <-- Xử lý Web request (Booking, Workshop, Auth, Service)
│   ├── dto/                               <-- Data Transfer Objects, Form validation
│   ├── entity/                            <-- JPA Entities (Appointment, ServiceItem, User,...)
│   ├── repository/                        <-- Spring Data JPA Repositories
│   └── service/                           <-- Nghiệp vụ lõi (CapacityChecker, BookingService,...)
└── src/main/resources/
    ├── application.properties             <-- Cấu hình kết nối PostgreSQL
    ├── static/                            <-- CSS biên dịch, hình ảnh giao diện
    └── templates/                         <-- Giao diện người dùng Thymeleaf
```
