# BẢN GHI THAY ĐỔI THIẾT KẾ VÀ QUYẾT ĐỊNH KIẾN TRÚC (ADRs)
## Architecture Decision Records & Design Change Log

---

## 1. NHẬT KÝ THAY ĐỔI THIẾT KẾ (DESIGN CHANGE LOG)

| Phiên bản | Ngày | Người thực hiện | Hạng mục thay đổi | Lý do / Tác động kỹ thuật |
| :---: | :---: | :--- | :--- | :--- |
| **v1.0.0** | 15/10/2026 | Nhóm dự án | Thiết kế nguyên mẫu HTML ban đầu | Tạo giao diện tĩnh dùng localStorage mô phỏng chức năng |
| **v1.1.0** | 22/10/2026 | Nhóm dự án | Chuyển đổi sang Kiến trúc Clean Architecture | Thay thế hoàn toàn localStorage bằng Spring Boot Backend, RESTful API và Spring Security |
| **v1.2.0** | 28/10/2026 | Nhóm dự án | Chuẩn hóa CSDL sang PostgreSQL 14+ | Cấu hình Schema 3NF, hỗ trợ UTF-8 cho dữ liệu tiếng Việt có dấu, bổ sung index |
| **v1.3.0** | 01/11/2026 | Senior Engineer | Tối ưu hóa 1-Click Run & Cơ chế Đa Profile | Bổ sung Profile `dev` (In-Memory PostgreSQL mode), tự động seed tài khoản, cấu hình Shared Run Configuration cho IntelliJ IDEA |

---

## 2. CÁC QUYẾT ĐỊNH KIẾN TRÚC CỐT LÕI (ARCHITECTURE DECISION RECORDS)

### ADR-001: Sử dụng Cơ chế Khóa Bi Quan (Pessimistic Lock) cho Đặt lịch Xưởng
* **Trạng thái**: **ĐÃ CHẤP THUẬN (ACCEPTED)**.
* **Bối cảnh**:
  * Mỗi khung giờ tại xưởng chỉ có thể tiếp nhận tối đa số lượng xe giới hạn (`capacity = 3`). Khi có chương trình ưu đãi hoặc giờ cao điểm, nhiều khách hàng có thể cùng bấm đặt lịch vào đúng một giây.
  * Nếu dùng Optimistic Lock (`@Version`), transaction đến sau sẽ bị Exception và buộc phải Retry, gây trải nghiệm người dùng kém và tốn tài nguyên hệ thống.
* **Quyết định**:
  * Áp dụng `@Lock(LockModeType.PESSIMISTIC_WRITE)` (`SELECT ... FOR UPDATE`) lên bản ghi cấu hình `workshop_settings` (ID = 1) khi bắt đầu giao dịch đặt lịch.
* **Hệ quả**:
  * Đảm bảo tính tuần tự tuyệt đối (Strict Serializability) trong cửa sổ kiểm tra sức chứa.
  * Không bao giờ xảy ra tình trạng nhận quá công suất (Overbooking).
  * Độ trễ tăng thêm không đáng kể (< 5ms) vì thời gian giữ khóa cực ngắn.

---

### ADR-002: Áp dụng Chiến lược Lưu Bản chụp Giá và Tên Dịch vụ (Snapshotting)
* **Trạng thái**: **ĐÃ CHẤP THUẬN (ACCEPTED)**.
* **Bối cảnh**:
  * Bảng giá dịch vụ và tên dịch vụ kỹ thuật có thể được Giám đốc xưởng cập nhật theo thời gian hoặc theo biến động giá thị trường.
  * Nếu lịch hẹn chỉ liên kết Khóa ngoại (Foreign Key) `service_id` trỏ đến bảng `workshop_services`, khi quản trị viên sửa giá gói dịch vụ, tổng chi phí của các lịch hẹn đã chốt trong quá khứ sẽ bị sai lệch.
* **Quyết định**:
  * Nhúng bảng phụ `workshop_appointment_services` làm `@Embeddable` (`ServiceSnapshot`), lưu cứng `service_name`, `service_price`, `service_duration` tại thời điểm khách hàng bấm đặt lịch hoặc đổi lịch.
* **Hệ quả**:
  * Lịch sử giao dịch và doanh thu báo cáo luôn chính xác 100% theo đúng cam kết ban đầu với khách hàng.

---

### ADR-003: Kiến trúc Single-Source Monolith tích hợp Frontend tĩnh trong Spring Boot
* **Trạng thái**: **ĐÃ CHẤP THUẬN (ACCEPTED)**.
* **Bối cảnh**:
  * Dự án cần bàn giao và trình diễn mượt mà trong 3 tuần, phục vụ hội đồng chấm đồ án học phần.
  * Nếu tách riêng Frontend (React/Vue/Vite) chạy trên port 3000/5173 và Backend chạy trên port 8080:
    * Người chấm bài phải cài đặt cả Node.js, npm, Java, cài đặt thư viện `node_modules` tốn thời gian.
    * Phát sinh lỗi CORS, phức tạp hóa việc xử lý Session Cookie và CSRF Token qua domain khác nguồn gốc (Cross-Origin).
* **Quyết định**:
  * Đóng gói toàn bộ tài nguyên Frontend (HTML, CSS, JS, Fonts) vào `src/main/resources/static/`. Spring Boot đóng vai trò máy chủ duy nhất phục vụ toàn bộ ứng dụng.
* **Hệ quả**:
  * Trải nghiệm "1-Click Run": Chỉ cần bấm nút Run trong IntelliJ IDEA hoặc chạy 1 lệnh Maven duy nhất, cả hệ thống tự động hoạt động hoàn chỉnh tại `http://localhost:8080`.
  * Không phát sinh lỗi CORS, việc gửi nhận Cookie bảo mật và CSRF Token trở nên hoàn toàn tự nhiên và an toàn.

