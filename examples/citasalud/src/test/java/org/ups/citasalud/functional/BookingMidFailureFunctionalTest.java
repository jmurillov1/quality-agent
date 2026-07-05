package org.ups.citasalud.functional;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.ups.citasalud.adapter.out.persistence.*;
import org.ups.citasalud.domain.port.out.AppointmentEventPublisherPort;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Cubre el caso extremo de spec.md: "¿Qué pasa si el paciente pierde la conexión durante la
 * confirmación?" — desde la perspectiva de un fallo real a mitad del procesamiento del servidor
 * (equivalente a un timeout/corte de red durante la transacción). La cita sólo debe registrarse
 * si el servidor completa el procesamiento por entero; un fallo a mitad de camino NO debe dejar
 * una cita a medio confirmar ni una franja bloqueada sin cita asociada.
 */
class BookingMidFailureFunctionalTest extends FunctionalTestBase {

    @Autowired ObjectMapper objectMapper;

    @MockitoBean AppointmentEventPublisherPort eventPublisherPort;

    @Test
    void dado_fallo_del_servidor_a_mitad_de_confirmacion_cuando_reserva_entonces_no_persiste_cita_ni_notificacion_y_franja_queda_disponible()
            throws Exception {
        var doctor = persistDoctor("Dr. Carlos", "General");
        var patient = patientRepo.save(new PatientJpaEntity(
                UUID.randomUUID(), "Juanita", "+573001234567", null, Instant.now()));
        LocalDate date = LocalDate.now().plusDays(1);
        var slot = timeSlotRepo.save(new TimeSlotJpaEntity(
                UUID.randomUUID(), doctor.getId(), date,
                LocalTime.of(9, 0), LocalTime.of(9, 30), "AVAILABLE"
        ));
        var body = Map.of("patientId", patient.getId().toString(), "timeSlotId", slot.getId().toString());

        doThrow(new RuntimeException("Conexión perdida a mitad de la confirmación"))
                .when(eventPublisherPort).publish(any());

        mockMvc.perform(post("/v1/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"));

        // Nada de estado a medio confirmar: ni cita, ni notificación.
        assertThat(appointmentRepo.findAll()).isEmpty();
        assertThat(notificationRepo.findAll()).isEmpty();

        // La franja vuelve a estar disponible para otros pacientes.
        mockMvc.perform(get("/v1/doctors/{id}/time-slots", doctor.getId()).param("date", date.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timeSlots[0].available").value(true));
    }
}
