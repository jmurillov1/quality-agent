package org.ups.citasalud.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ups.citasalud.domain.model.TimeSlot;
import org.ups.citasalud.domain.port.in.CheckAvailabilityUseCase;
import org.ups.citasalud.domain.port.out.TimeSlotRepositoryPort;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CheckAvailabilityUseCaseImpl implements CheckAvailabilityUseCase {

    private final TimeSlotRepositoryPort timeSlotRepository;

    @Override
    public List<TimeSlot> execute(UUID doctorId, LocalDate date) {
        return timeSlotRepository.findByDoctorIdAndDate(doctorId, date);
    }
}
