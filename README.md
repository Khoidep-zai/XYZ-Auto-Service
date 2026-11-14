# XYZ Auto Service — Source code (Frontend / Backend)

Source code tách rõ 2 thư mục nhưng vẫn là **một ứng dụng Spring Boot**: giao diện tĩnh được Maven sao chép từ `frontend/` sang `backend/target/classes/static/` khi chạy/build. Không cần `npm start` hay server frontend riêng.

## Cấu trúc

```text
XYZ_Auto_Service_Frontend_Backend/
├── frontend/
│   ├── index.html                  # Giao diện chính
│   ├── app.js                      # JavaScript gọi /api/**
│   ├── workshop.css                # CSS đã biên dịch, dùng trực tiếp
│   ├── tailwind.input.css          # Nguồn Tailwind (tùy chọn)
│   ├── tailwind.config.cjs         # Cấu hình Tailwind (tùy chọn)
│   └── vendor/                     # Font Awesome CSS và font
├── backend/
│   ├── pom.xml                     # Maven, Java 21, Spring Boot
│   ├── src/main/java/             # Backend, API, nghiệp vụ, phân quyền
│   ├── src/main/resources/         # Cấu hình H2 / PostgreSQL
│   ├── src/test/                   # Kiểm thử Java
│   ├── xyz_auto_service_postgresql.sql
│   ├── docker-compose.yml         # PostgreSQL
│   ├── mvnw, mvnw.cmd              # Maven Wrapper
│   ├── run-dev.cmd/.sh
│   └── run-postgres.cmd/.sh
└── README.md
```

## Cách chạy trên Windows

Yêu cầu Java 21. Cần có Maven hoặc Maven Wrapper tải được gói phụ thuộc lần đầu. Mở thư mục **backend** bằng IntelliJ IDEA hoặc VS Code.

**Chế độ demo nhanh (H2, không cần PostgreSQL):**

```bat
cd backend
run-dev.cmd
```

**Chế độ PostgreSQL theo yêu cầu đề bài:** cần cài Docker Desktop và bật Docker Engine.

```bat
cd backend
docker compose up -d
run-postgres.cmd
```

Mở http://localhost:8080 (hoặc địa chỉ máy chủ in ra sau khi khởi chạy). `docker compose` có thể xung đột cổng 5432 nếu máy đã có PostgreSQL đang chạy; khi đó chỉnh cấu hình cổng và `DB_URL` tương ứng.

Linux/Mac: `cd backend && sh run-dev.sh` (hoặc `docker compose up -d && sh run-postgres.sh`).

## Cách hoạt động của frontend đã tách

- Mã giao diện gốc nằm trong **frontend/**. Không sửa các tệp đã được sao chép trong `backend/target/` vì lần build tiếp theo sẽ ghi đè.
- `backend/pom.xml` dùng `maven-resources-plugin` sao chép `index.html`, `app.js`, `workshop.css` và `vendor/**` từ frontend vào `target/classes/static/` khi Maven thực hiện `process-resources`.
- Frontend vẫn sử dụng đường dẫn `/api/...` và cookie đăng nhập cùng origin của backend; không cần sửa fetch, không cần cấu hình CORS.
- Khi đổi HTML/JS/CSS, dừng và chạy lại Maven để sao chép phiên bản mới. Nếu chạy trực tiếp từ nút Run của IDE, **hãy thực hiện `mvn process-resources` trước** hoặc dùng lệnh Maven trong file `.cmd`.
- `tailwind.input.css` là nguồn, `workshop.css` đã là CSS sử dụng cho bản demo. Không bắt buộc cài Node/NPM.

## Kiểm thử

Trong thư mục backend:

```bat
mvnw.cmd test
```

Chưa có xác nhận chạy test trên máy của bạn; hãy lưu log kiểm thử để làm minh chứng báo cáo.

## Lưu ý

Đây là mã nguồn được **tổ chức lại thư mục** từ bản chỉnh sửa trước đó. Không thay đổi luồng API hay thiết kế database. Để triển khai thật, thay thông tin mật khẩu/cấu hình mặc định phù hợp môi trường, kiểm tra kỹ quyền truy cập và dữ liệu mẫu.
