package org.ups.citasalud.functional;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.ups.citasalud.adapter.out.persistence.*;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Cubre el caso extremo de spec.md: "¿Qué pasa si el paciente pierde la conexión durante la
 * confirmación?" — desde la perspectiva del cliente que no recibió la respuesta a tiempo
 * (timeout percibido) y reintenta la misma reserva. El servidor ya había completado el
 * procesamiento del primer intento, así que el reintento NO debe crear una segunda cita ni
 * disparar una segunda notificación WhatsApp; sólo debe existir un único registro de cada uno.
 */
class BookingRetryAfterTimeoutFunctionalTest extends FunctionalTestBase {

    @Autowired ObjectMapper objectMapper;

    @Test
    void dado_reserva_ya_procesada_por_el_servidor_cuando_cliente_reintenta_tras_timeout_percibido_entonces_no_hay_cita_ni_notificacion_duplicada()
            throws Exception {
        var doctor = persistDoctor("Dr. Carlos", "General");
        var patient = patientRepo.save(new PatientJpaEntity(
                UUID.randomUUID(), "Juanita", "+573001234567", null, Instant.now()));
        var slot = timeSlotRepo.save(new TimeSlotJpaEntity(
                UUID.randomUUID(), doctor.getId(), LocalDate.now().plusDays(1),
                LocalTime.of(9, 0), LocalTime.of(9, 30), "AVAILABLE"
        ));
        var body = Map.of("patientId", patient.getId().toString(), "timeSlotId", slot.getId().toString());
        var requestJson = objectMapper.writeValueAsString(body);

        // Primer intento: el servidor procesa completamente la confirmación.
        mockMvc.perform(post("/v1/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        // El cliente, creyendo que hubo un timeout de red, reintenta la MISMA reserva.
        mockMvc.perform(post("/v1/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("TIME_SLOT_NOT_AVAILABLE"));

        // Consistencia: exactamente una cita, sin importar el reintento.
        assertThat(appointmentRepo.findAll()).hasSize(1);

        // Consistencia: exactamente una notificación WhatsApp, nunca dos.
        await().atMost(Duration.ofSeconds(15)).untilAsserted(() ->
                assertThat(notificationRepo.findAll()).hasSize(1)
        );
    }
}
