package org.ups.citasalud.domain.exception;

import java.util.UUID;

public class TimeSlotNotAvailableException extends RuntimeException {

    public TimeSlotNotAvailableException(UUID timeSlotId) {
        super("La franja horaria " + timeSlotId + " no está disponible para reserva.");
    }
}
