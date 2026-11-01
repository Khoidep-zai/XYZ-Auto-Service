package com.example.demo;

import static com.example.demo.WorkshopDomain.*;
import java.security.Principal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class WorkshopService {
    static final List<String> SLOTS = List.of("08:00", "09:00", "10:00", "11:00", "13:00", "14:00", "15:00", "16:00");
    static final Set<Status> OCCUPIED = EnumSet.of(Status.PENDING, Status.CONFIRMED, Status.IN_PROGRESS, Status.COMPLETED);
    private static final ZoneId WORKSHOP_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private final AccountRepository accounts;
    private final ServiceRepository services;
    private final AppointmentRepository appointments;
    private final SettingsRepository settings;
    private final PasswordEncoder passwords;

    public WorkshopService(AccountRepository accounts, ServiceRepository services, AppointmentRepository appointments,
            SettingsRepository settings, PasswordEncoder passwords) {
        this.accounts = accounts; this.services = services; this.appointments = appointments;
        this.settings = settings; this.passwords = passwords;
    }

    private ResponseStatusException invalid(String message) { return new ResponseStatusException(HttpStatus.BAD_REQUEST, message); }
    private ResponseStatusException conflict(String message) { return new ResponseStatusException(HttpStatus.CONFLICT, message); }
    private String clean(String value) { return value == null ? "" : value.trim(); }
    private Account current(Principal principal) {
        if (principal == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Vui lòng đăng nhập");
        return accounts.findByPhone(principal.getName()).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }
    private Settings lockSettings() { return settings.lockSettings().orElseThrow(() -> new IllegalStateException("Thiếu cấu hình xưởng")); }
    private Appointment lockAppointment(Long id) {
        return appointments.lockById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy lịch hẹn"));
    }
    private void requireOwner(Account actor, Appointment appointment) {
        if (appointment.customer == null || !Objects.equals(appointment.customer.getId(), actor.id))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Lịch hẹn không thuộc tài khoản này");
    }
    private void requireStaff(Account actor) {
        if (actor.role == Role.CUSTOMER) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cần quyền nhân viên");
    }
    private void validateSlot(LocalDate date, String slot) {
        LocalDate today = LocalDate.now(WORKSHOP_ZONE);
        if (date == null || date.isBefore(today)) throw invalid("Ngày đặt lịch không được ở quá khứ");
        if (!SLOTS.contains(slot)) throw invalid("Khung giờ không hợp lệ");
        if (date.getDayOfWeek() == DayOfWeek.SUNDAY) throw invalid("Xưởng nghỉ Chủ nhật");
        if (date.equals(today) && !LocalTime.parse(slot).isAfter(LocalTime.now(WORKSHOP_ZONE)))
            throw invalid("Khung giờ này đã qua");
    }
    private void validateSpace(Settings config, LocalDate date, String slot, String plate, Long excludeId) {
        validateSlot(date, slot);
        var occupied = appointments.findByDateAndSlotAndStatusIn(date, slot, OCCUPIED).stream()
                .filter(a -> !Objects.equals(a.id, excludeId)).toList();
        if (occupied.size() >= config.capacity) throw conflict("Khung giờ đã đầy, vui lòng chọn khung giờ khác");
        if (occupied.stream().anyMatch(a -> a.licensePlate.equalsIgnoreCase(plate)))
            throw conflict("Biển số này đã có lịch trong khung giờ đã chọn");
    }
    private void validateBay(Appointment target, String bay) {
        if (bay.isBlank()) throw invalid("Vui lòng chọn khoang tiếp nhận");
        boolean used = appointments.findByDateAndSlotAndStatusIn(target.date, target.slot, OCCUPIED).stream()
                .anyMatch(a -> !Objects.equals(a.id, target.id) && bay.equals(a.bay));
        if (used) throw conflict("Khoang này đã được phân công trong khung giờ");
    }
    private void setServices(Appointment appointment, List<String> ids) {
        if (new HashSet<>(ids).size() != ids.size()) throw invalid("Dịch vụ bị trùng");
        var snapshots = new ArrayList<ServiceSnapshot>();
        for (String id : ids) {
            var item = services.findById(id).orElseThrow(() -> invalid("Dịch vụ không tồn tại"));
            if (!item.active) throw invalid("Dịch vụ " + id + " đã tạm ngưng");
            var snapshot = new ServiceSnapshot();
            snapshot.serviceId = item.id; snapshot.name = item.name; snapshot.price = item.price; snapshot.duration = item.duration;
            snapshots.add(snapshot);
        }
        appointment.services.clear(); appointment.services.addAll(snapshots);
        appointment.totalPrice = snapshots.stream().mapToLong(s -> s.price).sum();
        appointment.totalDuration = snapshots.stream().mapToInt(s -> s.duration).sum();
    }

    @Transactional
    public void createAccount(ApiModels.AccountInput input, boolean managerRequest) {
        if (input.password().getBytes(StandardCharsets.UTF_8).length > 72)
            throw invalid("Mật khẩu phải có tối đa 72 byte UTF-8");
        if (accounts.findByPhone(input.phone()).isPresent()) throw conflict("Số điện thoại đã được đăng ký");
        var account = new Account();
        account.phone = input.phone(); account.name = clean(input.name()); account.meta = clean(input.meta());
        account.role = managerRequest && input.role() != null ? input.role() : Role.CUSTOMER;
        account.passwordHash = passwords.encode(input.password());
        accounts.save(account);
    }

    @Transactional
    public void saveService(String id, ApiModels.ServiceInput input) {
        if (id == null && services.existsById(input.code())) throw conflict("Mã dịch vụ đã tồn tại");
        if (id != null && !id.equals(input.code())) throw invalid("Không thể thay đổi mã dịch vụ");
        var item = id == null ? new ServiceItem() : services.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy dịch vụ"));
        item.id = input.code(); item.name = clean(input.name()); item.category = clean(input.category());
        item.duration = input.duration(); item.price = input.price(); item.active = input.active();
        item.warranty = clean(input.warranty()); item.description = clean(input.description());
        item.checklist = input.checklist() == null ? "" : input.checklist().stream().map(String::trim).collect(Collectors.joining("; "));
        services.save(item);
    }

    @Transactional
    public void setCapacity(int capacity) {
        var config = lockSettings();
        var counts = appointments.findAll().stream().filter(a -> !a.date.isBefore(LocalDate.now(WORKSHOP_ZONE)) && OCCUPIED.contains(a.status))
                .collect(Collectors.groupingBy(a -> a.date + "|" + a.slot, Collectors.counting()));
        if (counts.values().stream().anyMatch(count -> count > capacity))
            throw conflict("Không thể giảm công suất thấp hơn số lịch đã đặt");
        config.capacity = capacity;
    }

    @Transactional
    public Long book(Principal principal, ApiModels.BookingInput input) {
        var actor = current(principal); var config = lockSettings();
        if (input.walkIn()) requireStaff(actor);
        var appointment = new Appointment();
        appointment.customer = input.walkIn() ? accounts.findByPhone(input.phone()).filter(a -> a.role == Role.CUSTOMER).orElse(null) : actor;
        appointment.customerName = clean(input.customerName()); appointment.phone = input.phone();
        appointment.licensePlate = clean(input.licensePlate()).toUpperCase(Locale.ROOT);
        appointment.carModel = clean(input.carModel()); appointment.date = input.date(); appointment.slot = input.slot();
        appointment.note = clean(input.note());
        validateSpace(config, appointment.date, appointment.slot, appointment.licensePlate, null);
        setServices(appointment, input.serviceIds());
        if (input.walkIn()) {
            appointment.bay = clean(input.bay()); appointment.technician = clean(input.technician());
            if (appointment.technician.isBlank()) throw invalid("Vui lòng chọn kỹ thuật viên");
            validateBay(appointment, appointment.bay);
            appointment.status = Status.CONFIRMED; appointment.staffNote = "Đã tiếp nhận tại quầy";
        } else appointment.status = Status.PENDING;
        return appointments.saveAndFlush(appointment).id;
    }

    @Transactional
    public void reschedule(Principal principal, Long id, ApiModels.RescheduleInput input) {
        var actor = current(principal); var config = lockSettings(); var appointment = lockAppointment(id);
        requireOwner(actor, appointment);
        if (appointment.status != Status.PENDING && appointment.status != Status.CONFIRMED)
            throw conflict("Không thể đổi lịch ở trạng thái hiện tại");
        validateSpace(config, input.date(), input.slot(), appointment.licensePlate, id);
        setServices(appointment, input.serviceIds());
        appointment.date = input.date(); appointment.slot = input.slot(); appointment.status = Status.PENDING;
        appointment.bay = "Chờ phân khoang"; appointment.technician = "Chờ phân công";
        appointment.staffNote = "Khách đã thay đổi lịch; đang chờ xác nhận lại";
    }

    @Transactional
    public void changeStatus(Principal principal, Long id, ApiModels.StatusInput input) {
        var actor = current(principal); lockSettings(); var appointment = lockAppointment(id);
        Status next = input.status();
        if (next == Status.CANCELLED) {
            requireOwner(actor, appointment);
            if (appointment.status != Status.PENDING && appointment.status != Status.CONFIRMED)
                throw conflict("Không thể hủy lịch ở trạng thái hiện tại");
        } else {
            requireStaff(actor);
            boolean allowed = switch (appointment.status) {
                case PENDING -> next == Status.CONFIRMED || next == Status.REJECTED;
                case CONFIRMED -> next == Status.IN_PROGRESS || next == Status.NO_SHOW;
                case IN_PROGRESS -> next == Status.COMPLETED;
                default -> false;
            };
            if (!allowed) throw conflict("Không thể chuyển sang trạng thái này");
        }
        if (next == Status.CONFIRMED) {
            String bay = clean(input.bay()), technician = clean(input.technician());
            if (technician.isBlank()) throw invalid("Vui lòng chọn kỹ thuật viên");
            validateBay(appointment, bay);
            appointment.bay = bay; appointment.technician = technician;
        }
        appointment.status = next;
        appointment.staffNote = clean(input.reason());
        if (next == Status.CANCELLED || next == Status.REJECTED || next == Status.NO_SHOW) appointment.bay = "Đã giải phóng";
    }

    @Transactional(readOnly = true)
    public Map<String, Object> state(Principal principal) {
        Account actor = principal == null ? null : current(principal);
        var visible = actor == null ? List.<Appointment>of() : actor.role == Role.CUSTOMER
                ? appointments.findByCustomerIdOrderByDateDescIdDesc(actor.id) : appointments.findAll();
        var result = new LinkedHashMap<String, Object>();
        result.put("currentRole", actor == null ? "CUSTOMER" : actor.role.name());
        result.put("authenticated", actor != null);
        result.put("activeAccountId", actor == null ? "guest" : actor.id.toString());
        result.put("accounts", actor == null ? List.of(Map.of("id", "guest", "name", "Khách", "phone", "", "role", "CUSTOMER", "avatar", "K", "meta", "Chưa đăng nhập"))
                : (actor.role == Role.MANAGER ? accounts.findAll() : List.of(actor)).stream().map(this::accountView).toList());
        result.put("services", services.findAll().stream().map(this::serviceView).toList());
        result.put("appointments", visible.stream().map(this::appointmentView).toList());
        result.put("maxCapacityPerSlot", settings.findById(1L).orElseThrow().capacity);
        result.put("slotCounts", appointments.countBySlot(OCCUPIED).stream().collect(Collectors.toMap(
                row -> row[0] + "|" + row[1], row -> ((Number) row[2]).longValue())));
        result.put("customerPresetCars", List.of());
        return result;
    }

    private Map<String, Object> accountView(Account a) {
        String[] words = a.getName().trim().split("\\s+");
        String avatar = words.length > 1 ? words[words.length - 2].substring(0, 1) + words[words.length - 1].substring(0, 1) : words[0].substring(0, 1);
        return Map.of("id", a.getId().toString(), "phone", a.getPhone(), "name", a.getName(), "role", a.getRole().name(), "avatar", avatar.toUpperCase(Locale.ROOT), "meta", a.getMeta());
    }
    private Map<String, Object> serviceView(ServiceItem s) {
        var result = new LinkedHashMap<String, Object>();
        result.put("id", s.id); result.put("code", s.id); result.put("name", s.name); result.put("category", s.category);
        result.put("duration", s.duration); result.put("price", s.price); result.put("warranty", s.warranty);
        result.put("description", s.description); result.put("active", s.active);
        result.put("checklist", s.checklist.isBlank() ? List.of() : Arrays.stream(s.checklist.split(";")).map(String::trim).toList());
        return result;
    }
    private Map<String, Object> appointmentView(Appointment a) {
        var result = new LinkedHashMap<String, Object>();
        result.put("id", a.id.toString()); result.put("customerName", a.customerName); result.put("phone", a.phone);
        result.put("licensePlate", a.licensePlate); result.put("carModel", a.carModel); result.put("date", a.date.toString());
        result.put("slot", a.slot); result.put("serviceIds", a.services.stream().map(s -> s.serviceId).toList());
        result.put("serviceNames", a.services.stream().map(s -> s.name).toList());
        result.put("servicePrices", a.services.stream().map(s -> s.price).toList());
        result.put("totalDuration", a.totalDuration); result.put("totalPrice", a.totalPrice); result.put("status", a.status.name());
        result.put("bay", a.bay); result.put("technician", a.technician); result.put("note", a.note); result.put("staffNote", a.staffNote);
        return result;
    }
}
