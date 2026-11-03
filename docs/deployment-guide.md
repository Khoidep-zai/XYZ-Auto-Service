# HƯỚNG DẪN TRIỂN KHAI VÀ VẬN HÀNH (DEPLOYMENT & OPERATIONS GUIDE)
## XYZ Auto Service — Production & Development Environments

---

## 1. YÊU CẦU MÔI TRƯỜNG TIÊN QUYẾT (PREREQUISITES)

* **Hệ điều hành**: Windows 10/11, macOS hoặc Linux.
* **Java Development Kit (JDK)**: **JDK 21 LTS** (hoặc OpenJDK 21 / Microsoft OpenJDK 21).
  * Kiểm tra qua terminal: `java -version`.
* **Môi trường phát triển đề xuất**: **IntelliJ IDEA** (Bản Ultimate hoặc Community Edition 2024–2026).
* **Cơ sở dữ liệu (Tùy chọn)**:
  * Cách nhanh nhất (Dev Profile): Không cần cài đặt CSDL, dùng H2 In-Memory giả lập PostgreSQL.
  * Cách chuẩn Production: PostgreSQL 14+ (có sẵn dịch vụ cục bộ hoặc chạy qua Docker Compose).

---

## 2. TRIỂN KHAI 1-CLICK TRÊN INTELLIJ IDEA (KHUYÊN DÙNG)

Dự án đã được tích hợp sẵn các **Shared Run Configurations** trong thư mục `.idea/runConfigurations/`.

### Bước 1: Mở dự án trong IntelliJ IDEA
1. Khởi động IntelliJ IDEA.
2. Chọn **File → Open...** và duyệt đến thư mục `demo`.
3. IntelliJ sẽ tự động nhận diện tệp `pom.xml` và tải các thư viện Maven cần thiết.

### Bước 2: Thiết lập Project SDK
1. Vào **File → Project Structure...** (hoặc nhấn `Ctrl + Alt + Shift + S`).
2. Tại mục **Project → SDK**: Chọn **JDK 21** (ví dụ `ms-21` hoặc JDK 21 bạn đã cài đặt).
3. Bấm **Apply → OK**.

### Bước 3: Khởi chạy "Bấm một phát là chạy ngay"
1. Trên thanh công cụ phía trên góc phải của IntelliJ, tại danh sách Run Configurations, chọn:
   👉 **`XYZ Auto Service (1-Click Run)`**
2. Bấm vào nút **Run ▶** (hoặc nhấn `Shift + F10`).
3. **Kết quả**:
   * Spring Boot khởi động sau ~3 giây.
   * CSDL In-Memory tự động khởi tạo bảng, nạp 8 dịch vụ tiêu chuẩn và 3 tài khoản mặc định.
   * Cả Backend và Frontend cùng chạy tại: **`http://localhost:8080`**.

---

## 3. TRIỂN KHAI VỚI CƠ SỞ DỮ LIỆU POSTGRESQL THẬT

