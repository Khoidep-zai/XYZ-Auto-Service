# ĐẶC TẢ YÊU CẦU PHẦN MỀM (SOFTWARE REQUIREMENTS SPECIFICATION - SRS)
## Hệ thống Quản lý Đặt lịch Dịch vụ Ô tô XYZ (XYZ Auto Service)

---

## 1. GIỚI THIỆU

### 1.1. Mục đích tài liệu
Tài liệu này đặc tả chi tiết các yêu cầu chức năng (Functional Requirements), yêu cầu phi chức năng (Non-Functional Requirements), các quy tắc nghiệp vụ (Business Rules), mô hình dữ liệu và các ca sử dụng (Use Cases) cho hệ thống XYZ Auto Service.

### 1.2. Phạm vi sản phẩm
XYZ Auto Service là ứng dụng web quản lý đặt lịch bảo dưỡng, điều phối công suất khoang dịch vụ và theo dõi tiến độ sửa chữa xe ô tô trực tuyến, phục vụ nhu cầu tương tác hai chiều giữa Khách hàng sở hữu xe và Đội ngũ vận hành xưởng dịch vụ.

### 1.3. Định nghĩa và Thuật ngữ viết tắt
* **SRS**: Software Requirements Specification.
* **Slot**: Khung giờ tiếp nhận xe (thời lượng 60 phút mỗi slot).
* **Bay**: Khoang sửa chữa / Cầu nâng tại xưởng (ví dụ: Khoang 1, Khoang 2...).
* **Pessimistic Locking**: Cơ chế khóa bi quan cấp hàng trong cơ sở dữ liệu (`SELECT ... FOR UPDATE`) để ngăn ngừa tranh chấp dữ liệu khi nhiều người truy cập đồng thời.
* **CSRF**: Cross-Site Request Forgery.
* **Role**: Vai trò người dùng (`CUSTOMER`, `STAFF`, `MANAGER`).

---

## 2. MÔ TẢ TỔNG QUAN

### 2.1. Phân loại người dùng và Vai trò (Actors)
1. **Khách hàng vãng lai (Guest / Anonymous User)**:
   * Tra cứu danh mục dịch vụ, xem thời lượng, bảng giá và chính sách bảo hành.
   * Đăng ký tài khoản khách hàng mới hoặc đăng nhập.
2. **Khách hàng đã đăng nhập (`CUSTOMER`)**:
   * Đặt lịch hẹn trực tuyến (chọn xe, ngày, slot, danh sách dịch vụ).
   * Thay đổi thời gian hoặc dịch vụ của lịch hẹn đã đặt.
   * Hủy lịch hẹn khi xe chưa vào quá trình sửa chữa.
   * Tra cứu trạng thái và lịch sử bảo dưỡng của cá nhân.
3. **Nhân viên tiếp nhận / Lễ tân xưởng (`STAFF`)**:
   * Quản lý danh sách lịch hẹn toàn xưởng theo ngày.
   * Tiếp nhận duyệt lịch (`CONFIRMED`), gán khoang tiếp nhận (Bay) và kỹ thuật viên (Technician).
   * Cập nhật tiến độ xe: Bắt đầu sửa (`IN_PROGRESS`), Hoàn thành (`COMPLETED`), Khách không đến (`NO_SHOW`).
   * Tiếp nhận và tạo lịch trực tiếp tại quầy cho khách vãng lai (Walk-in booking).
4. **Quản lý / Giám đốc xưởng (`MANAGER`)**:
   * Toàn quyền như nhân viên tiếp nhận.
   * Quản lý danh mục dịch vụ (Thêm, sửa, bật/tắt kích hoạt).
   * Cấu hình năng lực phục vụ tối đa của xưởng (`capacity` từ 1 đến 5 xe/slot).
   * Tạo và phân quyền tài khoản nội bộ (Nhân viên / Quản lý).
   * Xem Bảng điều khiển thống kê tổng quan và doanh thu dự kiến.

---

## 3. CÁC QUY TẮC NGHIỆP VỤ CỐT LÕI (BUSINESS RULES)

* **BR-01 (Thời gian biểu làm việc)**:
  * Xưởng chỉ phục vụ từ Thứ Hai đến Thứ Bảy. **Chủ nhật xưởng nghỉ hoàn toàn**, hệ thống từ chối mọi yêu cầu đặt lịch vào Chủ nhật.
  * Mỗi ngày làm việc gồm đúng 08 khung giờ (Slot):
    * Buổi sáng: `08:00`, `09:00`, `10:00`, `11:00`.
    * Buổi chiều: `13:00`, `14:00`, `15:00`, `16:00`.
