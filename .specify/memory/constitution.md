<!--
Sync Impact Report
Version: 1.1.0 -> 1.2.0
Modified: Core Principles I-V broadened to include Scrum project governance, non-hallucination evidence rules, EVM calculation fidelity, and student roles.
Added: Principle VI. Quản trị Scrum & Không giả mạo dữ liệu (Zero Hallucination); Principle VII. Chuẩn mực Hồ sơ Quản trị Dự án & Đánh giá Giáo viên; Principle VIII. Quy trình Quản lý Thay đổi (Change Request).
Removed: none. Deferred items: none.
-->
# Hiến chương dự án WorkshopOS — Hệ thống Quản lý Đặt lịch Ô tô XYZ

## Core Principles

### I. Yêu cầu rõ và có thể kiểm chứng (Spec-Driven & Testable)

Với mọi thay đổi nghiệp vụ, mã nguồn hoặc kiến trúc, bắt buộc xác định người dùng (Persona), phạm vi (Scope), hành vi mong đợi, các trường hợp biên (Edge Cases), và tiêu chí nghiệm thu (Acceptance Criteria) trước khi triển khai.
Mọi User Story trong Product Backlog phải viết bằng **ngôn ngữ khách hàng dễ hiểu** ("Là [người dùng], tôi muốn [chức năng] để [lợi ích]"), không chứa thuật ngữ kỹ thuật sâu (như API, JWT, DB Migration). Thuật ngữ kỹ thuật chỉ nằm trong Technical Tasks và WBS.

### II. Tôn trọng hệ thống và nghiệp vụ hiện có

Kiểm tra mã, thiết kế, tài liệu liên quan và tác động trước khi thay đổi. Giữ khả năng sử dụng các luồng dịch vụ (F01–F09), lịch hẹn và vai trò hiện hữu trừ khi yêu cầu thay đổi chúng. Khi quy tắc về công suất (Capacity/Overbooking), trạng thái lịch (`pending`, `confirmed`, `rejected`, `cancelled`), dữ liệu hoặc quyền truy cập thay đổi, nêu rõ ảnh hưởng và cách xử lý dữ liệu hiện có.

### III. Bảo mật, phân quyền và dữ liệu (Security & Concurrency)

Xác thực và phân quyền nghiêm ngặt ở phía máy chủ (RBAC: Customer, Staff, Manager); bảo vệ dữ liệu người dùng và xử lý lỗi thân thiện mà không để lộ thông tin nhạy cảm. Không đưa bí mật mới vào mã, đặc tả hay log.
Bảo đảm tính toàn vẹn giao dịch khi xử lý đặt lịch đồng thời (tránh Overbooking). Không dùng kết quả H2 làm bằng chứng duy nhất cho hành vi riêng của PostgreSQL, đặc biệt với khóa và giao dịch (`SELECT FOR UPDATE` / pessimistic lock).

### IV. Chứng cứ trước khi báo hoàn thành (Evidence-Based Completion)

Xác định cách kiểm chứng phù hợp với từng thay đổi. Chạy kiểm thử hoặc kiểm tra thực tế, nêu kết quả thực tế và các giới hạn chưa kiểm chứng.
Khi hoàn thành một gói công việc hoặc PBI, chỉ được đánh dấu `Done` khi thỏa mãn đầy đủ **Definition of Done (DoD)** và có **Acceptance Evidence** cụ thể (mã commit, log test, ảnh chụp màn hình, tài liệu đã được review). Tuyệt đối không đánh dấu Done trên giả định.

### V. Thiết kế đơn giản, có thể bảo trì (KISS & Deep Modules)

Ưu tiên cấu trúc và phụ thuộc đang dùng khi đáp ứng yêu cầu. Chia công việc theo phụ thuộc thành các phần có thể kiểm chứng; chỉ thêm độ phức tạp khi giải thích được lợi ích. Khi cần tìm hiểu cấu trúc mã, dùng CodeGraph trước khi tìm hoặc đọc mã được lập chỉ mục.

