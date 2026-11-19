# HƯỚNG DẪN TRIỂN KHAI VÀ VẬN HÀNH TOÀN DIỆN (INTERNET DEPLOYMENT GUIDE)
## Hệ Thống Đặt Lịch Bảo Dưỡng & Sửa Chữa Ô Tô (XYZ Auto Service — WorkshopOS)

Tài liệu này hướng dẫn chi tiết từng bước (step-by-step) để đưa toàn bộ hệ thống **XYZ Auto Service** lên môi trường Internet thực tế theo kiến trúc tách biệt hiện đại (Decoupled Cloud Architecture) với chi phí **0 đồng** (sử dụng hoàn toàn các gói Miễn phí / Free Tier từ các nhà cung cấp đám mây uy tín).

---

## 1. TỔNG QUAN KIẾN TRÚC ĐÁM MÂY (ARCHITECTURE OVERVIEW)

Hệ thống được thiết kế theo mô hình 3 lớp phân tán (3-Tier Cloud Architecture), đảm bảo tính sẵn sàng cao, bảo mật chặt chẽ và tối ưu tốc độ truy cập:

```mermaid
flowchart TD
    subgraph Client["Trình Duyệt Khách Hàng / Nhân Viên"]
        User["Người dùng (Web Desktop / Mobile)"]
    end

    subgraph CDN["Tầng 1: Netlify Edge CDN (Frontend)"]
        NetlifyStatic["Static Assets (HTML5, Vanilla JS, CSS3, FontAwesome)"]
        NetlifyProxy["Netlify API Rewrite Proxy (/api/* -> Backend)"]
    end

    subgraph CloudApp["Tầng 2: Cloud Container Runtime (Backend)"]
        Render["Render / Railway / Docker Web Service\nSpring Boot 4 / Java 21 LTS\nSpring Security + HikariCP"]
    end

    subgraph CloudDB["Tầng 3: Cloud Managed Database"]
        Neon["Neon Serverless PostgreSQL / Supabase\nPostgreSQL 16 (SSL Enforced)"]
    end

    User -->|"1. Tải giao diện tĩnh (HTTPS: *.netlify.app)"| NetlifyStatic
    User -->|"2. Gửi API call: /api/* (Same-Origin)"| NetlifyProxy
    NetlifyProxy -->|"3. Reverse Proxy chuyển tiếp HTTP (Internal 200 Rewrite)"| Render
    Render -->|"4. Truy vấn dữ liệu qua JDBC SSL Pool"| Neon

    style Client fill:#e1f5fe,stroke:#0288d1,stroke-width:2px;
    style CDN fill:#e8f5e9,stroke:#388e3c,stroke-width:2px;
    style CloudApp fill:#fff3e0,stroke:#f57c00,stroke-width:2px;
    style CloudDB fill:#f3e5f5,stroke:#7b1fa2,stroke-width:2px;
```

### Các Thành Phần Kiến Trúc:

