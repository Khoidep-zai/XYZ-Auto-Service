# Kế hoạch triển khai XYZ Auto Service

## Phạm vi

Chuyển thiết kế `design/xyz_auto_service_workshop_os.html` từ bản mô phỏng localStorage thành ứng dụng đặt lịch và điều phối có dữ liệu bền vững, đăng nhập và phân quyền. Ba nhóm chức năng: khách hàng đặt/tra cứu/đổi/hủy lịch; nhân viên tiếp nhận và cập nhật tiến độ; quản lý dịch vụ, công suất, tài khoản và thống kê.

## Quyết định kỹ thuật

- Java 21, Spring Boot, Spring MVC, Spring Security, Spring Data JPA và MySQL; giữ WebFlux, DevTools, Lombok, MySQL Driver như yêu cầu. Khi đồng thời có MVC và WebFlux, Spring Boot sử dụng MVC.
- Giao diện sử dụng bố cục, tiếng Việt, màu sắc và các trạng thái từ mẫu; gọi API cùng nguồn gốc, không dùng localStorage cho dữ liệu nghiệp vụ.
- Tài khoản có mật khẩu băm và phiên đăng nhập; phân quyền CUSTOMER/STAFF/MANAGER được thực thi ở máy chủ. Khách chỉ xem và sửa lịch thuộc tài khoản của mình. Tạo quản lý đầu tiên bằng biến môi trường, không cài mật khẩu cố định.
- Tám khung tiếp nhận 08:00–11:00 và 13:00–16:00 từ thứ Hai đến thứ Bảy; mỗi lịch chiếm một chỗ trong khung bắt đầu như mẫu. Không nhận khung giờ đã qua; thời gian tính theo múi giờ Việt Nam. Đặt lịch, đổi lịch và thay đổi công suất được thực hiện trong giao dịch có khóa để tránh vượt sức chứa.
- Giá/thời lượng được chốt khi tạo hoặc đổi lịch; doanh thu là dự kiến, không phải thanh toán. Không suy diễn thêm chức năng thanh toán, gửi SMS hay quản lý phụ tùng.
- Dữ liệu dịch vụ mẫu được khởi tạo khi danh mục còn trống; thông tin kết nối MySQL lấy từ môi trường. H2 chỉ dùng cho kiểm thử.

## Công việc theo thứ tự

Các đầu việc và tiêu chí nghiệm thu nằm trong `.scratch/workshop-os/issues/`:

1. `01-foundation.md` — cấu hình chạy và cơ sở dữ liệu.
2. `02-authentication.md` — tài khoản, đăng nhập và phân quyền.
3. `03-catalog.md` — danh mục dịch vụ và cấu hình công suất.
4. `04-bookings.md` — đặt lịch và quy tắc điều phối.
5. `05-customer-ui.md` — kết nối giao diện khách hàng.
6. `06-staff-ui.md` — kết nối giao diện nhân viên.
7. `07-manager-ui.md` — kết nối giao diện quản lý.
8. `08-verification.md` — kiểm thử, tài liệu và kiểm tra luồng thực tế.

## Kiểm tra theo mốc

- [x] Nền tảng: ứng dụng và bộ kiểm thử khởi động, xác thực/chặn truy cập theo vai trò.
- [x] Luồng lịch: kiểm tra tạo, thay đổi, hủy, duyệt, tiến độ, giới hạn công suất và quyền sở hữu trên H2 và MySQL 8.
- [x] Giao diện: kiểm tra các màn hình chính, đặt/đổi lịch, duyệt lịch, đổi công suất và trạng thái dịch vụ; dữ liệu được giữ sau khi tải lại và khởi động lại ứng dụng.
- [x] Kiểm tra bố cục tại độ rộng 320, 768, 1024 và 1440 px; hướng dẫn chạy MySQL trong `README.md`.

## Giả định và rủi ro

- Mẫu chưa mô tả lịch làm việc ngoài tám khung, thời lượng chiếm nhiều khung, chính sách đặt cọc hay tích hợp bên ngoài; phạm vi hiện tại giữ quy tắc một chỗ/khung bắt đầu của mẫu.
- MySQL trên máy đang chạy nhưng tài khoản/mật khẩu chưa được cung cấp; hướng dẫn cấu hình và kiểm thử với một phiên MySQL riêng nếu môi trường cho phép.
- CSS và biểu tượng được phục vụ cục bộ; font từ Google Fonts có font dự phòng khi không có mạng.
