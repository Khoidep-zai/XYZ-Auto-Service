# KẾ HOẠCH KIỂM THỬ PHẦN MỀM (TEST PLAN)
## XYZ Auto Service — Quality Assurance & Test Strategy

---

## 1. MỤC TIÊU VÀ PHẠM VI KIỂM THỬ

### 1.1. Mục tiêu kiểm thử
* Xác minh tính toàn vẹn của 9 chức năng thuộc phạm vi (F01 đến F09).
* Đảm bảo tính nhất quán của các quy tắc nghiệp vụ: giới hạn năng lực trạm, không nhận ngày Chủ nhật, bảo toàn snapshot giá dịch vụ, kiểm soát luồng trạng thái xe.
* Kiểm tra khả năng chịu tải và chống điều kiện tranh đua (Race Condition / Concurrency Collision).
* Kiểm tra tính bảo mật: xác thực phiên, phân quyền theo vai trò (RBAC), phòng chống CSRF.

### 1.2. Phạm vi kiểm thử
* **Trong phạm vi (In-Scope)**:
  * Kiểm thử đơn vị (Unit Tests) cho các Service logic, Validation constraints.
  * Kiểm thử tích hợp tự động (End-to-End API Integration Tests) qua HTTP Client thật và Random Web Server Port.
  * Kiểm thử giao diện và tương tác người dùng (UI Functional Testing).
  * Kiểm thử tải đồng thời (Concurrent Booking Latch Test).
* **Ngoài phạm vi (Out-of-Scope)**:
  * Kiểm thử thâm nhập an ninh mạng nâng cao (Penetration Testing).
  * Kiểm thử hiệu năng hàng triệu người dùng đồng thời (Stress Test quy mô lớn).

---

## 2. CHIẾN LƯỢC KIỂM THỬ (TEST STRATEGY)

Hệ thống áp dụng mô hình kim tự tháp kiểm thử (Test Pyramid) với trọng tâm là **Kiểm thử tích hợp đầu cuối (Automated Integration Testing)** mô phỏng hành vi người dùng thật thông qua `java.net.http.HttpClient` kết hợp quản lý Cookie (`CookieManager`) và CSRF Token.

```text
       ▲
      / \     E2E UI & Demo Workflow (Manual / Browser)
     /───\
    /     \   Automated HTTP Integration Tests (WorkshopFlowTests)
   /───────\
  /         \ Spring Boot Slice & JPA Tests (DemoApplicationTests)
 ─────────────
```

---

## 3. MA TRẬN KỊCH BẢN KIỂM THỬ CHI TIẾT (TEST CASES)

| Mã TestCase | Phân hệ / Nghiệp vụ | Mô tả kịch bản kiểm thử | Dữ liệu đầu vào | Kết quả kỳ vọng |
| :--- | :--- | :--- | :--- | :--- |
| **TC-SEC-01** | Bảo mật CSRF | Gửi yêu cầu `POST /api/register` mà không có header `X-CSRF-TOKEN` | JSON đăng ký, thiếu token | Trả về `403 Forbidden` |
| **TC-SEC-02** | Xác thực bắt buộc | Khách chưa đăng nhập gọi `POST /api/appointments` | Dữ liệu đặt lịch | Trả về `401 Unauthorized` |
| **TC-SEC-03** | Phân quyền vai trò | Tài khoản khách hàng (`CUSTOMER`) gọi API chỉnh sửa công suất `PUT /api/capacity` | `{"capacity": 5}` | Trả về `403 Forbidden` |
| **TC-CONC-01** | Tranh chấp đồng thời (Race Condition) | 2 khách hàng gửi yêu cầu đặt lịch cùng 1 khung giờ trong cùng 1 mili-giây khi trạm chỉ còn đúng 1 slot trống | Dùng `CountDownLatch(1)` kích hoạt 2 `CompletableFuture` song song | Chính xác 1 request thành công (`201 Created`), request còn lại bị từ chối an toàn (`409 Conflict`) |
| **TC-PRIV-01** | Quyền riêng tư dữ liệu | Khách hàng B truy vấn `/api/state` để xem thông tin biển số xe của Khách hàng A | Phiên làm việc của Khách B | Không xuất hiện biển số hay thông tin xe của Khách A |
| **TC-PRIV-02** | Quyền hạn Quản lý | Quản lý xưởng truy vấn `/api/state` | Phiên làm việc của Quản lý | Hiển thị đầy đủ tất cả lịch hẹn của mọi khách hàng trong xưởng |
| **TC-SCHED-01**| Đổi lịch hẹn | Khách hàng thực hiện đổi ngày và giờ hẹn của lịch đang `PENDING` | `PUT /api/appointments/{id}/schedule` | Trả về `200 OK`, lịch chuyển về `PENDING` chờ xưởng xếp lại khoang |
| **TC-SCHED-02**| Đặt lịch Chủ nhật | Khách hàng chọn ngày hẹn rơi vào Chủ nhật | Ngày hẹn là Sunday kế tiếp | Trả về `400 Bad Request` ("Xưởng không làm việc vào Chủ nhật") |
| **TC-SNAP-01** | Bảo toàn Snapshot Giá | Quản lý tăng giá gói dịch vụ `SRV-02` từ 1.150.000đ lên 2.500.000đ | `PUT /api/services/SRV-02` | Lịch hẹn đã đặt trước đó vẫn giữ nguyên đơn giá cũ 1.150.000đ |
| **TC-STAT-01** | Luồng trạng thái xe | Nhân viên duyệt lịch, gán khoang, phân công thợ và chuyển tiến độ | `PENDING` → `CONFIRMED` → `IN_PROGRESS` → `COMPLETED` | Các bước tuần tự đều trả về `200 OK` |
| **TC-STAT-02** | Chống hủy lịch sai quy định | Khách hàng gửi yêu cầu hủy lịch khi xe đã ở trạng thái `COMPLETED` | `PUT .../status` với `CANCELLED` | Trả về `409 Conflict` (Không được phép hủy) |

---

## 4. TIÊU CHÍ NGHIỆM THU (ACCEPTANCE CRITERIA)

1. Tỷ lệ vượt qua kiểm thử tự động đạt **100%** (`Tests run: 2, Failures: 0, Errors: 0, Skipped: 0`).
2. Thời gian chạy toàn bộ bộ kiểm thử tích hợp không vượt quá 2 phút trên máy tính tiêu chuẩn.
3. Ứng dụng khởi động sạch, không sinh ra lỗi cảnh báo ngoại lệ nghiêm trọng (Fatal Errors).

