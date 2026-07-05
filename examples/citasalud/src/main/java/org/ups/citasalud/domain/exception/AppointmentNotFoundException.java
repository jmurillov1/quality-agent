package org.ups.citasalud.domain.exception;

import java.util.UUID;

public class AppointmentNotFoundException extends RuntimeException {

    public AppointmentNotFoundException(UUID appointmentId) {
        super("No se encontró la cita con identificador " + appointmentId + ".");
    }
}
