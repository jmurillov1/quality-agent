package org.ups.citasalud.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.ups.citasalud.domain.model.TimeSlot;
import org.ups.citasalud.domain.model.TimeSlotStatus;
import org.ups.citasalud.domain.port.out.TimeSlotRepositoryPort;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TimeSlotJpaAdapter implements TimeSlotRepositoryPort {

    private final TimeSlotJpaRepository repository;

    @Override
    public List<TimeSlot> findByDoctorIdAndDate(UUID doctorId, LocalDate date) {
        return repository.findByDoctorIdAndDate(doctorId, date)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<TimeSlot> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<TimeSlot> findByIdForUpdate(UUID id) {
        return repository.findByIdForUpdate(id).map(this::toDomain);
    }

    @Override
    public TimeSlot save(TimeSlot timeSlot) {
        var entity = toEntity(timeSlot);
        return toDomain(repository.save(entity));
    }

    private TimeSlot toDomain(TimeSlotJpaEntity e) {
        return new TimeSlot(
                e.getId(),
                e.getDoctorId(),
                e.getDate(),
                e.getStartTime(),
                e.getEndTime(),
                TimeSlotStatus.valueOf(e.getStatus())
        );
    }

    private TimeSlotJpaEntity toEntity(TimeSlot ts) {
        return new TimeSlotJpaEntity(
                ts.getId(),
                ts.getDoctorId(),
                ts.getDate(),
                ts.getStartTime(),
                ts.getEndTime(),
                ts.getStatus().name()
        );
    }
}
