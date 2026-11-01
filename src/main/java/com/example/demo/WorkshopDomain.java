package com.example.demo;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Persistent workshop records. Prices and service names on an appointment are snapshots. */
public final class WorkshopDomain {
    private WorkshopDomain() {}

    public enum Role { CUSTOMER, STAFF, MANAGER }
    public enum Status { PENDING, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED, REJECTED, NO_SHOW }

    @Entity(name = "WorkshopAccount")
    @Table(name = "workshop_accounts")
    public static class Account {
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
        @Column(nullable = false, unique = true, length = 20) public String phone;
        @Column(nullable = false, length = 100) public String name;
        @Column(nullable = false) public String passwordHash;
        @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16) public Role role;
        @Column(nullable = false, length = 200) public String meta = "";
        public Long getId() { return id; }
        public String getPhone() { return phone; }
        public String getName() { return name; }
        public Role getRole() { return role; }
        public String getMeta() { return meta; }
    }

    @Entity(name = "WorkshopService")
    @Table(name = "workshop_services")
    public static class ServiceItem {
        @Id @Column(length = 32) public String id;
        @Column(nullable = false, length = 150) public String name;
        @Column(nullable = false, length = 100) public String category;
        @Column(nullable = false) public int duration;
        @Column(nullable = false) public long price;
        @Column(nullable = false, length = 100) public String warranty = "";
        @Column(nullable = false, length = 1000) public String description = "";
        @Column(nullable = false, length = 2000) public String checklist = "";
        @Column(nullable = false) public boolean active = true;
    }

    @Embeddable
    public static class ServiceSnapshot {
        @Column(name = "service_code", nullable = false, length = 32) public String serviceId;
        @Column(name = "service_name", nullable = false, length = 150) public String name;
        @Column(name = "service_price", nullable = false) public long price;
        @Column(name = "service_duration", nullable = false) public int duration;
    }

    @Entity(name = "WorkshopAppointment")
    @Table(name = "workshop_appointments", indexes = {
            @Index(name = "idx_appointment_slot", columnList = "booking_date,slot,status"),
            @Index(name = "idx_appointment_customer", columnList = "customer_id") })
    public static class Appointment {
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
        @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "customer_id") public Account customer;
        @Column(nullable = false, length = 100) public String customerName;
        @Column(nullable = false, length = 20) public String phone;
        @Column(nullable = false, length = 30) public String licensePlate;
        @Column(nullable = false, length = 100) public String carModel;
        @Column(name = "booking_date", nullable = false) public LocalDate date;
        @Column(nullable = false, length = 5) public String slot;
        @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) public Status status;
        @Column(nullable = false) public int totalDuration;
        @Column(nullable = false) public long totalPrice;
        @Column(nullable = false, length = 120) public String bay = "Chờ phân khoang";
        @Column(nullable = false, length = 120) public String technician = "Chờ phân công";
        @Column(nullable = false, length = 1000) public String note = "";
        @Column(nullable = false, length = 1000) public String staffNote = "";
        @ElementCollection(fetch = FetchType.EAGER)
        @CollectionTable(name = "workshop_appointment_services", joinColumns = @JoinColumn(name = "appointment_id"))
        public List<ServiceSnapshot> services = new ArrayList<>();
    }

    @Entity(name = "WorkshopSettings")
    @Table(name = "workshop_settings")
    public static class Settings {
        @Id public Long id = 1L;
        @Column(nullable = false) public int capacity = 3;
    }
}
