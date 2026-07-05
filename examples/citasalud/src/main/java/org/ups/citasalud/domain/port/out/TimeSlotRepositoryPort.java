package org.ups.citasalud.domain.port.out;

import org.ups.citasalud.domain.model.TimeSlot;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TimeSlotRepositoryPort {
    List<TimeSlot> findByDoctorIdAndDate(UUID doctorId, LocalDate date);
    Optional<TimeSlot> findById(UUID id);
    Optional<TimeSlot> findByIdForUpdate(UUID id);
    TimeSlot save(TimeSlot timeSlot);
}
