# PHIẾU XÁC ĐỊNH PHẠM VI DỰ ÁN (PROJECT SCOPE STATEMENT)

---

## 1. THÔNG TIN CHUNG DỰ ÁN
* **Tên dự án**: Hệ thống Quản lý Đặt lịch Bảo dưỡng & Sửa chữa Ô tô Trực tuyến (XYZ Auto Service Workshop Management System).
* **Môn học**: Quản lý Dự án Phần mềm (Mã học phần: 71ITSE41503).
* **Đơn vị thụ hưởng**: Trung tâm Dịch vụ Kỹ thuật Ô tô XYZ.
* **Nhà tài trợ (Project Sponsor)**: Ban Giám đốc Trung tâm Ô tô XYZ.
* **Thời gian thực hiện**: 03 tuần (tương đương 03 Sprint Scrum).
* **Quy mô nhân sự**: Nhóm 03 sinh viên chuyên ngành Kỹ thuật Phần mềm.
* **Ngân sách**: Dự án học thuật ứng dụng, ngân sách tài chính thương mại 0 VNĐ (sử dụng tối đa công nghệ mã nguồn mở, không phát sinh chi phí bản quyền dịch vụ đám mây bên thứ ba).

---

## 2. BỐI CẢNH VÀ MỤC TIÊU DỰ ÁN

### 2.1. Bối cảnh nghiệp vụ hiện tại (As-Is)
Trung tâm XYZ cung cấp dịch vụ bảo dưỡng định kỳ, thay dầu nhờn, kiểm tra hệ thống phanh, sửa chữa động cơ và phụ tùng gầm. Hiện tại việc điều phối hoạt động thủ công gây nhiều rủi ro:
1. **Thiếu tính chủ động cho khách hàng**: Khách hàng chỉ có thể gọi điện thoại hoặc đến trực tiếp, không biết trước khung giờ nào còn chỗ trống.
2. **Quá tải tiếp nhận thủ công**: Lễ tân ghi nhận lịch bằng sổ tay và file Excel phân tán, tốn nhiều nhân lực xác nhận và cập nhật.
3. **Nguy cơ dồn ứ tại xưởng (Overbooking)**: Khách dồn vào cùng một khung giờ vượt quá năng lực phục vụ (số khoang nâng và kỹ thuật viên), dẫn đến xe phải chờ lâu, giảm chất lượng phục vụ.
4. **Mất dấu tiến độ**: Khách hàng không có công cụ tự tra cứu trạng thái tiếp nhận và tiến độ sửa chữa của xe.
5. **Thiếu số liệu điều hành**: Ban quản lý không có báo cáo thống kê chính xác về tỷ lệ khách đến đúng hẹn, tỷ lệ hủy lịch, công suất khai thác khoang dịch vụ và doanh thu dự kiến.

### 2.2. Mục tiêu chiến lược của phần mềm (To-Be)
1. **Mục tiêu 1 (Khách hàng)**: Cho phép khách hàng tra cứu chi tiết danh mục dịch vụ kỹ thuật và tự thao tác đặt lịch trực tuyến mọi lúc mọi nơi.
2. **Mục tiêu 2 (Nhân viên tiếp nhận)**: Cung cấp giao diện nghiệp vụ cho nhân viên xưởng xem danh sách, xác nhận phân khoang/kỹ thuật viên, hoặc từ chối lịch hẹn nhanh chóng.
3. **Mục tiêu 3 (Kiểm soát năng lực xưởng)**: Tự động hóa kiểm tra năng lực phục vụ theo thời gian thực, ngăn chặn tuyệt đối tình trạng đặt lịch trùng biển số hoặc vượt quá công suất trạm.
4. **Mục tiêu 4 (Minh bạch tiến độ)**: Cung cấp tính năng theo dõi trạng thái lịch hẹn minh bạch cho khách hàng từ khi tiếp nhận đến khi bàn giao xe.
5. **Mục tiêu 5 (Quản trị vận hành)**: Cung cấp bảng điều khiển (Dashboard) thống kê lịch hẹn theo ngày, trạng thái xử lý, tỷ trọng dịch vụ và doanh thu dự kiến.
6. **Mục tiêu 6 (Tiến độ & Chất lượng)**: Bàn giao sản phẩm phần mềm hoàn chỉnh, chạy được ổn định trong 3 tuần, đáp ứng chuẩn kiểm thử tích hợp tự động 100%.

---

## 3. CÁC BÊN LIÊN QUAN (STAKEHOLDERS)

