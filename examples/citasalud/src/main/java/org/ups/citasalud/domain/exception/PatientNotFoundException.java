package org.ups.citasalud.domain.exception;

import java.util.UUID;

public class PatientNotFoundException extends RuntimeException {

    public PatientNotFoundException(UUID patientId) {
        super("No se encontró el paciente con identificador " + patientId + ".");
    }
}
