# 👥 Đội ngũ Phát triển & Đóng góp Dự án (Contributors)

Dự án **XYZ Auto Service — WorkshopOS** được xây dựng và phát triển trong khuôn khổ học phần **Quản lý Dự án Phần mềm (Mã lớp học phần: 71ITSE41503)**, Khoa Công nghệ Thông tin.

---

## 🏆 Danh sách Thành viên Nhóm

| STT | Họ và tên | Mã số sinh viên (MSSV) | Vai trò trong nhóm (Scrum Role) | GitHub Profile | Liên hệ |
| :---: | :--- | :---: | :--- | :--- | :--- |
| 1 | **Nguyễn Bá Anh Khôi** | `2374802010247` | **Scrum Master** kiêm Backend Developer | [@Khoidep-zai](https://github.com/Khoidep-zai) | `nguyenkhoidk2005@gmail.com` |
| 2 | **Trần Văn Khoa** | `2374802013414` | **Product Owner** kiêm Frontend Developer | [@Khoa-Tran](https://github.com/) | — |
| 3 | **Nguyễn Tiến Dũng** | `2274802010102` | **Developer** kiêm Quality & Database Specialist | [@NguyenTienDung29](https://github.com/NguyenTienDung29) | `ntd29042004@gmail.com` |

---

## 🌟 Ghi nhận Quá trình & Đóng góp Kỹ thuật của Nguyễn Tiến Dũng (@NguyenTienDung29)

Nguyễn Tiến Dũng phụ trách vai trò **Developer kiêm Quality & Database Specialist**, đã đóng góp nhiều giải pháp kỹ thuật, tái cấu trúc mã nguồn và kiểm thử chất lượng quan trọng cho dự án:

### 1. Kiến trúc Phân tách Mô-đun Frontend & Backend
- Đề xuất và triển khai tái cấu trúc mã nguồn dự án thành 2 phân hệ độc lập:
  - `frontend/`: Chứa toàn bộ giao diện tĩnh (HTML5, Tailwind CSS, JavaScript Single Page Application, vendor FontAwesome).
  - `backend/`: Chứa toàn bộ dịch vụ máy chủ Spring Boot, JPA, Security và REST API.
- Cấu hình `maven-resources-plugin` trong `backend/pom.xml` để tự động hóa việc đồng bộ và đóng gói tài nguyên từ `frontend/` vào classpath `target/classes/static`, duy trì mô hình phân phối Single-Origin an toàn, không phát sinh lỗi CORS.

### 2. Tối ưu hóa Logic Nghiệp vụ & An ninh Bảo mật (Security Hardening)
- **Kiểm soát định danh & Chống giả mạo thông tin (`WorkshopService.java`)**:
  - Tại luồng khách hàng đặt lịch trực tuyến (`book`): Thiết lập ràng buộc bắt buộc tài khoản người dùng phải có vai trò `CUSTOMER` và số điện thoại đặt lịch phải trùng khớp tuyệt đối với số điện thoại đăng nhập của người dùng (`actor.phone.equals(input.phone())`).
  - Ghi đè tự động `customerName` và `phone` từ tài khoản xác thực vào thực thể lịch hẹn, triệt tiêu nguy cơ người dùng đặt lịch mạo danh số điện thoại người khác.
  - Tại luồng tiếp nhận xe trực tiếp tại quầy (`walkIn`): Áp đặt kiểm tra quyền hạn bắt buộc vai trò nhân viên (`requireStaff(actor)`), ngăn chặn người dùng thường gọi trái phép API tiếp nhận trực tiếp.

### 3. Phát triển Bộ Kiểm thử Tự động & Đảm bảo Chất lượng (Quality Assurance)
- Mở rộng bộ kiểm thử tích hợp tự động `backend/src/test/java/com/example/demo/WorkshopFlowTests.java`:
  - Bổ sung test case xác thực phản hồi `400 BAD REQUEST` khi khách hàng cố ý đặt lịch bằng số điện thoại người khác.
  - Bổ sung test case xác thực phản hồi `403 FORBIDDEN` khi tài khoản nhân viên cố ý gọi luồng đặt online.
- Phát triển kịch bản kiểm thử E2E tự động `backend/scripts/smoke_booking.py` bằng Python 3 thuần (không phụ thuộc thư viện ngoài):
  - Tự động lấy CSRF Token và Cookie phiên làm việc.
  - Tự động thực hiện luồng đặt lịch cho Khách hàng $\rightarrow$ Xác thực phản hồi $\rightarrow$ Đăng nhập Nhân viên $\rightarrow$ Phê duyệt lịch hẹn, xếp Khoang kỹ thuật và phân công Kỹ thuật viên $\rightarrow$ Xác nhận trạng thái `CONFIRMED`.

### 4. Tối ưu hóa Cơ sở Dữ liệu & Môi trường Chạy
- Chuẩn hóa cổng kết nối PostgreSQL về chuẩn `5432` trong `backend/src/main/resources/application-postgres.properties` và `backend/docker-compose.yml`.
- Cập nhật kịch bản khởi tạo cơ sở dữ liệu `xyz_auto_service_postgresql.sql` với mã băm BCrypt chuẩn cho mật khẩu khởi tạo `12345678`.
- Cấu hình môi trường kiểm thử H2 tương thích chế độ PostgreSQL (`MODE=PostgreSQL`) giúp đồng nhất cú pháp SQL giữa môi trường test và production.

### 5. Nâng cao Trải nghiệm Nhà phát triển (Developer Experience)
- Xây dựng các script 1-click khởi chạy đa nền tảng:
  - `backend/run-dev.cmd` & `backend/run-dev.sh`: Khởi chạy nhanh chế độ phát triển (In-Memory H2).
  - `backend/run-postgres.cmd` & `backend/run-postgres.sh`: Khởi chạy chế độ kết nối cơ sở dữ liệu PostgreSQL thực tế.

---

## 📈 Ma trận Trách nhiệm Dự án (RACI Matrix)

| Hạng mục công việc | Nguyễn Bá Anh Khôi (SM) | Trần Văn Khoa (PO) | Nguyễn Tiến Dũng (Dev/QA) |
| :--- | :---: | :---: | :---: |
| **Quản trị Tiến độ, Sprint & EVM** | **A / R** | C | I |
| **Đặc tả Yêu cầu & Product Backlog** | C | **A / R** | C |
| **Thiết kế Giao diện (Figma & UI)** | I | **A / R** | C |
| **Kiến trúc Hệ thống & API Design** | **A / R** | C | R |
| **Phân tách Module & Build Automation** | A | I | **R** |
| **Backend Service & Pessimistic Lock** | **A / R** | I | R |
| **Security Checks & Identity Protection** | A | I | **R** |
| **Thiết kế CSDL & PostgreSQL Script** | C | I | **A / R** |
| **Tự động hóa Kiểm thử & Smoke Test** | C | I | **A / R** |
| **Hồ sơ Quản trị Dự án (@docs)** | **A / R** | R | R |

*(Ghi chú: **R** - Responsible; **A** - Accountable; **C** - Consulted; **I** - Informed)*
