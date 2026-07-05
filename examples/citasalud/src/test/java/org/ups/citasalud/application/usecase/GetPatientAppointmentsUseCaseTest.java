package org.ups.citasalud.application.usecase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.ups.citasalud.domain.model.Appointment;
import org.ups.citasalud.domain.model.AppointmentStatus;
import org.ups.citasalud.domain.port.out.AppointmentRepositoryPort;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetPatientAppointmentsUseCaseTest {

    @Mock AppointmentRepositoryPort appointmentRepository;

    @InjectMocks
    GetPatientAppointmentsUseCaseImpl useCase;

    @Test
    void dado_paciente_con_citas_cuando_consulta_historial_entonces_retorna_pagina() {
        UUID patientId = UUID.randomUUID();
        var pageable = PageRequest.of(0, 20);
        var appointment = new Appointment(
                UUID.randomUUID(), patientId, UUID.randomUUID(),
                UUID.randomUUID(), AppointmentStatus.CONFIRMED, Instant.now()
        );
        var page = new PageImpl<>(List.of(appointment), pageable, 1);
        when(appointmentRepository.findByPatientId(patientId, AppointmentStatus.CONFIRMED, pageable))
                .thenReturn(page);

        var result = useCase.execute(patientId, AppointmentStatus.CONFIRMED, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).patientId()).isEqualTo(patientId);
    }
}
