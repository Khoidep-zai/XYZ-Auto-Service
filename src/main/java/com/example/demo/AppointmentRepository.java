package com.example.demo;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AppointmentRepository extends JpaRepository<WorkshopDomain.Appointment, Long> {
    List<WorkshopDomain.Appointment> findByCustomerIdOrderByDateDescIdDesc(Long customerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from WorkshopAppointment a where a.id = :id")
    Optional<WorkshopDomain.Appointment> lockById(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<WorkshopDomain.Appointment> findByDateAndSlotAndStatusIn(LocalDate date, String slot, Collection<WorkshopDomain.Status> statuses);

    @Query("select a.date, a.slot, count(a) from WorkshopAppointment a where a.status in :statuses group by a.date, a.slot")
    List<Object[]> countBySlot(@Param("statuses") Collection<WorkshopDomain.Status> statuses);
}
