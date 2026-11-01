package com.example.demo;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class InitialData {
    @Bean ApplicationRunner initializeWorkshop(ServiceRepository services, SettingsRepository settings,
            AccountRepository accounts, PasswordEncoder passwords,
            @Value("${APP_BOOTSTRAP_ADMIN_PHONE:}") String adminPhone,
            @Value("${APP_BOOTSTRAP_ADMIN_PASSWORD:}") String adminPassword) {
        return args -> {
            if (!settings.existsById(1L)) settings.save(new WorkshopDomain.Settings());
            if (services.count() == 0) {
                services.saveAll(List.of(
                    service("SRV-01", "Bảo dưỡng định kỳ cấp 10.000km", "Bảo dưỡng định kỳ", 60, 1450000, "6 tháng / 10.000km", "Thay dầu máy; Vệ sinh lọc gió; Kiểm tra phanh; Đảo lốp; Quét lỗi OBD-II"),
                    service("SRV-02", "Thay dầu động cơ Tổng hợp + Lọc dầu", "Thay dầu & Chất lỏng", 45, 1150000, "10.000km", "Xả nhớt cũ; Thay lọc dầu; Châm dầu tổng hợp"),
                    service("SRV-03", "Bảo dưỡng Hệ thống Phanh ABS 4 bánh", "Hệ thống phanh & Gầm", 60, 850000, "6 tháng", "Vệ sinh cụm phanh; Đo má phanh; Kiểm tra ABS"),
                    service("SRV-04", "Chẩn đoán & Sửa chữa Động cơ", "Sửa chữa động cơ", 120, 2400000, "12 tháng", "Đọc lỗi động cơ; Kiểm tra đánh lửa; Vệ sinh kim phun"),
                    service("SRV-05", "Cân chỉnh Thước lái & Độ chụm 3D", "Hệ thống phanh & Gầm", 60, 750000, "3 tháng", "Kiểm tra rô-tuyn; Đo góc đặt bánh xe; Cân chỉnh độ chụm"),
                    service("SRV-06", "Vệ sinh Điều hòa Nội soi & Khử khuẩn", "Điện & Điều hòa", 75, 950000, "6 tháng", "Đo nhiệt độ; Vệ sinh dàn lạnh; Khử khuẩn cabin")
                ));
            }
            if (!adminPhone.isBlank() || !adminPassword.isBlank()) {
                if (!adminPhone.matches("0[0-9]{9,10}") || adminPassword.length() < 8)
                    throw new IllegalStateException("APP_BOOTSTRAP_ADMIN_PHONE và APP_BOOTSTRAP_ADMIN_PASSWORD không hợp lệ");
                var existing = accounts.findByPhone(adminPhone);
                if (existing.isEmpty()) {
                    var admin = new WorkshopDomain.Account();
                    admin.phone = adminPhone;
                    admin.name = "Quản lý xưởng";
                    admin.role = WorkshopDomain.Role.MANAGER;
                    admin.meta = "Quản lý Xưởng dịch vụ";
                    admin.passwordHash = passwords.encode(adminPassword);
                    accounts.save(admin);
                } else if (existing.get().role != WorkshopDomain.Role.MANAGER) {
                    throw new IllegalStateException("Số điện thoại bootstrap đã thuộc tài khoản khác");
                }
            }
        };
    }

    private static WorkshopDomain.ServiceItem service(String id, String name, String category, int duration,
            long price, String warranty, String checklist) {
        var item = new WorkshopDomain.ServiceItem();
        item.id = id; item.name = name; item.category = category; item.duration = duration; item.price = price;
        item.warranty = warranty; item.checklist = checklist; item.description = checklist;
        return item;
    }
}
