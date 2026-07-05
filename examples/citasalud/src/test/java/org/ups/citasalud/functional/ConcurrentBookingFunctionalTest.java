package org.ups.citasalud.functional;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.ups.citasalud.adapter.out.persistence.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

class ConcurrentBookingFunctionalTest extends FunctionalTestBase {

    @Autowired ObjectMapper objectMapper;

    @Test
    void dado_dos_pacientes_cuando_reservan_misma_franja_simultaneamente_entonces_solo_una_cita_se_confirma()
            throws Exception {
        var doctor = persistDoctor("Dr. Conc", "General");
        var p1 = new PatientJpaEntity(UUID.randomUUID(), "Conc1", "+57310001", null, Instant.now());
        var p2 = new PatientJpaEntity(UUID.randomUUID(), "Conc2", "+57310002", null, Instant.now());
        patientRepo.saveAll(List.of(p1, p2));

        LocalDate date = LocalDate.now().plusDays(4);
        var slot = timeSlotRepo.save(new TimeSlotJpaEntity(
                UUID.randomUUID(), doctor.getId(), date, LocalTime.of(14, 0), LocalTime.of(14, 30), "AVAILABLE"
        ));

        AtomicInteger created = new AtomicInteger(0);
        AtomicInteger conflict = new AtomicInteger(0);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);

        Callable<Void> bookP1 = () -> {
            latch.countDown();
            latch.await();
            var body = Map.of("patientId", p1.getId().toString(), "timeSlotId", slot.getId().toString());
            MockHttpServletResponse response = mockMvc.perform(post("/v1/appointments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andReturn().getResponse();
            if (response.getStatus() == 201) created.incrementAndGet();
            if (response.getStatus() == 409) conflict.incrementAndGet();
            return null;
        };
        Callable<Void> bookP2 = () -> {
            latch.countDown();
            latch.await();
            var body = Map.of("patientId", p2.getId().toString(), "timeSlotId", slot.getId().toString());
            MockHttpServletResponse response = mockMvc.perform(post("/v1/appointments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andReturn().getResponse();
            if (response.getStatus() == 201) created.incrementAndGet();
            if (response.getStatus() == 409) conflict.incrementAndGet();
            return null;
        };

        var f1 = executor.submit(bookP1);
        var f2 = executor.submit(bookP2);
        f1.get(10, TimeUnit.SECONDS);
        f2.get(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(created.get()).isEqualTo(1);
        assertThat(conflict.get()).isEqualTo(1);
        assertThat(appointmentRepo.count()).isEqualTo(1L);
    }
}