* **BR-02 (Thời gian hợp lệ)**:
  * Không cho phép đặt lịch trong quá khứ.
  * Nếu đặt lịch cho ngày hôm nay, chỉ được chọn các khung giờ có thời gian bắt đầu sau thời điểm hiện tại theo giờ Việt Nam (`Asia/Ho_Chi_Minh`).
* **BR-03 (Kiểm soát năng lực trạm - Capacity Control)**:
  * Tổng số lượng xe tiếp nhận hợp lệ (`PENDING`, `CONFIRMED`, `IN_PROGRESS`, `COMPLETED`) trong một khung giờ không được vượt quá chỉ số `capacity` (mặc định 3 xe/slot).
  * Các lịch có trạng thái `CANCELLED`, `REJECTED`, `NO_SHOW` sẽ giải phóng chỗ trong khung giờ đó ngay lập tức.
* **BR-04 (Chống trùng lịch xe)**:
  * Trong cùng một ngày và cùng một khung giờ, một biển số xe chỉ được phép có duy nhất 01 lịch hẹn hiệu lực.
* **BR-05 (Điều kiện thay đổi và hủy lịch)**:
  * Khách hàng chỉ được phép đổi ngày/giờ hoặc hủy lịch khi lịch hẹn đang ở trạng thái `PENDING` (Chờ duyệt) hoặc `CONFIRMED` (Đã duyệt).
  * Khi xe đã chuyển sang `IN_PROGRESS` (Đang sửa) hoặc `COMPLETED` (Hoàn thành), khách hàng không được phép tự ý thay đổi hoặc hủy.
* **BR-06 (Bảo toàn giá dịch vụ - Price Snapshotting)**:
  * Khi lịch hẹn được tạo, giá và tên dịch vụ tại thời điểm đó được lưu bản chụp (Snapshot) gắn liền với lịch hẹn. Việc Quản lý thay đổi giá dịch vụ sau này không làm thay đổi chi phí của các lịch hẹn đã chốt trong quá khứ.
* **BR-07 (Quyền riêng tư dữ liệu)**:
  * Khách hàng chỉ nhìn thấy lịch hẹn thuộc sở hữu tài khoản của mình.
  * Nhân viên và Quản lý xem được toàn bộ danh sách điều phối của xưởng.

---

## 4. ĐẶC TẢ YÊU CẦU CHỨC NĂNG (FUNCTIONAL REQUIREMENTS)

### 4.1. Phân hệ Dịch vụ & Tra cứu (F01, F02)
* **REQ-F01-1**: Hệ thống phải cho phép Quản lý tạo mới gói dịch vụ gồm: Mã gói (ví dụ `SRV-09`), Tên gói, Nhóm danh mục, Thời lượng (phút), Đơn giá (VNĐ), Thời hạn bảo hành, Checklist các bước thực hiện.
* **REQ-F01-2**: Cho phép Quản lý cập nhật thông tin và bật/tắt trạng thái kinh doanh (`active = true/false`) của dịch vụ.
* **REQ-F02-1**: Cho phép mọi người dùng xem danh mục dịch vụ đang kích hoạt, phân loại trực quan theo thẻ: *Bảo dưỡng định kỳ*, *Thay dầu & Chất lỏng*, *Hệ thống phanh & Gầm*, *Sửa chữa động cơ*, *Điện & Điều hòa*, *Kiểm định & An toàn*.
* **REQ-F02-2**: Hiển thị pop-up chi tiết nội dung checklist kỹ thuật khi người dùng chọn vào dịch vụ.

### 4.2. Phân hệ Đặt lịch & Điều phối (F03, F04, F05, F06, F07)
* **REQ-F03-1**: Người dùng phải đăng nhập tài khoản khách hàng để thực hiện đặt lịch hẹn trực tuyến.
* **REQ-F03-2**: Form đặt lịch yêu cầu: Họ tên khách, Số điện thoại, Biển số xe, Hãng/Dòng xe, Ngày hẹn, Khung giờ, và chọn ít nhất 01 gói dịch vụ.
* **REQ-F03-3**: Hệ thống tự động tính tổng thời gian dự kiến (phút) và tổng chi phí ước tính (VNĐ).
* **REQ-F04-1**: Khi khách hàng đặt lịch thành công, lịch được tạo với trạng thái `PENDING`.
* **REQ-F04-2**: Nhân viên xưởng có quyền duyệt lịch (`CONFIRMED`), bắt buộc phải chọn Khoang dịch vụ (Khoang 1 - 4) và Kỹ thuật viên phụ trách.
* **REQ-F04-3**: Nhân viên có quyền từ chối lịch (`REJECTED`) và nhập lý do gửi đến khách hàng.
* **REQ-F04-4**: Nhân viên có thể tạo lịch tiếp nhận trực tiếp tại xưởng (Walk-in), tự động gán trạng thái `CONFIRMED`.
* **REQ-F05-1**: Cho phép khách hàng chọn lịch của mình để đổi ngày/khung giờ/danh sách dịch vụ. Khi đổi thành công, lịch quay về trạng thái `PENDING` để nhân viên phân bổ lại khoang.
* **REQ-F05-2**: Cho phép khách hàng hủy lịch (`CANCELLED`) kèm lý do hủy.
* **REQ-F06-1**: Giao diện cung cấp thanh tiến trình trạng thái trực quan:
  $$\text{PENDING} \longrightarrow \text{CONFIRMED} \longrightarrow \text{IN\_PROGRESS} \longrightarrow \text{COMPLETED}$$