### VI. Quản trị Scrum & Không giả mạo dữ liệu (Zero Hallucination)

1. **Chuẩn hóa thuật ngữ**: Sử dụng chính xác thuật ngữ **Sprint Goal** (mục tiêu Sprint) và **Milestone** (mốc kiểm soát dự án); không dùng thuật ngữ sai lệch. **Definition of Done (DoD)** được chia tách và thể hiện rõ theo từng Sprint cùng với Milestone tương ứng.
2. **Nguyên tắc dữ liệu thật**: Giờ công thực tế (Actual Hours), kết quả kiểm thử (Test Results), trạng thái hoàn thành (DoD), biểu đồ Burndown và các chỉ số EVM đều phải có nguồn gốc minh chứng thực tế. Tuyệt đối không giả định số liệu, không bịa lịch sử tiến độ. Nếu chưa có dữ liệu thật, ghi rõ trạng thái `CHƯA_ĐỦ_DỮ_LIỆU` hoặc `CẦN_XÁC_NHẬN`.
3. **Tính toán EVM minh bạch**:
   - `BAC` lấy từ tổng giờ công baseline của `WBS_Uoc_Luong_Phan_Cong_Tuan_1.xlsx`.
   - `PV` tính theo phương pháp 0/100 cho các gói có ngày kế hoạch `Planned_Finish <= Checkpoint`.
   - `EV` tính theo phương pháp 0/100 cho các gói thực sự thỏa mãn DoD nghiệm thu có bằng chứng `DoD_Accepted_Date <= Checkpoint`.
   - `AC` tính từ tổng `Actual_Hours` trong Timesheet thực tế của thành viên.
   - Chỉ rõ bảng nguồn và công thức cụ thể, tách biệt ví dụ minh họa khỏi kết quả thực tế.
4. **Biểu đồ Burndown**: Phải có dữ liệu theo dõi Remaining Hours theo từng ngày làm việc của Sprint, kèm đường Ideal và đường Actual, và phải có ảnh chụp màn hình thật của biểu đồ.

### VII. Chuẩn mực Hồ sơ Quản trị & Yêu cầu Giáo viên

1. **Sheet Trang bìa bắt buộc**: Tất cả các file Excel (`.xlsx`) trong dự án bắt buộc phải có sheet đầu tiên là `00_TrangBia_HuongDan`, được thiết kế trang nhã, trực quan, giải thích đầy đủ 10 mục A–J: Nhận diện, Mục đích, Vị trí quy trình, Đầu vào/Đầu ra, Cấu trúc từng sheet, Quy tắc màu sắc (ô nhập tay vàng nhạt, ô công thức xanh lá nhạt, ô liên kết ngoài xanh dương nhạt), Tiêu chuẩn chất lượng, Ma trận liên kết tài liệu, và Lịch sử phiên bản.
2. **Chuỗi định danh ổn định (Stable IDs)**: Tất cả tài liệu và mã nguồn phải liên kết chặt chẽ qua chuỗi ID: `F_ID -> PBI_ID -> WBS_ID -> TASK_ID -> TEST_ID -> CR_ID -> EVIDENCE_ID`.
3. **Project Charter chuyên sâu**: Project Charter phải được viết chi tiết, mạch lạc, dễ hiểu với 12 phần toàn diện: Giới thiệu & Lý do đầu tư, Business Case, Mục tiêu SMART, Phạm vi F01-F09, Ngoài phạm vi, Stakeholders, Vai trò 3 thành viên (Khôi, Khoa, Dũng), Kế hoạch 3 Sprint, Ngân sách/Giờ công (147h baseline + 33h buffer = 180h capacity), Ràng buộc & Giả định, Rủi ro, và Tiêu chí nghiệm thu/bàn giao.

### VIII. Quy trình Quản lý Thay đổi (Change Request Protocol)

