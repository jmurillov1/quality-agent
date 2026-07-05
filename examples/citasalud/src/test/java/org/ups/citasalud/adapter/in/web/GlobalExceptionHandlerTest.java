package org.ups.citasalud.adapter.in.web;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.http.HttpMethod;
import org.ups.citasalud.domain.exception.AppointmentNotFoundException;
import org.ups.citasalud.domain.exception.PatientNotFoundException;
import org.ups.citasalud.domain.exception.TimeSlotNotAvailableException;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void dado_TimeSlotNotAvailableException_cuando_maneja_entonces_retorna_409() {
        var response = handler.handleTimeSlotNotAvailable(new TimeSlotNotAvailableException(UUID.randomUUID()));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getCode()).isEqualTo("TIME_SLOT_NOT_AVAILABLE");
    }

    @Test
    void dado_AppointmentNotFoundException_cuando_maneja_entonces_retorna_404() {
        var response = handler.handleAppointmentNotFound(new AppointmentNotFoundException(UUID.randomUUID()));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getCode()).isEqualTo("RESOURCE_NOT_FOUND");
    }

    @Test
    void dado_PatientNotFoundException_cuando_maneja_entonces_retorna_404() {
        var response = handler.handlePatientNotFound(new PatientNotFoundException(UUID.randomUUID()));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getCode()).isEqualTo("PATIENT_NOT_FOUND");
    }

    @Test
    void dado_MethodArgumentNotValidException_cuando_maneja_entonces_retorna_400() {
        var bindException = new BindException(new Object(), "target");
        bindException.addError(new FieldError("target", "patientId", "no debe ser nulo"));
        var ex = new MethodArgumentNotValidException(mock(org.springframework.core.MethodParameter.class), bindException);

        var response = handler.handleValidation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getCode()).isEqualTo("VALIDATION_FAILED");
        assertThat(response.getBody().getMessage()).contains("no debe ser nulo");
    }

    @Test
    void dado_ConstraintViolationException_cuando_maneja_entonces_retorna_400() {
        var response = handler.handleConstraintViolation(new ConstraintViolationException("inválido", java.util.Set.of()));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getCode()).isEqualTo("VALIDATION_FAILED");
    }

    @Test
    void dado_NoResourceFoundException_cuando_maneja_entonces_retorna_404() {
        var response = handler.handleNoResource(new NoResourceFoundException(HttpMethod.GET, "/no-existe", "/no-existe"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getCode()).isEqualTo("NOT_FOUND");
    }

    @Test
    void dado_excepcion_generica_cuando_maneja_entonces_retorna_500() {
        var response = handler.handleGeneric(new RuntimeException("boom"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getCode()).isEqualTo("INTERNAL_ERROR");
    }
}