| Bên liên quan | Vai trò trong dự án | Mối quan tâm chính | Kỳ vọng sản phẩm |
| :--- | :--- | :--- | :--- |
| **Ban Giám đốc XYZ** | Nhà tài trợ, Chủ đầu tư | Tối ưu hóa năng lực trạm, giảm thời gian chết của khoang sửa chữa, báo cáo trực quan | Hệ thống vận hành ổn định, chi phí bảo trì thấp, giao diện quản lý chuyên nghiệp |
| **Quản lý Dự án (PM)** | Điều phối, kiểm soát | Tiến độ 3 Sprint, phạm vi chức năng, rủi ro kỹ thuật, chất lượng code | Bám sát kế hoạch WBS, kiểm soát thay đổi nghiêm ngặt, bàn giao đúng hạn |
| **Nhân viên Tiếp nhận** | Người dùng nghiệp vụ | Thao tác duyệt lịch nhanh, dễ nhìn lịch theo khung giờ, gán khoang dễ dàng | Giao diện điều phối trực quan, cập nhật trạng thái tức thời không cần F5 |
| **Khách hàng sở hữu xe** | Người dùng cuối | Đặt lịch nhanh gọn, biết trước giá/thời lượng, dễ dàng đổi/hủy lịch | Giao diện thân thiện responsive mobile/desktop, hiển thị rõ ràng slot trống |
| **Kỹ thuật viên xưởng** | Đơn vị thực thi | Lượng xe vào xưởng đều đặn, không bị dồn xe vượt công suất thiết kế | Lịch làm việc được điều phối nhịp nhàng theo năng lực trạm |

---

## 4. CHI TIẾT PHẠM VI DỰ ÁN (IN-SCOPE)

| Mã | Tên chức năng | Phân hệ người dùng | Mô tả chi tiết phạm vi thực hiện |
| :---: | :--- | :---: | :--- |
| **F01** | **Quản lý danh mục dịch vụ** | Quản lý xưởng (MANAGER) | Quản lý danh mục bảo dưỡng/sửa chữa: mã dịch vụ, tên gói, nhóm dịch vụ, thời lượng dự kiến (phút), giá niêm yết (VNĐ), chính sách bảo hành, checklist hạng mục kỹ thuật, trạng thái kích hoạt (Active/Inactive). |
| **F02** | **Tra cứu dịch vụ** | Khách hàng (PUBLIC / CUSTOMER) | Khách hàng duyệt danh mục dịch vụ theo nhóm (Bảo dưỡng định kỳ, Phanh & Gầm, Thay dầu, Động cơ, Điện & Điều hòa...), tìm kiếm nhanh, xem chi tiết thời lượng, chi phí và checklist. |
| **F03** | **Đặt lịch trực tuyến** | Khách hàng (CUSTOMER) | Khách hàng nhập thông tin xe (Biển số, Dòng xe), chọn ngày hẹn (từ thứ Hai đến thứ Bảy), chọn 1 trong 8 khung giờ (08:00–11:00, 13:00–16:00), chọn 1 hoặc nhiều dịch vụ cần thực hiện, ghi chú tình trạng xe. |
| **F04** | **Quản lý & Duyệt lịch hẹn** | Nhân viên xưởng (STAFF / MANAGER) | Xem danh sách lịch hẹn toàn xưởng; lọc theo ngày/trạng thái; tiếp nhận duyệt lịch (`CONFIRMED`) đồng thời gán Khoang dịch vụ và Kỹ thuật viên; từ chối lịch (`REJECTED`) kèm lý do; tiếp nhận khách vãng lai trực tiếp tại quầy (Walk-in). |
| **F05** | **Thay đổi và hủy lịch hẹn** | Khách hàng (CUSTOMER) | Khách hàng chủ động thay đổi ngày hẹn, khung giờ hoặc gói dịch vụ (chuyển trạng thái về `PENDING` để xưởng duyệt lại); hủy lịch hẹn (`CANCELLED`) khi lịch đang ở trạng thái `PENDING` hoặc `CONFIRMED`. |
| **F06** | **Theo dõi trạng thái lịch hẹn** | Khách hàng (CUSTOMER) | Xem danh sách lịch hẹn cá nhân; theo dõi trực quan vòng đời lịch hẹn qua 7 trạng thái chuẩn: `PENDING` (Chờ duyệt) → `CONFIRMED` (Đã duyệt) → `IN_PROGRESS` (Đang sửa) → `COMPLETED` (Hoàn thành) hoặc `CANCELLED`/`REJECTED`/`NO_SHOW`. |
| **F07** | **Quản lý năng lực phục vụ** | Hệ thống & Quản lý (SYSTEM / MANAGER) | Hệ thống tự động giới hạn số xe tối đa trên từng khung giờ (mặc định 3 xe/slot, Quản lý được phép tùy chỉnh từ 1–5). Sử dụng cơ chế khóa bi quan (Pessimistic Locking `SELECT ... FOR UPDATE`) chống xung đột đặt lịch đồng thời; ngăn chặn 1 biển số đặt 2 lần trong cùng slot. |
| **F08** | **Báo cáo thống kê quản trị** | Quản lý xưởng (MANAGER) | Bảng điều khiển (Dashboard) thống kê: tổng số lịch hẹn theo ngày, phân bổ theo trạng thái, tỷ lệ tiếp nhận/hủy, số lượt đặt và tổng doanh thu dự kiến theo từng gói dịch vụ. |
| **F09** | **Quản lý tài khoản & Phân quyền** | Tất cả người dùng (ALL ROLES) | Đăng ký tài khoản khách hàng mới; Đăng nhập bằng Số điện thoại + Mật khẩu mã hóa BCrypt; Quản lý phiên làm việc Session-Cookie bảo mật có CSRF Token; Phân quyền nghiêm ngặt 3 vai trò: Khách hàng (`CUSTOMER`), Lễ tân tiếp nhận (`STAFF`), Giám đốc/Quản lý (`MANAGER`). |

