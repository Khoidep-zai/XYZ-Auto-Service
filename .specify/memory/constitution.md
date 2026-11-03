<!--
Sync Impact Report
Version: 1.0.0 -> 1.1.0
Modified: III. Bảo mật và dữ liệu; IV. Chứng cứ trước khi báo hoàn thành;
Ràng buộc công nghệ. Added: database and IntelliJ verification requirements.
Removed: none. Deferred items: none.
-->
# Hiến chương dự án WorkshopOS

## Core Principles

### I. Yêu cầu rõ và có thể kiểm chứng

Với thay đổi nghiệp vụ hoặc kiến trúc đáng kể, xác định người dùng, phạm vi,
hành vi mong đợi, trường hợp biên và tiêu chí nghiệm thu trước khi triển khai.
Ghi các giả định; làm rõ những quyết định thiếu thông tin có ảnh hưởng lớn.
Các lỗi nhỏ được điều tra và kiểm chứng theo mức độ ảnh hưởng.

### II. Tôn trọng hệ thống và nghiệp vụ hiện có

Kiểm tra mã, thiết kế, tài liệu liên quan và tác động trước khi thay đổi. Giữ
khả năng sử dụng các luồng dịch vụ, lịch hẹn và vai trò hiện hữu trừ khi yêu cầu
thay đổi chúng. Khi quy tắc về công suất, trạng thái lịch, dữ liệu hoặc quyền
truy cập thay đổi, nêu rõ ảnh hưởng và cách xử lý dữ liệu hiện có.

### III. Bảo mật và dữ liệu

Xác thực và phân quyền ở phía máy chủ; bảo vệ dữ liệu người dùng và xử lý lỗi
mà không lộ thông tin nhạy cảm. Không đưa bí mật mới vào mã, đặc tả hay log.
Xem xét dữ liệu, tính đồng thời và khả năng khôi phục khi đề xuất thay đổi cơ sở
dữ liệu. Không dùng kết quả H2 làm bằng chứng duy nhất cho hành vi riêng của
PostgreSQL, đặc biệt với khóa và giao dịch.
Không xóa hoặc thay đổi dữ liệu hiện có khi chưa xác định cách bảo toàn và khôi
phục; đánh giá truy vấn và khóa khi thay đổi luồng đặt lịch đồng thời.

### IV. Chứng cứ trước khi báo hoàn thành

Xác định cách kiểm chứng phù hợp với từng thay đổi. Chạy kiểm thử hoặc kiểm tra
thủ công liên quan, nêu kết quả thực tế và các giới hạn chưa kiểm chứng. Khi thay
đổi luồng hoặc hợp đồng, kiểm tra cả trường hợp thành công và lỗi quan trọng.
Với thay đổi hiệu năng, ghi cách đo và điều kiện kiểm thử trước khi kết luận.

### V. Thiết kế đơn giản, có thể bảo trì

Ưu tiên cấu trúc và phụ thuộc đang dùng khi đáp ứng yêu cầu. Chia công việc theo
phụ thuộc thành các phần có thể kiểm chứng; chỉ thêm độ phức tạp khi giải thích
được lợi ích. Dùng CodeGraph trước khi tìm hoặc đọc mã được lập chỉ mục.

## Ràng buộc công nghệ

- Backend: Java 21, Spring Boot và Maven theo cấu hình thực tế trong `pom.xml`.
- Giao diện web tĩnh được Spring Boot phục vụ từ `src/main/resources/static/`.
- Theo lựa chọn của người dùng, giữ PostgreSQL cho profile PostgreSQL và H2
  in-memory cho profile phát triển; kiểm tra cấu hình thực tế trước khi lập kế hoạch.
- Giữ nguyên cấu hình IntelliJ và các lệnh chạy hiện có trừ khi nhiệm vụ yêu cầu
  thay đổi; không giả định cơ sở dữ liệu hoặc dịch vụ ngoài đang chạy.
- Khi thay đổi cách build hoặc chạy trong IntelliJ, kiểm tra JDK 21, cấu hình
  chạy, profile và đường dẫn dự án có dấu; báo rõ phần chưa kiểm chứng.

## Quy trình phát triển

- Lưu đặc tả, kế hoạch và tác vụ theo tính năng tại `.scratch/<feature-slug>/`;
  issue tách riêng theo `docs/agents/issue-tracker.md`.
- Với yêu cầu đáng kể, đối chiếu đặc tả, kế hoạch và tác vụ trước khi thực hiện;
  sau đó kiểm tra sản phẩm với tiêu chí nghiệm thu và báo cáo phần còn thiếu.
- Đọc `docs/agents/spec-kit.md` để biết cách dùng Spec Kit, giữ quy trình tương
  xứng với phạm vi và không tạo bước chờ phê duyệt ngoài yêu cầu hiện hành.
- Tài liệu và kết quả kiểm thử phải phản ánh trạng thái đã xác minh, không suy
  diễn rằng một môi trường khác cũng hoạt động.

## Governance

Các yêu cầu trực tiếp của người dùng và chỉ dẫn có thẩm quyền cao hơn được ưu
tiên khi có xung đột. Khi thay đổi hiến chương, ghi lý do, ngày và phiên bản;
kiểm tra tác động đến đặc tả và kế hoạch liên quan. Tăng MAJOR khi thay đổi
nguyên tắc không tương thích, MINOR khi bổ sung hoặc mở rộng đáng kể, PATCH cho
đính chính không thay đổi ý nghĩa.

**Version**: 1.1.0 | **Ratified**: 2026-11-04 | **Last Amended**: 2026-11-04
