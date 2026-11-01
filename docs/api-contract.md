# TÀI LIỆU HỢP ĐỒNG API (API CONTRACT SPECIFICATION)
## XYZ Auto Service RESTful Endpoints

---

## 1. NGUYÊN TẮC THIẾT KẾ CHUNG

* **Kiểu kiến trúc**: RESTful JSON API.
* **Định dạng dữ liệu**: `application/json;charset=UTF-8`.
* **Xác thực phiên**: Cookie-based Session (`JSESSIONID`).
* **Bảo vệ CSRF**: Mọi phương thức thay đổi trạng thái (`POST`, `PUT`, `DELETE`) bắt buộc phải gửi kèm header `X-CSRF-TOKEN` lấy từ `/api/csrf`.
* **Chuẩn mã trạng thái HTTP**:
  * `200 OK`: Yêu cầu thực thi thành công, trả về dữ liệu.
  * `201 Created`: Tạo tài nguyên thành công (lịch hẹn, tài khoản, dịch vụ).
  * `204 No Content`: Thao tác thành công, không có nội dung trả về (đăng xuất, duyệt lịch).
  * `400 Bad Request`: Dữ liệu đầu vào sai định dạng hoặc vi phạm quy tắc ràng buộc.
  * `401 Unauthorized`: Chưa đăng nhập hoặc phiên làm việc đã hết hạn.
  * `403 Forbidden`: Đã đăng nhập nhưng không đủ quyền hạn thực hiện.
  * `404 Not Found`: Không tìm thấy bản ghi yêu cầu.
  * `409 Conflict`: Vi phạm tính toàn vẹn (trùng số điện thoại, khung giờ đã đầy chỗ, trạng thái không cho phép).

---

## 2. DANH SÁCH CHI TIẾT CÁC ENDPOINT

### 2.1. Nhóm Xác thực & Trạng thái Hệ thống

#### `GET /api/csrf`
* **Mô tả**: Lấy CSRF token cho phiên làm việc hiện tại.
* **Quyền hạn**: Công khai (`permitAll`).
* **Phản hồi mẫu**:
```json
{
  "token": "d748f3e2-1a2b-4c5d-9e8f-0123456789ab",
  "headerName": "X-CSRF-TOKEN"
}
```

#### `GET /api/state`
* **Mô tả**: Lấy toàn bộ trạng thái đồng bộ xưởng cho phiên làm việc hiện tại (danh mục dịch vụ, danh sách lịch hẹn trong phạm vi quyền hạn, công suất trạm, số lượng đặt chỗ hiện tại).
* **Quyền hạn**: Công khai (`permitAll` - tự động lọc dữ liệu theo danh tính phiên).
* **Phản hồi mẫu**:
```json
{
  "currentRole": "CUSTOMER",
  "authenticated": true,
  "activeAccountId": "3",
  "accounts": [
    {
      "id": "3",
      "phone": "0988888888",
      "name": "Lê Khách Hàng",
      "role": "CUSTOMER",
      "avatar": "KH",
      "meta": "Khách hàng VIP"
    }
  ],
  "services": [
    {
      "id": "SRV-01",
      "code": "SRV-01",
      "name": "Bảo dưỡng định kỳ cấp 10.000km",
      "category": "Bảo dưỡng định kỳ",
      "duration": 60,
      "price": 1450000,
      "warranty": "6 tháng / 10.000km",
      "description": "Thay dầu máy; Vệ sinh lọc gió; Kiểm tra phanh; Đảo lốp; Quét lỗi OBD-II",
      "active": true,
      "checklist": ["Thay dầu máy", "Vệ sinh lọc gió", "Kiểm tra phanh", "Đảo lốp", "Quét lỗi OBD-II"]
    }
  ],
  "appointments": [
    {
      "id": "1",
      "customerName": "Lê Khách Hàng",
      "phone": "0988888888",
      "licensePlate": "51K-888.88",
      "carModel": "Mazda CX-5 2022",
      "date": "2026-11-02",
      "slot": "08:00",
      "status": "CONFIRMED",
      "totalDuration": 105,
      "totalPrice": 2600000,
      "bay": "Khoang 1 - Cầu nâng 2 trụ",
      "technician": "KTV. Hoàng Tuấn",
      "note": "Xe đi đường trường về cần kiểm tra kỹ phanh",
      "staffNote": "Đã tiếp nhận xe",
      "services": [
        {"serviceId": "SRV-01", "name": "Bảo dưỡng định kỳ cấp 10.000km", "price": 1450000, "duration": 60}
      ]
    }
  ],
  "maxCapacityPerSlot": 3,
  "slotCounts": {
    "2026-11-02|08:00": 1
  }
}
```

#### `POST /api/login`
* **Mô tả**: Đăng nhập bằng số điện thoại và mật khẩu.
* **Content-Type**: `application/x-www-form-urlencoded`.
* **Headers bắt buộc**: `X-CSRF-TOKEN: <token>`.
* **Tham số Body**:
  * `phone` (string, bắt buộc): Số điện thoại (ví dụ: `0901234567`).
  * `password` (string, bắt buộc): Mật khẩu (ví dụ: `12345678`).
* **Mã trả về**: `204 No Content` (Đăng nhập thành công) hoặc `401 Unauthorized`.

#### `POST /api/logout`
* **Mô tả**: Hủy phiên làm việc và xóa Cookie trên trình duyệt.
* **Headers bắt buộc**: `X-CSRF-TOKEN: <token>`.
* **Mã trả về**: `204 No Content`.