Mọi yêu cầu thay đổi phát sinh trong Sprint (điển hình: Ban giám đốc đề xuất bổ sung tính năng SMS nhắc lịch trong Sprint 2) phải được ghi nhận và phân tích đánh giá đầy đủ qua Phiếu yêu cầu thay đổi (`CR-SMS-01`) theo 6 nội dung cốt lõi:
1. Phân tích lý do và lợi ích nghiệp vụ kỳ vọng.
2. Đánh giá ảnh hưởng toàn diện đến 5 yếu tố: Phạm vi (Scope), Nỗ lực (Effort: 24-32 giờ), Tiến độ (Schedule: S2 chỉ còn 3h buffer, có nguy cơ trễ Sprint Goal), Chi phí (Cost: cước SMS/nhà mạng), và Chất lượng (Quality: rủi ro hồi quy, test cases mới).
3. Xác định danh mục công việc mới phát sinh (WBS & Technical tasks).
4. Đánh giá tính khả thi trong quỹ thời gian còn lại của Sprint.
5. Đề xuất quyết định có lập luận vững chắc (Đề xuất: `DEFER` sang giai đoạn sau để bảo vệ Sprint Goal và chất lượng bàn giao MVP cốt lõi).
6. Trình bày quy trình 7 bước ghi nhận, phân tích, thẩm định và phê duyệt thay đổi trong môi trường Scrum.

## Ràng buộc công nghệ

- Backend: Java 21, Spring Boot 3.x, Maven theo `pom.xml`.
- Cơ sở dữ liệu: PostgreSQL cho production/test tích hợp, H2 in-memory cho dev profile. Script migrations và seed data tại `xyz_auto_service_postgresql.sql`.
- Giao diện: Web giao diện HTML5/CSS/JavaScript + Bootstrap, phục vụ tĩnh từ Spring Boot.
- Công cụ quản lý: Trello / Azure DevOps cho bảng Kanban/Scrum; Excel và Word cho hồ sơ quản trị nộp đồ án; Figma cho thiết kế UI.
- Năng lực nhân sự: Nhóm 3 sinh viên, 20 giờ/người/tuần = 60h/Sprint, tổng 180 giờ cho 3 Sprint.

## Trách nhiệm thành viên

- **Nguyễn Bá Anh Khôi (2374802010247)**: Scrum Master kiêm Backend Developer (Quản trị tiến độ, điều phối Scrum, gỡ trở ngại, thiết kế kiến trúc, API, Authentication, logic đặt lịch, EVM và tích hợp hệ thống).
- **Trần Văn Khoa (2374802013414)**: Product Owner kiêm Frontend Developer (Xác định Product Goal, quản lý Product Backlog, User Stories dễ hiểu, thiết kế Figma, lập trình giao diện người dùng, thẩm định Change Request).
- **Nguyễn Tiến Dũng (2274802010102)**: Developer kiêm Quality & Database Specialist (Thiết kế cơ sở dữ liệu PostgreSQL, migration/seed data, kiểm thử tích hợp, Quality Plan, Test Cases, Defect Log, đảm bảo DoD).

## Governance

Hiến chương này là văn bản quy chuẩn cốt lõi cao nhất định hướng mọi hành vi của nhóm dự án và AI Agent.
Mọi cập nhật phải ghi rõ lý do, ngày ban hành và số hiệu phiên bản theo Semantic Versioning:
- MAJOR: Thay đổi căn bản các nguyên tắc cốt lõi hoặc định hướng công nghệ.
- MINOR: Bổ sung nguyên tắc mới, mở rộng quy chuẩn quản trị hoặc tiêu chí đánh giá.
- PATCH: Làm rõ câu chữ, chỉnh sửa lỗi chính tả, cải thiện định dạng.

**Version**: 1.2.0 | **Ratified**: 2026-10-23 | **Last Amended**: 2026-11-07
