# CHECKLIST AN TOÀN THÔNG TIN & BẢO MẬT (SECURITY CHECKLIST)
## Hệ thống Quản lý Đặt lịch Dịch vụ Ô tô XYZ

---

## 1. BẢNG KIỂM TRA BẢO MẬT TỔNG THỂ (OWASP ASVS & BEST PRACTICES)

| Hạng mục kiểm tra | Tiêu chuẩn / Cơ chế áp dụng | Hiện trạng trong Codebase | Đánh giá |
| :--- | :--- | :--- | :---: |
| **Mã hóa mật khẩu (Password Hashing)** | Không lưu mật khẩu dạng thô; sử dụng thuật toán băm chậm chống brute-force | Sử dụng `BCryptPasswordEncoder` với salt ngẫu nhiên chuẩn 10 vòng | **ĐẠT** |
| **Bảo vệ CSRF (CSRF Protection)** | Chống giả mạo yêu cầu từ trang web bên thứ ba | Kích hoạt `CsrfTokenRepository` trong Spring Security; frontend gửi header `X-CSRF-TOKEN` | **ĐẠT** |
| **Bảo vệ Phiên (Session Security)** | Ngăn chặn đánh cắp Session ID qua JavaScript độc hại | Cấu hình `HttpOnly = true`, `SameSite = Lax` cho Cookie phiên `JSESSIONID` | **ĐẠT** |
| **Chống SQL Injection** | Sử dụng Parameterized Queries hoặc ORM chuẩn | Sử dụng 100% Spring Data JPA và Hibernate Criteria, tham số hóa toàn bộ biến truy vấn | **ĐẠT** |
| **Kiểm tra dữ liệu đầu vào (Input Validation)** | Không tin tưởng dữ liệu từ client gửi lên | Sử dụng `jakarta.validation` (`@NotBlank`, `@Size`, `@Pattern`, `@Min`, `@Max`) trên tất cả DTO | **ĐẠT** |
| **Chống tấn công XSS (Cross-Site Scripting)** | Mã hóa ký tự đặc biệt HTML trước khi render vào DOM | Hàm `escapeHtml()` trong JavaScript mã hóa `& < > " '` trước khi đưa vào template string | **ĐẠT** |
| **Phân quyền theo vai trò (RBAC)** | Thực thi kiểm tra quyền ở cả tầng API và tầng Service | Sử dụng `@EnableMethodSecurity` và `@PreAuthorize("hasRole('MANAGER')")` | **ĐẠT** |
| **Bảo mật dữ liệu cá nhân (Data Privacy)** | Khách hàng không thể xem trộm thông tin xe hoặc lịch của người khác | `WorkshopService.state()` chỉ trả về danh sách lịch thuộc `actor.id` đối với quyền `CUSTOMER` | **ĐẠT** |
| **Phòng chống Race Condition** | Chống tranh chấp tài nguyên hạn chế khi gửi request đồng thời | Khóa bi quan `@Lock(LockModeType.PESSIMISTIC_WRITE)` (`SELECT ... FOR UPDATE`) trên bảng Settings | **ĐẠT** |
| **Ẩn thông tin lỗi hệ thống (Error Disclosure)** | Không làm lộ stack trace, phiên bản thư viện hoặc câu lệnh SQL ra client | Cấu hình `spring.web.error.include-message=never`, xử lý lỗi tập trung qua `@RestControllerAdvice` | **ĐẠT** |

---

## 2. CHI TIẾT CÁC CƠ CHẾ BẢO MẬT ĐÃ ĐƯỢC CÀI ĐẶT

### 2.1. Cấu hình Spring Security Chain
```java
@Bean SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(auth -> auth
            .requestMatchers("/", "/index.html", "/app.js", "/workshop.css", "/vendor/**", 
                             "/api/csrf", "/api/state", "/api/register", "/error").permitAll()
            .anyRequest().authenticated())
        .exceptionHandling(errors -> errors
            .authenticationEntryPoint((req, res, ex) -> res.sendError(HttpStatus.UNAUTHORIZED.value()))
            .accessDeniedHandler((req, res, ex) -> res.sendError(HttpStatus.FORBIDDEN.value())))
        .formLogin(form -> form.loginProcessingUrl("/api/login")
            .usernameParameter("phone").passwordParameter("password")
            .successHandler((req, res, auth) -> res.setStatus(HttpStatus.NO_CONTENT.value()))
            .failureHandler((req, res, ex) -> res.sendError(HttpStatus.UNAUTHORIZED.value())))
        .logout(logout -> logout.logoutUrl("/api/logout")
            .logoutSuccessHandler((req, res, auth) -> res.setStatus(HttpStatus.NO_CONTENT.value())));
    return http.build();
}
```

### 2.2. Kiểm soát độ dài chuỗi mật khẩu chống DoS băm BCrypt
Thuật toán BCrypt chỉ xử lý tối đa 72 bytes. Để ngăn chặn kẻ tấn công gửi chuỗi mật khẩu siêu dài hàng triệu ký tự gây nghẽn CPU máy chủ (ReDoS / Hash DoS), hệ thống kiểm tra:
```java
if (input.password().getBytes(StandardCharsets.UTF_8).length > 72)
    throw invalid("Mật khẩu phải có tối đa 72 byte UTF-8");
```

### 2.3. Quy tắc kiểm soát quyền sở hữu tài nguyên (Resource Ownership Check)
Trong mọi thao tác đổi lịch (`reschedule`) hoặc hủy lịch (`cancel`), hệ thống bắt buộc kiểm tra danh tính:
```java
private void requireOwner(Account actor, Appointment appointment) {
    if (appointment.customer == null || !Objects.equals(appointment.customer.getId(), actor.id))
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Lịch hẹn không thuộc tài khoản này");
}
```
Khách hàng A hoàn toàn không thể can thiệp hay sửa đổi lịch hẹn của Khách hàng B dù cố tình đoán được ID lịch hẹn.

