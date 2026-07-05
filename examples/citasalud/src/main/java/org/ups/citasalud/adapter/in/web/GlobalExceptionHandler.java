package org.ups.citasalud.adapter.in.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.ups.citasalud.domain.exception.AppointmentNotFoundException;
import org.ups.citasalud.domain.exception.PatientNotFoundException;
import org.ups.citasalud.domain.exception.TimeSlotNotAvailableException;
import org.ups.citasalud.generated.model.ErrorResponse;

import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(TimeSlotNotAvailableException.class)
    public ResponseEntity<ErrorResponse> handleTimeSlotNotAvailable(TimeSlotNotAvailableException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(error("TIME_SLOT_NOT_AVAILABLE",
                        "La franja horaria seleccionada ya fue reservada. Por favor elija otra."));
    }

    @ExceptionHandler(AppointmentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAppointmentNotFound(AppointmentNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(error("RESOURCE_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(PatientNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePatientNotFound(PatientNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(error("PATIENT_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(error("VALIDATION_FAILED", "Solicitud inválida: " + detail));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(error("VALIDATION_FAILED", "Solicitud inválida: " + ex.getMessage()));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResource(NoResourceFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(error("NOT_FOUND", "Recurso no encontrado: " + ex.getResourcePath()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error("INTERNAL_ERROR", "Ocurrió un error inesperado. Por favor intente más tarde."));
    }

    private ErrorResponse error(String code, String message) {
        var err = new ErrorResponse();
        err.setCode(code);
        err.setMessage(message);
        return err;
    }
}