### Cách A: Sử dụng Docker Compose (Nhanh nhất & Độc lập)
Dự án có sẵn file [`docker-compose.yml`](file:///c:/O%20E/Qu%E1%BA%A3n%20l%C3%BD%20d%E1%BB%B1%20%C3%A1n%20ph%E1%BA%A7n%20m%E1%BB%81m/%C4%90%E1%BB%93%20%C3%A1n%20m%C3%B4n%20h%E1%BB%8Dc/demo/docker-compose.yml):
1. Mở terminal tại thư mục gốc của dự án, chạy lệnh:
   ```bash
   docker compose up -d
   ```
2. Container PostgreSQL 16 sẽ tự động khởi tạo trên cổng `5432`, tạo database `xyz_auto_service` và tự động thực thi script `xyz_auto_service_postgresql.sql`. Script có lệnh `DROP TABLE` và chỉ phù hợp với cơ sở dữ liệu thử; không chạy lại trên dữ liệu cần giữ.
3. Chạy ứng dụng với cấu hình trỏ vào cổng 5432:
   ```powershell
   $env:DB_URL = "jdbc:postgresql://localhost:5432/xyz_auto_service"
   $env:DB_USER = "postgres"
   $env:DB_PASSWORD = "postgres"
   .\mvnw.cmd spring-boot:run
   ```

### Cách B: Sử dụng dịch vụ PostgreSQL cục bộ trên Windows (Cổng 5433)
Trên máy thực tế của bạn, PostgreSQL 18 đang lắng nghe tại cổng **`5433`**:
1. Mở pgAdmin hoặc công cụ psql kết nối vào cổng 5433:
   ```sql
   CREATE DATABASE xyz_auto_service WITH ENCODING 'UTF8';
   ```
2. Chỉ nạp dữ liệu mẫu vào cơ sở dữ liệu thử trống: script xóa các bảng hiện có.
   Sao lưu dữ liệu cần giữ và không chạy script này trên cơ sở dữ liệu đang sử dụng.
   ```powershell
   & "C:\Program Files\PostgreSQL\18\bin\psql.exe" -U postgres -p 5433 -d xyz_auto_service -f xyz_auto_service_postgresql.sql
   ```
3. Trong IntelliJ IDEA, chọn cấu hình:
   👉 **`XYZ Auto Service (PostgreSQL)`**
   Sau đó bấm **Run ▶**. Cấu hình này đã được thiết lập sẵn biến môi trường trỏ trực tiếp đến cổng `5433`!

---

## 4. DANH SÁCH TÀI KHOẢN MẪU ĐỂ TEST HỆ THỐNG

| Vai trò người dùng | Số điện thoại đăng nhập | Mật khẩu mặc định | Nhiệm vụ kiểm thử / Trình diễn |
| :--- | :---: | :---: | :--- |
| **Quản lý xưởng (`MANAGER`)** | `0901234567` | `12345678` | Quản lý danh mục dịch vụ, đổi năng lực tiếp nhận (capacity), xem Dashboard thống kê và doanh thu dự kiến |
| **Nhân viên tiếp nhận (`STAFF`)** | `0912345678` | `12345678` | Tiếp nhận xe tại xưởng, duyệt lịch hẹn, phân khoang Bay 1–4, gán KTV, tiếp nhận khách vãng lai |
| **Khách hàng mẫu (`CUSTOMER`)** | `0988888888` | `12345678` | Khách hàng VIP, đặt lịch trực tuyến, theo dõi tiến độ sửa chữa, đổi lịch hoặc hủy lịch |

*(Khách hàng mới có thể tự bấm "Đăng nhập / Đăng ký" trên giao diện web để tạo tài khoản riêng).*

---

## 5. XỬ LÝ SỰ CỐ THƯỜNG GẶP (TROUBLESHOOTING)

1. **Lỗi `Connection refused: localhost:5432`**:
   * *Nguyên nhân*: Cổng 5432 chưa được mở hoặc PostgreSQL trên máy đang chạy ở cổng 5433.
   * *Khắc phục*: Dùng cấu hình `XYZ Auto Service (1-Click Run)` để chạy mượt mà ngay lập tức với profile `dev`, hoặc chỉnh biến môi trường `DB_URL` trỏ vào cổng `5433`.
2. **Lỗi `JAVA_HOME is not defined correctly` khi chạy Maven qua dòng lệnh**:
   * *Khắc phục*: Thiết lập biến môi trường trỏ đến JDK 21 trước khi chạy:
     ```powershell
     $env:JAVA_HOME = "$env:USERPROFILE\.jdks\ms-21.0.11"
     .\mvnw.cmd test
     ```
3. **Cổng 8080 bị chiếm dụng bởi ứng dụng khác**:
   * *Khắc phục*: Chạy với tham số ghi đè cổng:
     `--server.port=8081` hoặc đặt `$env:SERVER_PORT=8081`.

