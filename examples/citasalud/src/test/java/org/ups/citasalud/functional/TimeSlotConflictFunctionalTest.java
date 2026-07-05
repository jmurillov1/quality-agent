package org.ups.citasalud.functional;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.ups.citasalud.adapter.out.persistence.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TimeSlotConflictFunctionalTest extends FunctionalTestBase {

    @Autowired ObjectMapper objectMapper;

    @Test
    void dado_franja_ocupada_cuando_otro_paciente_intenta_reservar_entonces_409_time_slot_not_available()
            throws Exception {
        var doctor = persistDoctor("Dr. A", "General");
        var p1 = new PatientJpaEntity(UUID.randomUUID(), "P1", "+5730001", null, Instant.now());
        var p2 = new PatientJpaEntity(UUID.randomUUID(), "P2", "+5730002", null, Instant.now());
        patientRepo.saveAll(List.of(p1, p2));

        LocalDate date = LocalDate.now().plusDays(2);
        var slot = timeSlotRepo.save(new TimeSlotJpaEntity(
                UUID.randomUUID(), doctor.getId(), date, LocalTime.of(10, 0), LocalTime.of(10, 30), "AVAILABLE"
        ));

        mockMvc.perform(post("/v1/appointments").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("patientId", p1.getId().toString(), "timeSlotId", slot.getId().toString()))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/v1/appointments").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("patientId", p2.getId().toString(), "timeSlotId", slot.getId().toString()))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("TIME_SLOT_NOT_AVAILABLE"));
    }

    @Test
    void dado_todas_franjas_ocupadas_cuando_consulta_disponibilidad_entonces_todas_available_false()
            throws Exception {
        var doctor = persistDoctor("Dr. B", "General");
        LocalDate date = LocalDate.now().plusDays(3);
        timeSlotRepo.save(new TimeSlotJpaEntity(
                UUID.randomUUID(), doctor.getId(), date, LocalTime.of(11, 0), LocalTime.of(11, 30), "BOOKED"
        ));

        mockMvc.perform(get("/v1/doctors/{id}/time-slots", doctor.getId()).param("date", date.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timeSlots[0].available").value(false));
    }
}
