package com.example.demo;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<WorkshopDomain.Account, Long> {
    Optional<WorkshopDomain.Account> findByPhone(String phone);
}
