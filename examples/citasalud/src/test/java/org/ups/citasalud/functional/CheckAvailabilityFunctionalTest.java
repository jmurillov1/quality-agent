package org.ups.citasalud.functional;

import org.junit.jupiter.api.Test;
import org.ups.citasalud.adapter.out.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CheckAvailabilityFunctionalTest extends FunctionalTestBase {

    @Test
    void dado_medico_con_franjas_cuando_consulta_disponibilidad_entonces_retorna_franjas_con_estado_correcto()
            throws Exception {
        var doctor = persistDoctor("Dr. Test", "General");
        LocalDate date = LocalDate.now().plusDays(1);
        timeSlotRepo.save(new TimeSlotJpaEntity(
                UUID.randomUUID(), doctor.getId(), date,
                LocalTime.of(9, 0), LocalTime.of(9, 30), "AVAILABLE"
        ));

        mockMvc.perform(get("/v1/doctors/{id}/time-slots", doctor.getId()).param("date", date.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timeSlots[0].available").value(true));
    }
}
