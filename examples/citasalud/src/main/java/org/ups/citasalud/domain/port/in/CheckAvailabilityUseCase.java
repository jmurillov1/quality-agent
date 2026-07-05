package org.ups.citasalud.domain.port.in;

import org.ups.citasalud.domain.model.TimeSlot;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface CheckAvailabilityUseCase {
    List<TimeSlot> execute(UUID doctorId, LocalDate date);
}
