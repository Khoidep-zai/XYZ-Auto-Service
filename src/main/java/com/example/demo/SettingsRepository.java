package com.example.demo;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface SettingsRepository extends JpaRepository<WorkshopDomain.Settings, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from WorkshopSettings s where s.id = 1")
    Optional<WorkshopDomain.Settings> lockSettings();
}