* **REQ-F07-1**: Hệ thống thực hiện kiểm tra công suất trạm trước khi lưu lịch. Nếu khung giờ đã đạt ngưỡng `capacity`, trả về mã lỗi HTTP 409 Conflict với thông báo rõ ràng cho người dùng.
* **REQ-F07-2**: Sử dụng khóa bi quan cấp bản ghi trên CSDL để loại bỏ triệt để lỗi điều kiện tranh đua (Race Condition) khi 2 khách hàng cùng đặt vị trí cuối cùng trong cùng 1 mili-giây.

### 4.3. Phân hệ Thống kê Quản trị (F08)
* **REQ-F08-1**: Cung cấp thẻ đo lường (KPI Cards): Tổng số lịch hẹn, Tỷ lệ duyệt thành công, Số xe đang trong xưởng, Doanh thu dự kiến.
* **REQ-F08-2**: Biểu đồ phân bổ tỷ trọng đặt lịch theo từng danh mục dịch vụ.
* **REQ-F08-3**: Bảng phân tích chi tiết doanh thu ước tính và số lượt tiếp nhận theo từng gói dịch vụ kỹ thuật.

### 4.4. Phân hệ Xác thực & Phân quyền (F09)
* **REQ-F09-1**: Cho phép khách hàng tự đăng ký tài khoản với Số điện thoại (chuẩn 10-11 số), Họ tên, và Mật khẩu (tối thiểu 8 ký tự).
* **REQ-F09-2**: Đăng nhập bằng số điện thoại và mật khẩu. Sử dụng thuật toán BCrypt kiểm tra mật khẩu.
* **REQ-F09-3**: Quản lý phiên bằng Session Cookie bảo mật (`HttpOnly`, `SameSite=Lax`).
* **REQ-F09-4**: Mọi thao tác thay đổi dữ liệu (POST/PUT/DELETE) bắt buộc phải có CSRF Token hợp lệ gửi qua header `X-CSRF-TOKEN`.

---

## 5. YÊU CẦU PHI CHỨC NĂNG (NON-FUNCTIONAL REQUIREMENTS)

1. **Hiệu năng (Performance)**:
   * Thời gian phản hồi của API truy vấn trạng thái xưởng (`GET /api/state`) không quá 200ms trong điều kiện mạng nội bộ.
   * Thời gian thực thi giao dịch đặt lịch có khóa bi quan không quá 500ms.
2. **Độ tin cậy & Toàn vẹn dữ liệu (Reliability & ACID)**:
   * 100% các thao tác đặt lịch, đổi lịch, duyệt lịch được bọc trong `@Transactional`.
   * CSDL thiết kế chuẩn 3NF, ràng buộc khóa ngoại (Foreign Key) chặt chẽ, hỗ trợ UTF-8 toàn diện cho tiếng Việt có dấu.
3. **Bảo mật (Security)**:
   * Mật khẩu không bao giờ lưu dưới dạng văn bản thô (Plaintext).
   * Phân quyền dựa trên vai trò ở mức phương thức với `@PreAuthorize("hasRole('MANAGER')")`.
   * Lọc và vô hiệu hóa XSS trên giao diện (Escape HTML đầu vào).
4. **Khả năng tương thích & Triển khai (Portability & Usability)**:
   * Chạy mượt mà trên JDK 21 LTS và Spring Boot 4.x.
   * Tương thích hoàn hảo với IntelliJ IDEA, hỗ trợ "1-Click Run" không cần thiết lập môi trường phức tạp.
   * Giao diện Responsive hoạt động tối ưu trên máy tính bảng, điện thoại di động và máy tính để bàn.

