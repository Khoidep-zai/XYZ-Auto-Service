# Spec Kit cho WorkshopOS

Spec Kit được cài từ `specify-cli` 1.3.0 với tích hợp Codex và script PowerShell.
Các skill nằm trong `.agents/skills/`; nguyên tắc dự án nằm trong
`.specify/memory/constitution.md`. Mẫu kế hoạch riêng của dự án nằm trong
`.specify/templates/overrides/plan-template.md` và được ưu tiên khi tạo kế hoạch.
Khi nâng cấp Spec Kit, đối chiếu mẫu riêng này với mẫu mới của công cụ.

## Khi nhận yêu cầu

- Với tính năng hoặc thay đổi có ảnh hưởng đáng kể, phân tích mục tiêu, người dùng,
  phạm vi, hành vi hiện tại, ràng buộc và tiêu chí nghiệm thu trước khi sửa mã.
  Dùng CodeGraph trước khi tìm hoặc đọc mã được lập chỉ mục.
- Ghi giả định có căn cứ. Chỉ hỏi người dùng khi quyết định còn thiếu có thể thay
  đổi đáng kể kết quả; tiếp tục phần việc độc lập trong khi chờ câu trả lời.
- Dùng chuỗi bên dưới khi phù hợp. Kiểm tra nhất quán giữa yêu cầu, kế hoạch và
  tác vụ trước khi triển khai; kiểm tra kết quả thực tế sau khi triển khai. Không
  bắt buộc người dùng gọi từng skill nếu yêu cầu đã cho phép thực hiện công việc.
- Với lỗi nhỏ hoặc thay đổi đơn giản, điều tra và kiểm chứng tương xứng; không tạo
  bộ tài liệu dài chỉ để tuân thủ hình thức.

## Thư mục tính năng và issue

Quy ước của dự án là `.scratch/<feature-slug>/`, không dùng `specs/` mặc định.
Khi chạy `$speckit-specify`, cung cấp rõ
`SPECIFY_FEATURE_DIRECTORY=.scratch/<feature-slug>` trong yêu cầu hoặc môi trường.
Skill tạo `spec.md` và ghi đường dẫn thực vào `.specify/feature.json`, để các bước
tiếp theo dùng cùng thư mục. Biến môi trường chỉ tồn tại trong phiên đã đặt; nếu
chạy script trực tiếp bằng PowerShell, ví dụ:

```powershell
$env:SPECIFY_FEATURE_DIRECTORY = '.scratch/nhac-lich-hen'
```

`plan.md`, `tasks.md` và các tài liệu hỗ trợ của tính năng cũng ở thư mục này.
Nếu cần tách issue, tạo `.scratch/<feature-slug>/issues/NN-ten-issue.md` theo
`docs/agents/issue-tracker.md`; `tasks.md` là danh sách công việc của Spec Kit,
không tự thay thế quy ước issue của dự án. Khi làm nhiều tính năng đồng thời,
đặt `SPECIFY_FEATURE_DIRECTORY` riêng cho từng phiên và cân nhắc
`SPECIFY_FEATURE_NO_PERSIST=1` để tránh ghi đè con trỏ tính năng chung.

## Các bước trong Codex

Gọi skill trong cuộc trò chuyện Codex, không chạy tên skill trong terminal:

1. `$speckit-specify` — viết yêu cầu, trường hợp sử dụng và tiêu chí nghiệm thu.
2. `$speckit-clarify` — làm rõ điểm thiếu có ảnh hưởng, khi cần.
3. `$speckit-plan` — phân tích mã hiện tại, phương án, rủi ro và cách kiểm chứng.
4. `$speckit-tasks` — chia việc theo thứ tự phụ thuộc, có điều kiện hoàn thành.
5. `$speckit-analyze` — đối chiếu spec, kế hoạch và tác vụ trước khi thực hiện.
6. `$speckit-implement` — thực hiện, kiểm thử và cập nhật kết quả.
7. `$speckit-converge` — đối chiếu sản phẩm với yêu cầu, ghi lại phần còn thiếu.

Ví dụ yêu cầu: `$speckit-specify SPECIFY_FEATURE_DIRECTORY=.scratch/nhac-lich-hen
Hãy đặc tả chức năng nhắc khách về lịch hẹn; phân tích vai trò, thời điểm nhắc,
trường hợp hủy lịch và các thông tin còn cần làm rõ.` Đây chỉ là ví dụ, chưa phải
tính năng được yêu cầu triển khai.

Nếu skill mới chưa xuất hiện trong danh sách Codex, mở lại phiên Codex tại thư mục
dự án. Nếu không gọi được skill theo tên, vẫn có thể yêu cầu Codex đọc hướng dẫn
trong `.agents/skills/speckit-<buoc>/SKILL.md` và làm theo.

CLI đã được chạy một lần qua `uvx`, chưa cài lệnh `specify` toàn máy. Để kiểm tra
tích hợp về sau khi có `uv` và quyền truy cập gói, chạy:

```powershell
uvx --from specify-cli==1.3.0 specify integration status
```

## Nguồn tham khảo

- [Spec Kit cho dự án hiện có](https://github.github.io/spec-kit/guides/existing-projects.html)
- [Tích hợp Codex](https://github.github.io/spec-kit/integrations/codex.html)
- [Các bước Spec-Driven Development](https://github.github.io/spec-kit/reference/agentic-sdd.html)
