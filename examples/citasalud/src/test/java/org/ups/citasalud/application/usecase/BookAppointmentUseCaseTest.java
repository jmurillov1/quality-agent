package org.ups.citasalud.application.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ups.citasalud.domain.exception.TimeSlotNotAvailableException;
import org.ups.citasalud.domain.model.*;
import org.ups.citasalud.domain.port.out.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookAppointmentUseCaseTest {

    @Mock PatientRepositoryPort patientRepository;
    @Mock TimeSlotRepositoryPort timeSlotRepository;
    @Mock AppointmentRepositoryPort appointmentRepository;
    @Mock AppointmentEventPublisherPort eventPublisher;

    @InjectMocks
    BookAppointmentUseCaseImpl useCase;

    UUID patientId;
    UUID timeSlotId;
    UUID doctorId;
    Patient patient;
    TimeSlot availableSlot;

    @BeforeEach
    void setUp() {
        patientId  = UUID.randomUUID();
        timeSlotId = UUID.randomUUID();
        doctorId   = UUID.randomUUID();
        patient    = new Patient(patientId, "Juanita", "+573001234567", null, Instant.now());
        availableSlot = new TimeSlot(
                timeSlotId, doctorId,
                LocalDate.now().plusDays(1),
                LocalTime.of(9, 0), LocalTime.of(9, 30),
                TimeSlotStatus.AVAILABLE
        );
    }

    @Test
    void dado_franja_disponible_cuando_reserva_entonces_cita_confirmada_y_evento_publicado() {
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(timeSlotRepository.findByIdForUpdate(timeSlotId)).thenReturn(Optional.of(availableSlot));
        when(timeSlotRepository.save(any())).thenReturn(availableSlot);
        when(appointmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Appointment result = useCase.execute(patientId, timeSlotId);

        assertThat(result.status()).isEqualTo(AppointmentStatus.CONFIRMED);
        assertThat(result.patientId()).isEqualTo(patientId);
        assertThat(result.timeSlotId()).isEqualTo(timeSlotId);
        verify(eventPublisher).publish(any(AppointmentConfirmedEvent.class));
    }

    @Test
    void dado_franja_ocupada_cuando_reserva_entonces_lanza_TimeSlotNotAvailableException() {
        var bookedSlot = new TimeSlot(
                timeSlotId, doctorId,
                LocalDate.now().plusDays(1),
                LocalTime.of(9, 0), LocalTime.of(9, 30),
                TimeSlotStatus.BOOKED
        );
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(timeSlotRepository.findByIdForUpdate(timeSlotId)).thenReturn(Optional.of(bookedSlot));

        assertThatThrownBy(() -> useCase.execute(patientId, timeSlotId))
                .isInstanceOf(TimeSlotNotAvailableException.class);
        verify(eventPublisher, never()).publish(any());
    }
}
