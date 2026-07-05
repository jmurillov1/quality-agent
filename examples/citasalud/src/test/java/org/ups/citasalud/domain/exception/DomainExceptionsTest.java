package org.ups.citasalud.domain.exception;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DomainExceptionsTest {

    @Test
    void dado_appointmentId_cuando_crea_AppointmentNotFoundException_entonces_mensaje_lo_incluye() {
        var appointmentId = UUID.randomUUID();

        var ex = new AppointmentNotFoundException(appointmentId);

        assertThat(ex.getMessage()).contains(appointmentId.toString());
    }

    @Test
    void dado_patientId_cuando_crea_PatientNotFoundException_entonces_mensaje_lo_incluye() {
        var patientId = UUID.randomUUID();

        var ex = new PatientNotFoundException(patientId);

        assertThat(ex.getMessage()).contains(patientId.toString());
    }
}
