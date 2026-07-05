package org.ups.citasalud.domain.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record AppointmentConfirmedEvent(
        UUID appointmentId,
        UUID patientId,
        UUID doctorId,
        LocalDate date,
        LocalTime startTime
) {}
