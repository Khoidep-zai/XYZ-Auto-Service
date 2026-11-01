# NHẬT KÝ LỖI VÀ BIỆN PHÁP KHẮC PHỤC (BUG LOG & POST-MORTEM)
## XYZ Auto Service — Issue Tracking & Engineering Resolutions

---

## 1. TỔNG HỢP CÁC SỰ CỐ ĐÃ PHÁT HIỆN VÀ XỬ LÝ TRIỆT ĐỂ

### BUG-01: Lệch cổng dịch vụ PostgreSQL (Port Mismatch dẫn đến Connection Refused)
* **Mức độ nghiêm trọng**: **CRITICAL (Chặn toàn bộ quá trình khởi chạy)**.
* **Mô tả hiện tượng**:
  * Khi khởi chạy ứng dụng Spring Boot bằng lệnh thông thường, ứng dụng crash ngay tại bước khởi tạo HikariPool với thông báo:
    `connection to server at "localhost" (127.0.0.1), port 5432 failed: Connection refused (0x0000274D/10061)`.
* **Phân tích nguyên nhân gốc rễ (Root Cause Analysis)**:
  * Trong tệp `application.properties`, chuỗi kết nối mặc định ghi cứng cổng `5432` (`localhost:5432`).
  * Thực tế trên máy tính người dùng Windows, dịch vụ `postgresql-x64-18` được cài đặt và cấu hình lắng nghe tại cổng **`5433`** (`C:\Program Files\PostgreSQL\18\data\postgresql.conf` dòng 64: `port = 5433`). Cổng 5432 hoàn toàn không có tiến trình nào lắng nghe.
* **Giải pháp khắc phục**:
  1. Cấu hình lại `spring.datasource.url` trỏ linh hoạt thông qua biến môi trường: `${DB_URL:jdbc:postgresql://localhost:5433/xyz_auto_service}`.
  2. Thiết lập cơ chế đa Profile (`dev` và `postgres`): Profile mặc định `dev` sử dụng CSDL In-Memory mô phỏng PostgreSQL, giúp bất kỳ máy nào mở IntelliJ lên cũng có thể bấm Run chạy ngay mà không bao giờ bị lỗi cổng DB.
* **Trạng thái**: **RESOLVED & VERIFIED**.

---

### BUG-02: Thiếu tài khoản mặc định khi khởi động trên Cơ sở dữ liệu trắng
* **Mức độ nghiêm trọng**: **HIGH (Ảnh hưởng nghiêm trọng đến trải nghiệm kiểm thử và demo)**.
* **Mô tả hiện tượng**:
  * Nếu người dùng chạy ứng dụng trên một database PostgreSQL mới tạo chưa thực thi script SQL `xyz_auto_service_postgresql.sql`, hệ thống chỉ tạo duy nhất tài khoản Quản lý nếu có biến môi trường `APP_BOOTSTRAP_ADMIN_PHONE`.
  * Hậu quả: Không có tài khoản Nhân viên tiếp nhận (`STAFF`) và Khách hàng (`CUSTOMER`), dẫn đến người dùng không thể đăng nhập để test luồng duyệt lịch hay kiểm tra chức năng.
* **Phân tích nguyên nhân gốc rễ**:
  * Trong `InitialData.java`, khối code khởi tạo phụ thuộc vào biến môi trường và điều kiện rỗng chưa bao quát.
* **Giải pháp khắc phục**:
  * Bổ sung phương thức tự động kiểm tra và đảm bảo tài khoản `ensureAccount(...)` trong `InitialData.java`.
  * Bất cứ khi nào ứng dụng khởi động (dù trên H2, PostgreSQL mới hay cũ), hệ thống tự động kiểm tra số điện thoại:
    * Nếu chưa có `0901234567`: Tự động tạo Quản lý xưởng (`MANAGER`, Pass: `12345678`).
    * Nếu chưa có `0912345678`: Tự động tạo Nhân viên tiếp nhận (`STAFF`, Pass: `12345678`).
    * Nếu chưa có `0988888888`: Tự động tạo Khách hàng VIP (`CUSTOMER`, Pass: `12345678`).
  * Đảm bảo luôn có sẵn 3 tài khoản cho 3 vai trò để hội đồng chấm điểm đăng nhập kiểm tra tức thì.
* **Trạng thái**: **RESOLVED & VERIFIED**.

---

### BUG-03: Lỗi đường dẫn chứa khoảng trắng và ký tự tiếng Việt khi chạy Maven trên Windows
* **Mức độ nghiêm trọng**: **MEDIUM (Gây lỗi biên dịch dòng lệnh)**.
* **Mô tả hiện tượng**:
  * Khi chạy `.\mvnw.cmd spring-boot:run` trên Windows PowerShell từ thư mục `C:\O E\Quản lý dự án phần mềm\Đồ án môn học\demo`, plugin Spring Boot Maven báo lỗi:
    `Error: Could not find or load main class com.example.demo.DemoApplication`.
* **Phân tích nguyên nhân gốc rễ**:
  * Thư mục dự án chứa ký tự tiếng Việt có dấu (`Quản lý dự án...`) và khoảng trắng. Cơ chế fork tiến trình JVM của plugin Maven trên Windows không bọc nháy kép chính xác chuỗi classpath khi truyền vào tham số dòng lệnh.
* **Giải pháp khắc phục**:
  1. Hướng dẫn đóng gói ứng dụng thành file JAR độc lập bằng `.\mvnw.cmd package -DskipTests` rồi chạy trực tiếp `java -jar target/demo-0.0.1-SNAPSHOT.jar`.
  2. Tạo cấu hình Run Configuration chuẩn trong IntelliJ IDEA (`.idea/runConfigurations/`), nơi IntelliJ tự động quản lý classpath qua tệp manifest ngắn (Path shortening), loại bỏ 100% rủi ro đường dẫn có dấu.
* **Trạng thái**: **RESOLVED & VERIFIED**.

---

### BUG-04: Thiếu thư viện H2 ở Runtime khi chạy chế độ Dev
* **Mức độ nghiêm trọng**: **HIGH**.
* **Mô tả hiện tượng**:
  * Trong file `pom.xml` ban đầu, dependency `h2` có `<scope>test</scope>`.
  * Khi chạy ứng dụng với Profile `dev` ở chế độ thường (ngoài phạm vi test), JVM ném ra lỗi `ClassNotFoundException: org.h2.Driver`.
* **Giải pháp khắc phục**:
  * Chuyển phạm vi phụ thuộc của H2 trong `pom.xml` từ `<scope>test</scope>` sang `<scope>runtime</scope>`.
  * Nhờ đó, H2 có mặt ở cả môi trường chạy thử nghiệm `dev` lẫn môi trường chạy kiểm thử tự động `test`.
* **Trạng thái**: **RESOLVED & VERIFIED**.

