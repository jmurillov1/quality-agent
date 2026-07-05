package org.ups.citasalud.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.ups.citasalud.domain.model.Appointment;
import org.ups.citasalud.domain.model.AppointmentStatus;
import org.ups.citasalud.domain.port.out.AppointmentRepositoryPort;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AppointmentJpaAdapter implements AppointmentRepositoryPort {

    private final AppointmentJpaRepository repository;

    @Override
    public Appointment save(Appointment appointment) {
        var entity = toEntity(appointment);
        return toDomain(repository.saveAndFlush(entity));
    }

    @Override
    public Optional<Appointment> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Page<Appointment> findByPatientId(UUID patientId, AppointmentStatus status, Pageable pageable) {
        Page<AppointmentJpaEntity> page = (status != null)
                ? repository.findByPatientIdAndStatus(patientId, status.name(), pageable)
                : repository.findByPatientId(patientId, pageable);
        return page.map(this::toDomain);
    }

    private Appointment toDomain(AppointmentJpaEntity e) {
        return new Appointment(
                e.getId(),
                e.getPatientId(),
                e.getDoctorId(),
                e.getTimeSlotId(),
                AppointmentStatus.valueOf(e.getStatus()),
                e.getCreatedAt()
        );
    }

    private AppointmentJpaEntity toEntity(Appointment a) {
        return new AppointmentJpaEntity(
                a.id(),
                a.patientId(),
                a.doctorId(),
                a.timeSlotId(),
                a.status().name(),
                a.createdAt()
        );
    }
}
