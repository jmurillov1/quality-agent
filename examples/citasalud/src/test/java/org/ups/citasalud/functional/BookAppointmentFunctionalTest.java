package org.ups.citasalud.functional;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.ups.citasalud.adapter.out.persistence.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class BookAppointmentFunctionalTest extends FunctionalTestBase {

    @Autowired ObjectMapper objectMapper;

    @Test
    void dado_paciente_autenticado_cuando_reserva_franja_disponible_entonces_cita_confirmada()
            throws Exception {
        var doctor = persistDoctor("Dr. Carlos", "General");
        var patient = patientRepo.save(new PatientJpaEntity(UUID.randomUUID(), "Juanita", "+573001234567", null, Instant.now()));
        LocalDate date = LocalDate.now().plusDays(1);
        var slot = timeSlotRepo.save(new TimeSlotJpaEntity(
                UUID.randomUUID(), doctor.getId(), date,
                LocalTime.of(9, 0), LocalTime.of(9, 30), "AVAILABLE"
        ));

        var body = Map.of("patientId", patient.getId().toString(), "timeSlotId", slot.getId().toString());

        mockMvc.perform(post("/v1/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        mockMvc.perform(get("/v1/doctors/{id}/time-slots", doctor.getId()).param("date", date.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timeSlots[0].available").value(false));

        mockMvc.perform(get("/v1/patients/{patientId}/appointments", patient.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("CONFIRMED"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }
}
