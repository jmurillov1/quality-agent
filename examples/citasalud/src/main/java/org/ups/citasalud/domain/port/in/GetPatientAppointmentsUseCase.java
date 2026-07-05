package org.ups.citasalud.domain.port.in;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.ups.citasalud.domain.model.Appointment;
import org.ups.citasalud.domain.model.AppointmentStatus;

import java.util.UUID;

public interface GetPatientAppointmentsUseCase {
    Page<Appointment> execute(UUID patientId, AppointmentStatus status, Pageable pageable);
}