---

## 5. CÁC TÍNH NĂNG NGOÀI PHẠM VI (OUT-OF-SCOPE)

Để bảo đảm mục tiêu hoàn thành đồ án trong 3 tuần với 3 thành viên, các tính năng sau **không thuộc phạm vi triển khai của phiên bản này**:
1. **Thanh toán trực tuyến (Payment Gateway)**: Không tích hợp cổng VNPAY, MoMo, ZaloPay, thẻ tín dụng. Mọi thanh toán doanh thu trong hệ thống là giá trị ước tính nghiệp vụ.
2. **Gửi tin nhắn SMS / Zalo ZNS / Notification bên thứ ba**: Không phát sinh chi phí thuê Brandname SMS hay kết nối API viễn thông trả phí.
3. **Quản lý kho phụ tùng & Vật tư chi tiết**: Không quản lý tồn kho, số lô, hạn dùng hay nhập-xuất phụ tùng từng con ốc vít.
4. **Quản lý tiền lương và chấm công nhân sự**: Không tính KPI, lương thưởng hay quản lý ca kíp chuyên sâu của kỹ thuật viên.
5. **Tích hợp bảo hiểm xe cơ giới**: Không liên kết API giám định bồi thường với các công ty bảo hiểm (Bảo Việt, PVI, PTI...).
6. **Ứng dụng di động độc lập (Native Mobile App iOS/Android)**: Không phát triển app trên App Store / Google Play; thay vào đó sử dụng Web Responsive chuẩn HTML5/Tailwind tối ưu hoàn hảo trên trình duyệt di động.
7. **Trí tuệ nhân tạo (AI) dự báo nhu cầu bảo dưỡng**: Không triển khai mô hình học máy hay Deep Learning để tránh phình to phạm vi dự án học phần.

---

## 6. CÁC RÀNG BUỘC DỰ ÁN (PROJECT CONSTRAINTS)

* **Ràng buộc thời gian**: Đúng 03 tuần (3 Sprint x 1 tuần/Sprint).
* **Ràng buộc nhân lực**: Nhóm cố định gồm 3 sinh viên, phân chia vai trò rõ ràng:
  * Thành viên 1: Backend Architecture, Database PostgreSQL, Bảo mật Spring Security.
  * Thành viên 2: Frontend UI/UX, Single Page Interaction, Responsive Design, State Management.
  * Thành viên 3: Kiểm thử tự động (Automation Testing), Viết tài liệu kỹ thuật (SRS, Architecture, API Contract).
* **Ràng buộc công nghệ**:
  * Ngôn ngữ & Nền tảng: Java 21 LTS, Spring Boot 4.x.
  * Cơ sở dữ liệu: PostgreSQL 14+ (hỗ trợ UTF-8, chuẩn 3NF).
  * Kiểm thử: JUnit 5, Spring Boot Test, Testcontainers / Embedded In-Memory.
* **Ràng buộc chất lượng**:
  * Toàn bộ mã nguồn phải vượt qua `mvn test` với tỷ lệ đạt 100%.
  * Cấu hình IntelliJ IDEA hỗ trợ "1-Click Run" sẵn sàng khởi chạy FE + BE + DB ngay lập tức mà không gặp lỗi phụ thuộc môi trường.
* **Quy trình quản lý thay đổi (Change Management)**:
  * Mọi yêu cầu phát sinh thay đổi phạm vi phải được lập Phiếu yêu cầu thay đổi thiết kế (`docs/thay-doi-thiet-ke.md`), đánh giá tác động tới tiến độ 3 tuần trước khi Ban Giám đốc phê duyệt.

