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

class WhatsAppNotificationFunctionalTest extends FunctionalTestBase {

    @Autowired ObjectMapper objectMapper;

    @Test
    void dado_cita_confirmada_cuando_se_reserva_entonces_se_dispara_notificacion_whatsapp_sin_revertir_la_cita()
            throws Exception {
        var doctor = persistDoctor("Dr. Carlos", "General");
        var patient = patientRepo.save(new PatientJpaEntity(
                UUID.randomUUID(), "Juanita", "+573001234567", null, Instant.now()));
        var slot = timeSlotRepo.save(new TimeSlotJpaEntity(
                UUID.randomUUID(), doctor.getId(), LocalDate.now().plusDays(1),
                LocalTime.of(9, 0), LocalTime.of(9, 30), "AVAILABLE"
        ));
        var body = Map.of("patientId", patient.getId().toString(), "timeSlotId", slot.getId().toString());

        String appointmentId = mockMvc.perform(post("/v1/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andReturn().getResponse().getContentAsString();
        UUID id = UUID.fromString(objectMapper.readTree(appointmentId).get("id").asText());

        await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
            var notifications = notificationRepo.findAll().stream()
                    .filter(n -> n.getAppointmentId().equals(id))
                    .toList();
            assertThat(notifications).hasSize(1);
        });

        var appointmentAfterNotification = appointmentRepo.findById(id).orElseThrow();
        assertThat(appointmentAfterNotification.getStatus()).isEqualTo("CONFIRMED");
    }
}
