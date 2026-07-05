package org.ups.citasalud.application.usecase;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.ups.citasalud.adapter.out.persistence.*;
import org.ups.citasalud.domain.port.in.BookAppointmentUseCase;
import org.ups.citasalud.domain.port.out.AppointmentEventPublisherPort;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

/**
 * Cubre el caso extremo del spec: "¿Qué pasa si el paciente pierde la conexión durante la
 * confirmación?" — la cita sólo debe registrarse si el servidor procesa la confirmación
 * completa; cualquier fallo a mitad de proceso debe revertir por completo la transacción
 * (ninguna cita parcial, la franja vuelve a quedar disponible).
 */
@SpringBootTest
class BookAppointmentUseCaseTransactionalIntegrationTest {

    @Autowired BookAppointmentUseCase bookAppointmentUseCase;
    @Autowired TimeSlotJpaRepository timeSlotRepo;
    @Autowired AppointmentJpaRepository appointmentRepo;
    @Autowired DoctorJpaRepository doctorRepo;
    @Autowired PatientJpaRepository patientRepo;
    @Autowired LocationJpaRepository locationRepo;

    @MockitoBean AppointmentEventPublisherPort eventPublisherPort;

    @Test
    void dado_fallo_a_mitad_de_confirmacion_cuando_execute_entonces_no_persiste_cita_y_franja_sigue_disponible() {
        var location = locationRepo.save(new LocationJpaEntity(UUID.randomUUID(), "Sede", "Dirección"));
        var doctor = doctorRepo.save(new DoctorJpaEntity(UUID.randomUUID(), "Dr. Test", "General", location));
        var patient = patientRepo.save(new PatientJpaEntity(UUID.randomUUID(), "Paciente Test", "+573000000000", null, Instant.now()));
        var slot = timeSlotRepo.save(new TimeSlotJpaEntity(
                UUID.randomUUID(), doctor.getId(), LocalDate.now().plusDays(1),
                LocalTime.of(11, 0), LocalTime.of(11, 30), "AVAILABLE"
        ));

        doThrow(new RuntimeException("Conexión perdida a mitad de la confirmación"))
                .when(eventPublisherPort).publish(any());

        assertThatThrownBy(() -> bookAppointmentUseCase.execute(patient.getId(), slot.getId()))
                .isInstanceOf(RuntimeException.class);

        var slotAfter = timeSlotRepo.findById(slot.getId()).orElseThrow();
        assertThat(slotAfter.getStatus()).isEqualTo("AVAILABLE");
        assertThat(appointmentRepo.findAll()).isEmpty();
    }
}
