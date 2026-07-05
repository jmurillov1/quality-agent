package org.ups.citasalud.domain.port.in;

import org.ups.citasalud.domain.model.Appointment;

import java.util.UUID;

public interface BookAppointmentUseCase {
    Appointment execute(UUID patientId, UUID timeSlotId);
}