| Thành phần | Nền tảng đề xuất | Vai trò & Đặc điểm kỹ thuật |
| :--- | :--- | :--- |
| **Frontend** | [Netlify](https://www.netlify.com/) (Free Tier) | Lưu trữ và phân phối Single Page Application (SPA), chứng chỉ SSL/TLS tự động, cấu hình reverse proxy `[[redirects]]` loại bỏ hoàn toàn vấn đề CORS và cookie bên thứ ba. |
| **Backend** | [Render](https://render.com/) / [Railway](https://railway.app/) | Chạy container Java 21 Spring Boot độc lập qua [`Dockerfile`](file:///c:/O%20E/Qu%E1%BA%A3n%20l%C3%BD%20d%E1%BB%B1%20%C3%A1n%20ph%E1%BA%A7n%20m%E1%BB%81m/%C4%90%E1%BB%93%20%C3%A1n%20m%C3%B4n%20h%E1%BB%8Dc/demo/Dockerfile) hoặc [`render.yaml`](file:///c:/O%20E/Qu%E1%BA%A3n%20l%C3%BD%20d%E1%BB%B1%20%C3%A1n%20ph%E1%BA%A7n%20m%E1%BB%81m/%C4%90%E1%BB%93%20%C3%A1n%20m%C3%B4n%20h%E1%BB%8Dc/demo/render.yaml), cơ chế auto-scale, quản lý biến môi trường an toàn. |
| **Database** | [Neon](https://neon.tech/) / [Supabase](https://supabase.com/) | Cơ sở dữ liệu Cloud PostgreSQL 16 serverless, hỗ trợ SSL bắt buộc (`sslmode=require`), sao lưu tự động và bảng điều khiển SQL Web Console. |

---

## 2. GIAI ĐOẠN 1: KHỞI TẠO CƠ SỞ DỮ LIỆU CLOUD (CLOUD DATABASE)

> [!IMPORTANT]
> Cơ sở dữ liệu cần được triển khai và nạp cấu trúc dữ liệu trước tiên để cung cấp chuỗi kết nối cho Backend.

Dự án cung cấp sẵn kịch bản SQL chuẩn hóa cho môi trường Cloud: [`Data/cloud_postgres_init.sql`](file:///c:/O%20E/Qu%E1%BA%A3n%20l%C3%BD%20d%E1%BB%B1%20%C3%A1n%20ph%E1%BA%A7n%20m%E1%BB%81m/%C4%90%E1%BB%93%20%C3%A1n%20m%C3%B4n%20h%E1%BB%8Dc/demo/Data/cloud_postgres_init.sql). Kịch bản này có tính năng **Unprivileged-Safe** (không đòi hỏi quyền SUPERUSER), **Lũy thừa (Idempotent)** và **Tự động đồng bộ Sequence**.

### Lựa chọn 1A: Sử dụng Neon Serverless PostgreSQL (Khuyên dùng — Nhanh nhất & Ổn định)

1. **Đăng ký / Đăng nhập**: Truy cập [https://neon.tech](https://neon.tech) và đăng nhập bằng tài khoản GitHub hoặc Google.
2. **Tạo Project mới**:
   * Nhấn nút **Create Project**.
   * Đặt tên dự án: `xyz-auto-service`.
   * Chọn vùng máy chủ gần nhất (ví dụ: `ap-southeast-1` Singapore hoặc `us-east-2`).
   * Chọn phiên bản PostgreSQL: **PostgreSQL 16**.
   * Nhấn **Create Project**.
3. **Thực thi script khởi tạo**:
   * Trên thanh điều hướng bên trái của Neon Console, chọn mục **SQL Editor**.
   * Mở file [`Data/cloud_postgres_init.sql`](file:///c:/O%20E/Qu%E1%BA%A3n%20l%C3%BD%20d%E1%BB%B1%20%C3%A1n%20ph%E1%BA%A7n%20m%E1%BB%81m/%C4%90%E1%BB%93%20%C3%A1n%20m%C3%B4n%20h%E1%BB%8Dc/demo/Data/cloud_postgres_init.sql) trong project, sao chép toàn bộ nội dung và dán vào cửa sổ **SQL Editor** của Neon.
   * Nhấn nút **Run** (hoặc `Ctrl + Enter`).
   * Bảng kết quả phía dưới sẽ hiển thị thông báo thành công và bảng kiểm tra số lượng bản ghi:
     * `workshop_settings`: 1 dòng
     * `workshop_accounts`: 3 dòng (Quản lý, Nhân viên, Khách hàng)
     * `workshop_services`: 8 dòng dịch vụ tiêu chuẩn
     * `workshop_appointments`: 2 dòng lịch hẹn mẫu
4. **Lấy chuỗi kết nối (Connection String)**:
   * Trở về trang **Dashboard** của Neon.
   * Tại mục **Connection Details**, chọn định dạng kết nối: **`Connection string`**.
   * URL mẫu nhận được sẽ có dạng:
     ```text
     postgresql://alex_owner:npg_x7ABc123@ep-proud-water-a1b2c3d4.ap-southeast-1.aws.neon.tech/neondb?sslmode=require
     ```
   * Lưu chuỗi này lại để sử dụng ở Giai đoạn 2.

---

### Lựa chọn 1B: Sử dụng Supabase PostgreSQL

1. **Đăng nhập**: Truy cập [https://supabase.com](https://supabase.com).
2. **Tạo Project**:
   * Chọn **New Project**, đặt tên `xyz-auto-service`.
   * Thiết lập **Database Password** (Hãy ghi nhớ mật khẩu này!).
   * Chọn Region: **Singapore (ap-southeast-1)**.
   * Nhấn **Create new project** (chờ 1–2 phút để hệ thống khởi tạo).
3. **Chạy Migration SQL**:
   * Tại menu bên trái, vào mục **SQL Editor**.
   * Bấm **New query**, dán toàn bộ mã từ [`Data/cloud_postgres_init.sql`](file:///c:/O%20E/Qu%E1%BA%A3n%20l%C3%BD%20d%E1%BB%B1%20%C3%A1n%20ph%E1%BA%A7n%20m%E1%BB%81m/%C4%90%E1%BB%93%20%C3%A1n%20m%C3%B4n%20h%E1%BB%8Dc/demo/Data/cloud_postgres_init.sql).
   * Nhấn nút **Run** (màu xanh).
4. **Lấy chuỗi kết nối**:
   * Vào **Project Settings** (biểu tượng bánh răng) → **Database**.
   * Cuộn đến mục **Connection parameters** hoặc **Connection string** (chọn tab **URI**).
   * Chuỗi URI Direct Connection (cổng 5432):
     ```text
     postgresql://postgres:[YOUR-PASSWORD]@db.xxxxxx.supabase.co:5432/postgres
     ```

---

### 2.1. Quy Tắc Chuyển Đổi URL Cho Spring Boot

Hệ thống XYZ Auto Service được tích hợp sẵn bộ xử lý `CloudDatabaseEnvironmentPostProcessor`, cho phép bạn cấu hình theo một trong hai cách:

* **Cách 1 (Nhanh nhất)**: Cung cấp trực tiếp biến `DATABASE_URL` dạng `postgresql://user:pass@host:port/db?sslmode=require`. Bộ xử lý thông minh của ứng dụng sẽ tự động phân giải thành JDBC URL tương thích với HikariCP.
* **Cách 2 (Tiêu chuẩn JDBC)**: Tách thành 3 biến môi trường độc lập:
  * `DB_URL`: `jdbc:postgresql://<host>:<port>/<database>?sslmode=require`
  * `DB_USER`: Tên đăng nhập database
  * `DB_PASSWORD`: Mật khẩu database

Chi tiết tham khảo thêm tại tài liệu: [`docs/database-cloud-guide.md`](file:///c:/O%20E/Qu%E1%BA%A3n%20l%C3%BD%20d%E1%BB%B1%20%C3%A1n%20ph%E1%BA%A7n%20m%E1%BB%81m/%C4%90%E1%BB%93%20%C3%A1n%20m%C3%B4n%20h%E1%BB%8Dc/demo/docs/database-cloud-guide.md).

---

## 3. GIAI ĐOẠN 2: TRIỂN KHAI BACKEND LÊN CLOUD CONTAINER (BACKEND DEPLOYMENT)

Dự án cung cấp sẵn tệp cấu hình đa tầng:
- [`Dockerfile`](file:///c:/O%20E/Qu%E1%BA%A3n%20l%C3%BD%20d%E1%BB%B1%20%C3%A1n%20ph%E1%BA%A7n%20m%E1%BB%81m/%C4%90%E1%BB%93%20%C3%A1n%20m%C3%B4n%20h%E1%BB%8Dc/demo/Dockerfile): Tối ưu hóa build 2 giai đoạn với Eclipse Temurin 21 Alpine, chạy với quyền người dùng không đặc quyền (`spring:spring`), tự động cấu hình bộ nhớ container (`-XX:MaxRAMPercentage=75.0`).
- [`render.yaml`](file:///c:/O%20E/Qu%E1%BA%A3n%20l%C3%BD%20d%E1%BB%B1%20%C3%A1n%20ph%E1%BA%A7n%20m%E1%BB%81m/%C4%90%E1%BB%93%20%C3%A1n%20m%C3%B4n%20h%E1%BB%8Dc/demo/render.yaml): Mẫu Blueprint triển khai 1-click trên Render.

### Lựa chọn 2A: Triển khai trên Render.com (Khuyên dùng)

Render cung cấp gói Web Service miễn phí có hỗ trợ Docker container hoàn chỉnh.

#### Cách 1: Triển khai tự động bằng Git Repository (Khuyên dùng)

1. Đẩy mã nguồn dự án lên kho chứa GitHub cá nhân của bạn.
2. Truy cập [https://render.com](https://render.com), đăng nhập và vào trang **Dashboard**.
3. Nhấn **New +** → Chọn **Web Service**.
4. Chọn **Build and deploy from a Git repository**, nhấn **Next** và liên kết với kho lưu trữ GitHub của bạn.
5. Thiết lập thông số dịch vụ:
   * **Name**: `xyz-auto-service-backend`
   * **Region**: `Singapore` (hoặc vùng gần bạn nhất)
   * **Branch**: `main`
   * **Runtime**: **`Docker`**
   * **Dockerfile Path**: `Dockerfile` (hoặc `backend/Dockerfile` nếu Root Directory đặt là `backend`)
   * **Instance Type**: **`Free`**
6. Cấu hình **Environment Variables** (Mục quan trọng nhất):

| Tên biến (Key) | Giá trị (Value) | Mô tả |
| :--- | :--- | :--- |
| `SPRING_PROFILES_ACTIVE` | `postgres` | Kích hoạt profile kết nối PostgreSQL thật |
| `DATABASE_URL` | `postgresql://alex_owner:...@ep-proud-...neon.tech/neondb?sslmode=require` | Chuỗi kết nối lấy từ Neon/Supabase ở Giai đoạn 1 |
| `PORT` | `8080` | Cổng HTTP lắng nghe bên trong container |
| `SESSION_COOKIE_SECURE` | `true` | Đảm bảo cookie session chỉ truyền qua HTTPS an toàn |
| `APP_CORS_ALLOWED_ORIGINS` | `https://*.netlify.app,http://localhost:3000` | Cho phép các domain Netlify truy cập API |

> [!NOTE]
> Nếu bạn muốn dùng 3 biến riêng biệt thay cho `DATABASE_URL`:
> - `DB_URL`: `jdbc:postgresql://ep-proud-water-...neon.tech:5432/neondb?sslmode=require`
> - `DB_USER`: `alex_owner`
> - `DB_PASSWORD`: `npg_x7ABc123`

7. Thiết lập **Health Check Path**:
   * Điền: `/api/csrf` (Render sẽ định kỳ kiểm tra đường dẫn này để đảm bảo ứng dụng sống khỏe).
8. Bấm **Create Web Service**.
   * Render sẽ kích hoạt tiến trình tải image base, biên dịch Maven và khởi động container.
   * Sau ~3-4 phút, trạng thái chuyển sang **`Live`**.
   * Ghi lại URL do Render cấp (ví dụ: `https://xyz-auto-service-backend.onrender.com`).

---

### Lựa chọn 2B: Triển khai trên Railway.app

1. Truy cập [https://railway.app](https://railway.app) và đăng nhập bằng GitHub.
2. Chọn **New Project** → **Deploy from GitHub repo**.
3. Chọn repo `demo`.
4. Vào mục **Variables**, thêm các biến:
   * `SPRING_PROFILES_ACTIVE` = `postgres`
   * `DATABASE_URL` = `<Chuỗi kết nối Neon/Supabase>`
   * `PORT` = `8080`
   * `SESSION_COOKIE_SECURE` = `true`
5. Railway sẽ tự động phát hiện `Dockerfile` và deploy trong vòng 2 phút.
6. Vào tab **Settings** → **Networking** → Bấm **Generate Domain** để lấy URL công khai (ví dụ: `https://xyz-backend.up.railway.app`).

---

## 4. GIAI ĐOẠN 3: TRIỂN KHAI FRONTEND LÊN NETLIFY (FRONTEND DEPLOYMENT)

Netlify là nền tảng tối ưu nhất cho giao diện tĩnh HTML/JS. Tại đây, chúng ta sẽ cấu hình **API Rewrite Proxy** thông qua tệp [`netlify.toml`](file:///c:/O%20E/Qu%E1%BA%A3n%20l%C3%BD%20d%E1%BB%B1%20%C3%A1n%20ph%E1%BA%A7n%20m%E1%BB%81m/%C4%90%E1%BB%93%20%C3%A1n%20m%C3%B4n%20h%E1%BB%8Dc/demo/netlify.toml).

```mermaid
flowchart LR
    A["Trình duyệt Client\n(https://xyz-service.netlify.app)"] -->|fetch('/api/state')| B["Netlify Edge Proxy"]
    B -->|Proxy status 200 rewrite| C["Backend API\n(https://xyz-backend.onrender.com/api/state)"]
    C -->|JSON Data + Set-Cookie| B
    B -->|Cùng Origin, không lỗi CORS!| A
```

### Ưu điểm vượt trội của Netlify Proxy:
1. **Không bị chặn CORS**: Trình duyệt chỉ tương tác với domain `*.netlify.app` của chính nó.
2. **Không mất Session Cookie**: Trình duyệt coi đây là cookie cùng nguồn (First-party cookie), không bị các chính sách bảo mật chặn bên thứ ba (Third-party cookie restrictions).
3. **Che giấu địa chỉ backend thực**: Tăng cường bảo mật hạ tầng backend.

---

### Cách 3A: Triển khai tự động qua kết nối Git Repo (Khuyên dùng)

1. Truy cập [https://app.netlify.com](https://app.netlify.com) và đăng nhập.
2. Nhấn nút **Add new site** → Chọn **Import an existing project**.
3. Chọn dịch vụ Git (**GitHub**) và cấp quyền truy cập kho chứa mã nguồn dự án.
4. Tại màn hình **Site configuration**:
   * **Base directory**: `.` (để trống hoặc gõ dấu chấm)
   * **Build command**: `node ./build-redirects.js || true`
   * **Publish directory**: `frontend`
5. Nhấn vào mục **Add environment variables**:
   * **Key**: `BACKEND_URL`
   * **Value**: `https://xyz-auto-service-backend.onrender.com` *(Thay bằng URL Render bạn nhận được ở Giai đoạn 2, không có dấu gạch chéo `/` ở cuối)*.
6. Nhấn **Deploy site**.
   * Netlify sẽ chạy script sinh file proxy `_redirects` động trỏ tới backend URL của bạn.
   * Quá trình xuất bản hoàn tất trong vòng ~10 giây.
   * Netlify sẽ cấp cho bạn một tên miền ngẫu nhiên (ví dụ: `https://resonant-cheesecake-123456.netlify.app`). Bạn có thể vào **Site configuration** → **Change site name** để đổi thành `https://xyz-auto-service.netlify.app`.

---

### Cách 3B: Triển khai thủ công bằng Kéo & Thả (Netlify Drop)

Nếu bạn không muốn liên kết GitHub hoặc muốn kiểm tra ngay:

1. Mở file [`frontend/_redirects`](file:///c:/O%20E/Qu%E1%BA%A3n%20l%C3%BD%20d%E1%BB%B1%20%C3%A1n%20ph%E1%BA%A7n%20m%E1%BB%81m/%C4%90%E1%BB%93%20%C3%A1n%20m%C3%B4n%20h%E1%BB%8Dc/demo/frontend/_redirects) trên máy của bạn.
2. Sửa dòng đầu tiên trỏ tới backend URL của bạn:
   ```text
   /api/*  https://xyz-auto-service-backend.onrender.com/api/:splat  200!
   /*      /index.html                                               200
   ```
3. Truy cập [https://app.netlify.com/drop](https://app.netlify.com/drop).
4. Kéo thư mục **`frontend`** từ máy tính của bạn và thả vào khung tải lên của Netlify.
5. Sau 5 giây, trang web sẽ được xuất bản trực tuyến ngay lập tức!

---

## 5. GIAI ĐOẠN 4: KIỂM THỬ KHÓI VÀ XÁC NHẬN CHẤT LƯỢNG (SMOKE TESTING)

Sau khi hoàn tất cả 3 giai đoạn, hãy thực hiện kiểm thử chức năng toàn diện trên trình duyệt:

```mermaid
flowchart TD
    Step1["1. Kiểm tra Health Check: GET /api/csrf"] --> Step2["2. Đăng nhập Quản lý (0901234567)"]
    Step2 --> Step3["3. Đổi năng lực tiếp nhận (Capacity)"]
    Step3 --> Step4["4. Đăng nhập Nhân viên (0912345678)"]
    Step4 --> Step5["5. Duyệt lịch hẹn & Gán KTV / Khoang"]
    Step5 --> Step6["6. Đăng nhập Khách hàng & Đặt lịch mới"]
    Step6 --> Step7["7. Kiểm tra đồng bộ cơ sở dữ liệu"]

    style Step1 fill:#e0f7fa,stroke:#00acc1
    style Step2 fill:#e8f5e9,stroke:#43a047
    style Step3 fill:#fff9c4,stroke:#fbc02d
    style Step4 fill:#e8f5e9,stroke:#43a047
    style Step5 fill:#ffe0b2,stroke:#fb8c00
    style Step6 fill:#ede7f6,stroke:#8e24aa
    style Step7 fill:#c8e6c9,stroke:#2e7d32,stroke-width:2px
```

### Danh Sách Tài Khoản Mẫu Mặc Định:

| Vai trò | Số điện thoại | Mật khẩu | Chức năng kiểm thử chính |
| :--- | :---: | :---: | :--- |
| **Quản lý (`MANAGER`)** | `0901234567` | `12345678` | Đổi năng lực tiếp nhận (capacity), xem thống kê lịch đặt, cấu hình giá dịch vụ |
| **Nhân viên (`STAFF`)** | `0912345678` | `12345678` | Xem danh sách lịch hẹn chờ duyệt, gán KTV (Kỹ thuật viên), phân khoang Bay 1–4, tiếp nhận xe |
| **Khách hàng (`CUSTOMER`)** | `0988888888` | `12345678` | Chọn ngày giờ, chọn dịch vụ bảo dưỡng, nhập biển số xe & ghi chú, đặt lịch trực tuyến, theo dõi tiến độ |

### Quy Trình Kiểm Thử Khói (Smoke Test Checklist):

1. **Kiểm tra trạng thái CSRF & API Connectivity**:
   * Mở DevTools (nhấn `F12`) → chọn tab **Network**.
   * Truy cập trang web Netlify: `https://xyz-auto-service.netlify.app`.
   * Quan sát request tới `/api/csrf`:
     * Mã phản hồi HTTP: `200 OK`.
     * Response body trả về định dạng JSON: `{"headerName":"X-CSRF-TOKEN","parameterName":"_csrf","token":"..."}`.
   * Quan sát request tới `/api/state`: Trả về trạng thái xưởng và danh sách 8 dịch vụ tiêu chuẩn.

2. **Kiểm thử Luồng Quản Lý**:
   * Nhấn nút **Đăng nhập** ở góc phải trên.
   * Nhập SĐT `0901234567`, Mật khẩu `12345678`.
   * Kiểm tra giao diện hiển thị huy hiệu: `Quản lý xưởng`.
   * Vào phần **Cấu hình năng lực tiếp nhận**: Chuyển capacity từ `3` thành `4` và bấm lưu.
   * Làm mới trang (`F5`): Xác nhận thông số `4 xe/khung giờ` được lưu trữ bền vững trong PostgreSQL.

3. **Kiểm thử Luồng Đặt Lịch (Khách Hàng)**:
   * Đăng xuất tài khoản Quản lý.
   * Đăng nhập tài khoản Khách hàng: `0988888888` / `12345678` (hoặc bấm **Đăng ký** tạo SĐT mới).
   * Chọn ngày khám xe (ví dụ: ngày làm việc tiếp theo).
   * Chọn khung giờ: `08:00`.
   * Tích chọn 2 dịch vụ: **Thay dầu động cơ & lọc nhớt** (450.000 đ) + **Cân chỉnh thước lái 3D** (350.000 đ).
   * Điền biển số xe: `30A-999.88`.
   * Bấm **Xác nhận đặt hẹn**.
   * Kiểm tra thông báo thành công: Lịch hẹn chuyển sang trạng thái `Chờ tiếp nhận`.

4. **Kiểm thử Luồng Tiếp Nhận & Phân Khoang (Nhân Viên)**:
   * Đăng xuất và đăng nhập với tài khoản Nhân viên: `0912345678` / `12345678`.
   * Vào tab **Danh sách lịch hẹn hôm nay**.
   * Tìm biển số xe `30A-999.88` vừa đặt.
   * Bấm **Tiếp nhận**: Chọn khoang sửa chữa `Khoang 1 (Bay 1)`, gán Kỹ thuật viên `Nguyễn Văn A`.
   * Xác nhận trạng thái chuyển thành `Đang thực hiện`.

5. **Kiểm tra An toàn Bảo mật HTTP Headers**:
   * Kiểm tra Response Headers của trang web trên tab Network:
     * `X-Content-Type-Options: nosniff` (Chống MIME-type sniffing)
     * `X-Frame-Options: DENY` (Chống tấn công Clickjacking)
     * `Referrer-Policy: strict-origin-when-cross-origin`
     * `Set-Cookie: ...; SameSite=Lax; HttpOnly`

---

## 6. XỬ LÝ SỰ CỐ VÀ TỐI ƯU HÓA (TROUBLESHOOTING & FAQS)

### 1. Hiện tượng "Cold Start" (Khởi động nguội) trên gói miễn phí của Render
* **Hiện tượng**: Lần truy cập đầu tiên sau 15 phút không có người dùng, trang web tải mất khoảng 40–50 giây.
* **Nguyên nhân**: Render Free Tier tự động đưa container vào chế độ ngủ (sleep) để tiết kiệm tài nguyên.
* **Giải pháp đã tích hợp**:
  * Frontend của hệ thống đã được tối ưu: khi backend đang khởi động, frontend sẽ tự động thử lại (retry) với thông báo trạng thái thân thiện.
  * Mẹo duy trì ứng dụng thức (Wake-up ping): Sử dụng dịch vụ giám sát miễn phí như [UptimeRobot](https://uptimerobot.com/) hoặc [Cron-job.org](https://cron-job.org/) để gửi request `GET /api/csrf` mỗi 10 phút một lần.

### 2. Lỗi `Connection refused` hoặc `SSL connection required` tới cơ sở dữ liệu
* **Hiện tượng**: Backend log báo `PSQLException: SSL error` hoặc không kết nối được database.
* **Nguyên nhân**: Thiếu tham số SSL bắt buộc của Cloud PostgreSQL.
* **Khắc phục**: Đảm bảo chuỗi kết nối luôn có đuôi `?sslmode=require`. Nếu sử dụng JDBC URL, định dạng chuẩn là:
  ```text
  jdbc:postgresql://<host>:5432/<database>?sslmode=require
  ```

### 3. Lỗi 403 Forbidden khi thực hiện POST/PUT (Lỗi CSRF Token)
* **Hiện tượng**: Gọi API POST đăng nhập hoặc đặt lịch trả về HTTP 403.
* **Giải pháp đã tích hợp**:
  * Trong file [`frontend/app.js`](file:///c:/O%20E/Qu%E1%BA%A3n%20l%C3%BD%20d%E1%BB%B1%20%C3%A1n%20ph%E1%BA%A7n%20m%E1%BB%81m/%C4%90%E1%BB%93%20%C3%A1n%20m%C3%B4n%20h%E1%BB%8Dc/demo/frontend/app.js), hàm `api()` được cấu hình tự động gửi kèm header `X-CSRF-TOKEN`.
  * Nếu token hết hạn hoặc session mới được tạo, client sẽ tự động gọi `refreshCsrf()` và thử lại request 1 lần hoàn toàn tự động và trong suốt với người dùng.

### 4. Lỗi trùng khóa chính Sequence (`duplicate key value violates unique constraint`)
* **Hiện tượng**: Đăng ký khách hàng mới hoặc tạo lịch hẹn mới bị lỗi trùng ID.
* **Nguyên nhân**: Nạp dữ liệu mẫu có sẵn số ID thủ công nhưng sequence của bảng chưa được cập nhật.
* **Giải pháp đã xử lý**: Kịch bản [`Data/cloud_postgres_init.sql`](file:///c:/O%20E/Qu%E1%BA%A3n%20l%C3%BD%20d%E1%BB%B1%20%C3%A1n%20ph%E1%BA%A7n%20m%E1%BB%81m/%C4%90%E1%BB%93%20%C3%A1n%20m%C3%B4n%20h%E1%BB%8Dc/demo/Data/cloud_postgres_init.sql) có sẵn các câu lệnh:
  ```sql
  SELECT setval(pg_get_serial_sequence('workshop_accounts', 'id'), COALESCE(MAX(id), 1)) FROM workshop_accounts;
  SELECT setval(pg_get_serial_sequence('workshop_appointments', 'id'), COALESCE(MAX(id), 1)) FROM workshop_appointments;
  ```
  giúp con trỏ sequence tự động nhảy lên giá trị an toàn lớn nhất.

---

## 7. PHỤ LỤC: CHẠY THỬ NGHIỆM TẠI MÁY CỤC BỘ (LOCAL DEVELOPMENT)

Nếu bạn muốn chạy thử nghiệm nhanh trên máy cá nhân trước khi đưa lên mạng:

### Cách 1: Khởi chạy 1-Click trên IntelliJ IDEA (Khuyên dùng cho lập trình viên)
1. Mở thư mục dự án `demo` bằng IntelliJ IDEA (2024–2026).
2. Thiết lập Project SDK: **JDK 21 LTS** (`Ctrl + Alt + Shift + S`).
3. Trên thanh công cụ Run Configurations góc trên bên phải, chọn:
   👉 **`XYZ Auto Service (1-Click Run)`**
4. Bấm nút **Run ▶** (`Shift + F10`).
5. Truy cập ứng dụng tại: `http://localhost:8080`.

### Cách 2: Chạy với PostgreSQL thật qua Docker Compose
1. Khởi động PostgreSQL 16 container:
   ```bash
   docker compose up -d
   ```
2. Khởi chạy Backend với hồ sơ PostgreSQL:
   ```powershell
   $env:JAVA_HOME = "$env:USERPROFILE\.jdks\ms-21.0.11"
   $env:SPRING_PROFILES_ACTIVE = "postgres"
   $env:DB_URL = "jdbc:postgresql://localhost:5432/xyz_auto_service"
   $env:DB_USER = "postgres"
   $env:DB_PASSWORD = "postgres"
   cd backend
   .\mvnw.cmd spring-boot:run
   ```
3. Xem tài liệu kiểm thử: [`docs/test-plan.md`](file:///c:/O%20E/Qu%E1%BA%A3n%20l%C3%BD%20d%E1%BB%B1%20%C3%A1n%20ph%E1%BA%A7n%20m%E1%BB%81m/%C4%90%E1%BB%93%20%C3%A1n%20m%C3%B4n%20h%E1%BB%8Dc/demo/docs/test-plan.md) và hợp đồng API: [`docs/api-contract.md`](file:///c:/O%20E/Qu%E1%BA%A3n%20l%C3%BD%20d%E1%BB%B1%20%C3%A1n%20ph%E1%BA%A7n%20m%E1%BB%81m/%C4%90%E1%BB%93%20%C3%A1n%20m%C3%B4n%20h%E1%BB%8Dc/demo/docs/api-contract.md).
