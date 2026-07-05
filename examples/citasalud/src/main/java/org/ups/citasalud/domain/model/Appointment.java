package org.ups.citasalud.domain.model;

import java.time.Instant;
import java.util.UUID;

public record Appointment(
        UUID id,
        UUID patientId,
        UUID doctorId,
        UUID timeSlotId,
        AppointmentStatus status,
        Instant createdAt
) {}
