package org.ups.citasalud.adapter.out.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TimeSlotJpaRepository extends JpaRepository<TimeSlotJpaEntity, UUID> {

    List<TimeSlotJpaEntity> findByDoctorIdAndDate(UUID doctorId, LocalDate date);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM TimeSlotJpaEntity t WHERE t.id = :id")
    Optional<TimeSlotJpaEntity> findByIdForUpdate(@Param("id") UUID id);
}
