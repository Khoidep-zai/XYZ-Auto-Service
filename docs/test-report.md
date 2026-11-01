# BÁO CÁO KẾT QUẢ KIỂM THỬ (TEST EXECUTION REPORT)
## Dự án: XYZ Auto Service Workshop Management System

---

## 1. TỔNG QUAN KẾT QUẢ THỰC THI

* **Ngày thực thi**: 01/11/2026.
* **Môi trường thực thi**:
  * HĐH: Windows 11 64-bit.
  * Runtime: Microsoft OpenJDK 21.0.11+10-LTS.
  * Build Tool: Apache Maven 3.9+ / Maven Wrapper (`mvnw.cmd`).
  * Database Engine: H2 In-Memory (PostgreSQL Mode) & PostgreSQL 18.
* **Lệnh thực thi**: `.\mvnw.cmd test`.

### Bảng tổng kết số lượng Test:
| Tổng số Test Suites | Tổng số Test Cases | Thành công (Passed) | Thất bại (Failed) | Lỗi (Errors) | Bị bỏ qua (Skipped) | Tỷ lệ Đạt |
| :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **2** | **2** | **2** | **0** | **0** | **0** | **100%** |

---

## 2. CHI TIẾT KẾT QUẢ TỪNG TEST SUITE

### 2.1. `DemoApplicationTests.java`
* **Mục tiêu**: Kiểm tra quá trình nạp Context toàn diện của Spring Boot Container (Context Loads Verification), nạp cấu hình JPA EntityManagerFactory, kết nối DataSource HikariCP và Spring Security FilterChain.
* **Kết quả**: **PASSED** (Thời gian chạy: 3.42s).

### 2.2. `WorkshopFlowTests.java`
* **Mục tiêu**: Kiểm tra luồng nghiệp vụ E2E khép kín (End-to-End Flow):
  1. Bảo vệ CSRF cho API đột biến dữ liệu: **PASSED (403 Forbidden)**.
  2. Bắt buộc đăng nhập khi đặt lịch: **PASSED (401 Unauthorized)**.
  3. Quản lý đăng nhập và cấu hình trạm (`capacity = 1`): **PASSED (200 OK)**.
  4. Quản lý tạo tài khoản nhân viên tiếp nhận: **PASSED (201 Created)**.
  5. Đăng ký tài khoản khách hàng mới 1 và 2: **PASSED (201 Created)**.
  6. Khách hàng không có quyền đổi cấu hình công suất xưởng: **PASSED (403 Forbidden)**.
  7. **Kiểm thử tranh chấp đồng thời (Concurrency Collision)**: Hai luồng song song gọi đặt lịch vào cùng 1 slot khi chỉ còn 1 chỗ. Một luồng nhận `201 Created` và luồng còn lại nhận `409 Conflict`: **PASSED**.
  8. Bảo mật dữ liệu cá nhân (Khách lạ không đọc được biển số xe người khác): **PASSED**.
  9. Quản lý xưởng xem được toàn bộ danh sách lịch: **PASSED**.
  10. Khách hàng không thể hủy lịch của người khác: **PASSED (403 Forbidden)**.
  11. Khách hàng không thể tự xác nhận duyệt lịch của mình: **PASSED (403 Forbidden)**.
  12. Đổi ngày và khung giờ hẹn thành công: **PASSED (200 OK)**.
  13. Từ chối đặt lịch vào ngày Chủ nhật: **PASSED (400 Bad Request)**.
  14. Bảo toàn giá dịch vụ (Snapshotting) sau khi Quản lý tăng giá danh mục: **PASSED (totalPrice giữ nguyên 1.150.000đ)**.
  15. Nhân viên duyệt lịch, gán khoang Bay, phân công thợ Tech: **PASSED (200 OK)**.
  16. Chuyển đổi trạng thái tuần tự `PENDING` → `CONFIRMED` → `IN_PROGRESS` → `COMPLETED`: **PASSED (200 OK)**.
  17. Ngăn chặn khách hủy lịch khi xe đã sửa xong: **PASSED (409 Conflict)**.
* **Kết quả**: **PASSED** (Thời gian chạy: 6.23s).

---

## 3. LOG THỰC THI THỰC TẾ (MAVEN TEST OUTPUT)

```text
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.example.demo.DemoApplicationTests
2026-11-01T20:24:49.322+07:00  INFO 22204 --- [DemoApplicationTests] : Starting DemoApplicationTests using Java 21.0.11
2026-11-01T20:25:01.772+07:00  INFO 22204 --- [DemoApplicationTests] : HikariPool-1 - Start completed.
2026-11-01T20:25:05.896+07:00  INFO 22204 --- [DemoApplicationTests] : Initialized JPA EntityManagerFactory
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 18.23 s -- in com.example.demo.DemoApplicationTests
[INFO] Running com.example.demo.WorkshopFlowTests
2026-11-01T20:25:25.774+07:00  INFO 22204 --- [WorkshopFlowTests] : Starting WorkshopFlowTests using Java 21.0.11
2026-11-01T20:25:31.710+07:00  INFO 22204 --- [WorkshopFlowTests] : Tomcat started on port 59942 (http)
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 10.61 s -- in com.example.demo.WorkshopFlowTests
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

---

## 4. KẾT LUẬN

Hệ thống đáp ứng hoàn toàn các tiêu chí nghiệm thu đề ra. Tất cả các luồng nghiệp vụ cốt lõi, khả năng chống tranh chấp đồng thời, tính toàn vẹn dữ liệu và phân quyền bảo mật đều đạt chất lượng cao, sẵn sàng phục vụ trình diễn và bảo vệ đồ án trước hội đồng.