#### `POST /api/register`
* **Mô tả**: Khách hàng mới tự đăng ký tài khoản (tự động gán quyền `CUSTOMER`).
* **Body**:
```json
{
  "name": "Trần Văn A",
  "phone": "0918112233",
  "password": "password123",
  "meta": ""
}
```
* **Mã trả về**: `201 Created` hoặc `409 Conflict` (Số điện thoại đã tồn tại).

---

### 2.2. Nhóm Nghiệp vụ Đặt lịch & Điều phối

#### `POST /api/appointments`
* **Mô tả**: Đặt lịch hẹn mới trực tuyến hoặc tiếp nhận trực tiếp tại quầy (walk-in).
* **Quyền hạn**: Đã đăng nhập (`authenticated`). Nếu `walkIn = true` yêu cầu quyền `STAFF`/`MANAGER`.
* **Body**:
```json
{
  "customerName": "Lê Khách Hàng",
  "phone": "0988888888",
  "licensePlate": "51K-888.88",
  "carModel": "Mazda CX-5 2022",
  "date": "2026-11-03",
  "slot": "09:00",
  "serviceIds": ["SRV-01", "SRV-02"],
  "note": "Khách yêu cầu kiểm tra kỹ phanh đĩa",
  "walkIn": false,
  "bay": "",
  "technician": ""
}
```
* **Phản hồi**: `201 Created` kèm JSON: `{"id": 12}`.
* **Lỗi có thể xảy ra**:
  * `400 Bad Request`: Ngày chủ nhật, khung giờ quá khứ, danh sách dịch vụ rỗng.
  * `409 Conflict`: Khung giờ đã đạt sức chứa tối đa, hoặc biển số xe đã có lịch trong slot này.

#### `PUT /api/appointments/{id}/schedule`
* **Mô tả**: Khách hàng đổi ngày hẹn, khung giờ hoặc danh mục dịch vụ của lịch hẹn đã tạo.
* **Quyền hạn**: Chủ sở hữu lịch hẹn (`CUSTOMER`).
* **Body**:
```json
{
  "date": "2026-11-04",
  "slot": "10:00",
  "serviceIds": ["SRV-03"]
}
```
* **Mã trả về**: `200 OK` (Lịch tự động chuyển về `PENDING` để nhân viên phân bổ lại khoang).

#### `PUT /api/appointments/{id}/status`
* **Mô tả**: Cập nhật trạng thái lịch hẹn (Khách hủy lịch, hoặc Nhân viên duyệt/chuyển tiến độ).
* **Body (Trường hợp Khách hủy lịch)**:
```json
{
  "status": "CANCELLED",
  "reason": "Khách có việc bận đột xuất"
}
```
* **Body (Trường hợp Nhân viên duyệt lịch `CONFIRMED`)**:
```json
{
  "status": "CONFIRMED",
  "bay": "Khoang 01 - Cầu nâng 2 trụ",
  "technician": "KTV. Hoàng Tuấn",
  "reason": "Đã gọi điện xác nhận phụ tùng có sẵn"
}
```
* **Body (Trường hợp Nhân viên chuyển tiến độ `IN_PROGRESS` / `COMPLETED`)**:
```json
{
  "status": "IN_PROGRESS",
  "reason": ""
}
```
* **Mã trả về**: `200 OK`.

---

### 2.3. Nhóm Quản trị (Dành riêng cho Quản lý / MANAGER)

#### `POST /api/services`
* **Mô tả**: Tạo mới một gói dịch vụ vào danh mục xưởng.
* **Quyền hạn**: `@PreAuthorize("hasRole('MANAGER')")`.
* **Body**:
```json
{
  "code": "SRV-09",
  "name": "Phủ Ceramic Bảo vệ Sơn Xe Cao cấp",
  "category": "Chăm sóc & Làm đẹp xe",
  "duration": 180,
  "price": 4500000,
  "warranty": "24 tháng",
  "description": "Phủ 3 lớp Ceramic bảo vệ sơn bóng",
  "checklist": ["Rửa xe chi tiết", "Hiệu chỉnh bề mặt sơn", "Phủ lớp lót Ceramic", "Sấy đèn hồng ngoại"],
  "active": true
}
```
* **Mã trả về**: `201 Created`.

#### `PUT /api/services/{id}`
* **Mô tả**: Cập nhật thông tin hoặc trạng thái kích hoạt của gói dịch vụ.
* **Quyền hạn**: `@PreAuthorize("hasRole('MANAGER')")`.

#### `PUT /api/capacity`
* **Mô tả**: Điều chỉnh năng lực tiếp nhận tối đa của xưởng trên từng khung giờ.
* **Quyền hạn**: `@PreAuthorize("hasRole('MANAGER')")`.
* **Body**:
```json
{
  "capacity": 4
}
```
* **Ràng buộc**: `capacity` nằm trong khoảng từ `1` đến `5`.
* **Mã trả về**: `200 OK`.

#### `POST /api/accounts`
* **Mô tả**: Quản lý cấp tài khoản nội bộ (Nhân viên tiếp nhận hoặc Quản lý khác).
* **Quyền hạn**: `@PreAuthorize("hasRole('MANAGER')")`.
* **Body**:
```json
{
  "name": "Nguyễn Kỹ Thuật",
  "phone": "0933445566",
  "password": "password123",
  "role": "STAFF",
  "meta": "Kỹ thuật viên trưởng"
}
```
* **Mã trả về**: `201 Created`.

