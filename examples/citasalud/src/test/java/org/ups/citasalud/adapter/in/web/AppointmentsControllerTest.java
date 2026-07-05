package org.ups.citasalud.adapter.in.web;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.ups.citasalud.domain.exception.AppointmentNotFoundException;
import org.ups.citasalud.domain.model.Appointment;
import org.ups.citasalud.domain.model.AppointmentStatus;
import org.ups.citasalud.domain.model.Doctor;
import org.ups.citasalud.domain.model.TimeSlot;
import org.ups.citasalud.domain.model.TimeSlotStatus;
import org.ups.citasalud.domain.port.in.BookAppointmentUseCase;
import org.ups.citasalud.domain.port.in.GetPatientAppointmentsUseCase;
import org.ups.citasalud.domain.port.out.AppointmentRepositoryPort;
import org.ups.citasalud.domain.port.out.DoctorRepositoryPort;
import org.ups.citasalud.domain.port.out.TimeSlotRepositoryPort;
import org.ups.citasalud.generated.model.BookAppointmentRequest;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {AppointmentsController.class, GlobalExceptionHandler.class})
class AppointmentsControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean BookAppointmentUseCase bookAppointmentUseCase;
    @MockitoBean GetPatientAppointmentsUseCase getPatientAppointmentsUseCase;
    @MockitoBean AppointmentRepositoryPort appointmentRepository;
    @MockitoBean TimeSlotRepositoryPort timeSlotRepository;
    @MockitoBean DoctorRepositoryPort doctorRepository;

    @Test
    void dado_request_con_patientId_nulo_cuando_book_appointment_entonces_400_validation_failed()
            throws Exception {
        var body = Map.of("timeSlotId", "d1000000-0000-0000-0000-000000000001");

        mockMvc.perform(post("/v1/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @ExtendWith(MockitoExtension.class)
    static class UnitLevel {

        @Mock BookAppointmentUseCase bookAppointmentUseCase;
        @Mock GetPatientAppointmentsUseCase getPatientAppointmentsUseCase;
        @Mock AppointmentRepositoryPort appointmentRepository;
        @Mock TimeSlotRepositoryPort timeSlotRepository;
        @Mock DoctorRepositoryPort doctorRepository;

        @InjectMocks
        AppointmentsController controller;

        @Test
        void dado_franja_disponible_cuando_book_appointment_entonces_retorna_201_con_cita() {
            var patientId = UUID.randomUUID();
            var timeSlotId = UUID.randomUUID();
            var appointment = new Appointment(
                    UUID.randomUUID(), patientId, UUID.randomUUID(),
                    timeSlotId, AppointmentStatus.CONFIRMED, Instant.now()
            );
            when(bookAppointmentUseCase.execute(patientId, timeSlotId)).thenReturn(appointment);
            when(timeSlotRepository.findById(timeSlotId)).thenReturn(Optional.empty());
            when(doctorRepository.findById(any())).thenReturn(Optional.empty());

            var response = controller.bookAppointment(new BookAppointmentRequest(patientId, timeSlotId));

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody().getPatientId()).isEqualTo(patientId);
        }

        @Test
        void dado_cita_existente_cuando_get_appointment_entonces_retorna_200_con_detalle() {
            var appointmentId = UUID.randomUUID();
            var doctorId = UUID.randomUUID();
            var timeSlotId = UUID.randomUUID();
            var appointment = new Appointment(
                    appointmentId, UUID.randomUUID(), doctorId,
                    timeSlotId, AppointmentStatus.CONFIRMED, Instant.now()
            );
            var timeSlot = new TimeSlot(
                    timeSlotId, doctorId, LocalDate.now().plusDays(1),
                    LocalTime.of(9, 0), LocalTime.of(9, 30), TimeSlotStatus.BOOKED
            );
            var doctor = new Doctor(doctorId, "Dr. Test", "General", UUID.randomUUID());
            when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
            when(timeSlotRepository.findById(timeSlotId)).thenReturn(Optional.of(timeSlot));
            when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));

            var response = controller.getAppointment(appointmentId);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getId()).isEqualTo(appointmentId);
            assertThat(response.getBody().getDoctor().getName()).isEqualTo("Dr. Test");
        }

        @Test
        void dado_cita_inexistente_cuando_get_appointment_entonces_lanza_AppointmentNotFoundException() {
            var appointmentId = UUID.randomUUID();
            when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.empty());

            org.assertj.core.api.Assertions.assertThatThrownBy(() -> controller.getAppointment(appointmentId))
                    .isInstanceOf(AppointmentNotFoundException.class);
        }

        @Test
        void dado_paciente_con_citas_cuando_list_patient_appointments_entonces_retorna_pagina() {
            var patientId = UUID.randomUUID();
            var appointment = new Appointment(
                    UUID.randomUUID(), patientId, UUID.randomUUID(),
                    UUID.randomUUID(), AppointmentStatus.CONFIRMED, Instant.now()
            );
            var page = new PageImpl<>(List.of(appointment), PageRequest.of(0, 20), 1);
            when(getPatientAppointmentsUseCase.execute(eq(patientId), any(), any()))
                    .thenReturn(page);
            when(timeSlotRepository.findById(any())).thenReturn(Optional.empty());
            when(doctorRepository.findById(any())).thenReturn(Optional.empty());

            var response = controller.listPatientAppointments(patientId, null, null, null);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getContent()).hasSize(1);
            assertThat(response.getBody().getTotalElements()).isEqualTo(1);
        }

        @Test
        void dado_status_filtro_cuando_list_patient_appointments_entonces_filtra_por_estado() {
            var patientId = UUID.randomUUID();
            var page = new PageImpl<Appointment>(List.of(), PageRequest.of(1, 10), 0);
            when(getPatientAppointmentsUseCase.execute(
                    eq(patientId),
                    eq(AppointmentStatus.CANCELLED),
                    any()))
                    .thenReturn(page);

            var response = controller.listPatientAppointments(
                    patientId, org.ups.citasalud.generated.model.AppointmentStatus.CANCELLED, 1, 10);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getContent()).isEmpty();
        }
    }
}
