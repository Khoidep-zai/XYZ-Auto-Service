package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceRepository extends JpaRepository<WorkshopDomain.ServiceItem, String> {}
