package com.example.demo;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

public final class ApiModels {
    private ApiModels() {}

    public record AccountInput(
            @NotBlank @Size(max = 100) String name,
            @NotBlank @Pattern(regexp = "0[0-9]{9,10}") String phone,
            @NotBlank @Size(min = 8, max = 72) String password,
            @Size(max = 200) String meta,
            WorkshopDomain.Role role) {}

    public record ServiceInput(
            @NotBlank @Pattern(regexp = "[A-Z0-9-]{2,32}") String code,
            @NotBlank @Size(max = 150) String name,
            @NotBlank @Size(max = 100) String category,
            @Min(1) @Max(1440) int duration,
            @Min(0) @Max(1000000000) long price,
            @Size(max = 100) String warranty,
            @Size(max = 1000) String description,
            @Size(max = 30) List<@NotBlank @Size(max = 100) String> checklist,
            boolean active) {}

    public record BookingInput(
            @NotBlank @Size(max = 100) String customerName,
            @NotBlank @Pattern(regexp = "0[0-9]{9,10}") String phone,
            @NotBlank @Pattern(regexp = "[A-Za-z0-9. -]{4,30}") String licensePlate,
            @NotBlank @Size(max = 100) String carModel,
            @NotNull LocalDate date,
            @NotBlank String slot,
            @NotEmpty @Size(max = 30) List<@NotBlank String> serviceIds,
            @Size(max = 1000) String note,
            boolean walkIn,
            @Size(max = 120) String bay,
            @Size(max = 120) String technician) {}

    public record RescheduleInput(@NotNull LocalDate date, @NotBlank String slot,
            @NotEmpty @Size(max = 30) List<@NotBlank String> serviceIds) {}
    public record StatusInput(@NotNull WorkshopDomain.Status status,
            @Size(max = 1000) String reason, @Size(max = 120) String bay,
            @Size(max = 120) String technician) {}
    public record CapacityInput(@Min(1) @Max(5) int capacity) {}
}
