package org.ups.citasalud.domain.port.out;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.ups.citasalud.domain.model.Appointment;
import org.ups.citasalud.domain.model.AppointmentStatus;

import java.util.Optional;
import java.util.UUID;

public interface AppointmentRepositoryPort {
    Appointment save(Appointment appointment);
    Optional<Appointment> findById(UUID id);
    Page<Appointment> findByPatientId(UUID patientId, AppointmentStatus status, Pageable pageable);
}
