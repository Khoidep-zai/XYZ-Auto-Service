# Feature Specification: Ổn định backend, dữ liệu và khởi chạy

**Feature Branch**: Không yêu cầu tạo nhánh

**Created**: 2026-11-04

**Status**: Draft

**Input**: Kiểm tra và tối ưu backend, cơ sở dữ liệu và khả năng chạy trong IntelliJ.

## User Scenarios & Testing

### User Story 1 - Bảo toàn luồng đặt lịch (Priority: P1)

Khách đặt, đổi hoặc hủy lịch; nhân viên xử lý lịch mà không mất dữ liệu hay vượt công suất.

**Why this priority**: Đây là nghiệp vụ chính và có nguy cơ xung đột dữ liệu.

**Independent Test**: Thực hiện đặt, đổi, hủy và chuyển trạng thái lịch với dữ liệu thử.

**Acceptance Scenarios**:

1. **Given** một khung giờ đã đủ công suất, **When** khách đặt thêm, **Then** hệ thống từ chối và giữ nguyên các lịch đã có.
2. **Given** các yêu cầu đặt đồng thời, **When** cùng tranh chấp chỗ trống, **Then** số lịch chiếm chỗ không vượt công suất.
3. **Given** người dùng không có quyền, **When** họ thay đổi lịch của người khác, **Then** yêu cầu bị từ chối.

### User Story 2 - Bảo vệ dữ liệu và xử lý lỗi (Priority: P1)

Người vận hành nhận lỗi dễ hiểu và dữ liệu thực tế được bảo toàn khi ứng dụng khởi động hoặc gặp lỗi.

**Why this priority**: Dữ liệu khách hàng và quyền truy cập cần được bảo vệ.

**Independent Test**: Khởi động lại với dữ liệu thử đã có, thử dữ liệu không hợp lệ và truy cập sai quyền.

**Acceptance Scenarios**:

1. **Given** dữ liệu có sẵn, **When** ứng dụng khởi động lại, **Then** dữ liệu không bị xóa hoặc ghi đè ngoài chủ ý.
2. **Given** đầu vào không hợp lệ, **When** gửi yêu cầu, **Then** người dùng nhận lỗi phù hợp mà không lộ thông tin nhạy cảm.

### User Story 3 - Khởi chạy và sử dụng ổn định (Priority: P2)

Người phát triển mở dự án trong IDE và người dùng sử dụng hệ thống với hiệu năng phù hợp.

**Why this priority**: Giảm lỗi cài đặt và tránh suy giảm hiệu năng khi dữ liệu tăng.

**Independent Test**: Theo hướng dẫn khởi chạy, kiểm tra các luồng chính và đo các trường hợp nghi ngờ chậm trước và sau thay đổi.

**Acceptance Scenarios**:

1. **Given** môi trường được cấu hình theo hướng dẫn, **When** người phát triển build và chạy, **Then** ứng dụng khởi động và trang chính phản hồi.
2. **Given** một điểm chậm được xác định, **When** thử lại cùng điều kiện sau tối ưu, **Then** kết quả đo được ghi nhận cùng giới hạn của phép đo.

### Edge Cases

- Cơ sở dữ liệu không sẵn sàng, thiếu cấu hình hoặc có dữ liệu đã tồn tại.
- Nhiều người đặt cùng lúc; hủy hoặc đổi lịch khi trạng thái đã thay đổi.
- Dự án nằm trong đường dẫn có dấu tiếng Việt; JDK hoặc profile không đúng.
- Dữ liệu thử tăng và các phép đo không đại diện cho dữ liệu thực tế.

## Requirements

### Functional Requirements

- **FR-001**: Rà soát và khắc phục các lỗi có thể tái hiện hoặc có bằng chứng trong luồng nghiệp vụ thuộc phạm vi.
- **FR-002**: Giữ nguyên quyền và quy tắc lịch hẹn hiện có trừ khi người dùng yêu cầu thay đổi.
- **FR-003**: Bảo toàn dữ liệu hiện hữu và nêu cách kiểm chứng, khôi phục nếu cần thay đổi cấu trúc dữ liệu.
- **FR-004**: Kiểm tra tính đồng thời và tính nhất quán của các thao tác ảnh hưởng công suất lịch.
- **FR-005**: Đánh giá hiệu năng dựa trên phép đo, phạm vi dữ liệu và điều kiện chạy được ghi lại.
- **FR-006**: Kiểm tra hướng dẫn và cấu hình khởi chạy, bao gồm đường dẫn tiếng Việt.
- **FR-007**: Ghi lại kết quả kiểm tra, vấn đề còn lại và điều kiện chưa thể xác minh.

### Key Entities

- **Lịch hẹn**: Khách, xe, thời gian, trạng thái và dịch vụ tại thời điểm đặt.
- **Tài khoản**: Danh tính, vai trò và quyền của người sử dụng.
- **Công suất xưởng**: Giới hạn số lịch chiếm chỗ trong một khung giờ.

## Success Criteria

### Measurable Outcomes

- **SC-001**: Tất cả kịch bản nghiệm thu P1 có kết quả kiểm tra được ghi lại; các kịch bản chưa chạy được có lý do rõ ràng.
- **SC-002**: Không có lịch thành công vượt công suất trong kịch bản tranh chấp đã kiểm tra.
- **SC-003**: Mỗi vấn đề hiệu năng được xử lý có số liệu trước và sau hoặc được đánh dấu chưa đo được.
- **SC-004**: Có kết quả kiểm tra khởi chạy và đường dẫn có dấu, hoặc ghi rõ bước và môi trường còn thiếu.

## Assumptions

- Giữ PostgreSQL cho môi trường tương ứng và H2 cho phát triển theo lựa chọn trước đó của người dùng.
- Giữ giao diện và hợp đồng hiện có; thay đổi tương thích được ưu tiên.
- Không suy ra dữ liệu thử phản ánh quy mô thực tế; không đặt mục tiêu tốc độ tùy ý khi chưa đo.
