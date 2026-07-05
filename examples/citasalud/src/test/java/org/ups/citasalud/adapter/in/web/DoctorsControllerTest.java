package org.ups.citasalud.adapter.in.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.ups.citasalud.domain.model.Doctor;
import org.ups.citasalud.domain.model.TimeSlot;
import org.ups.citasalud.domain.model.TimeSlotStatus;
import org.ups.citasalud.domain.port.in.CheckAvailabilityUseCase;
import org.ups.citasalud.domain.port.out.DoctorRepositoryPort;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.OK;

@ExtendWith(MockitoExtension.class)
class DoctorsControllerTest {

    @Mock DoctorRepositoryPort doctorRepository;
    @Mock CheckAvailabilityUseCase checkAvailabilityUseCase;

    @InjectMocks
    DoctorsController controller;

    @Test
    void dado_medicos_registrados_cuando_list_doctors_entonces_retorna_pagina() {
        var doctor = new Doctor(UUID.randomUUID(), "Dr. Test", "Medicina General", UUID.randomUUID());
        var page = new PageImpl<>(List.of(doctor), PageRequest.of(0, 20), 1);
        when(doctorRepository.findBySpecialty("Medicina General", PageRequest.of(0, 20))).thenReturn(page);

        var response = controller.listDoctors("Medicina General", null, null);

        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getContent().get(0).getName()).isEqualTo("Dr. Test");
    }

    @Test
    void dado_medico_con_franjas_cuando_check_availability_entonces_retorna_disponibilidad() {
        var doctorId = UUID.randomUUID();
        var date = LocalDate.now().plusDays(1);
        var slot = new TimeSlot(
                UUID.randomUUID(), doctorId, date,
                LocalTime.of(9, 0), LocalTime.of(9, 30), TimeSlotStatus.AVAILABLE
        );
        when(checkAvailabilityUseCase.execute(doctorId, date)).thenReturn(List.of(slot));

        var response = controller.checkAvailability(doctorId, date);

        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody().getDoctorId()).isEqualTo(doctorId);
        assertThat(response.getBody().getTimeSlots()).hasSize(1);
        assertThat(response.getBody().getTimeSlots().get(0).getAvailable()).isTrue();
    }
}
