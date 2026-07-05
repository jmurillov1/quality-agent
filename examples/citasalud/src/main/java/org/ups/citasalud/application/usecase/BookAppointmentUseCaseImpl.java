package org.ups.citasalud.application.usecase;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ups.citasalud.domain.exception.PatientNotFoundException;
import org.ups.citasalud.domain.exception.TimeSlotNotAvailableException;
import org.ups.citasalud.domain.model.Appointment;
import org.ups.citasalud.domain.model.AppointmentConfirmedEvent;
import org.ups.citasalud.domain.model.AppointmentStatus;
import org.ups.citasalud.domain.model.TimeSlot;
import org.ups.citasalud.domain.port.in.BookAppointmentUseCase;
import org.ups.citasalud.domain.port.out.AppointmentEventPublisherPort;
import org.ups.citasalud.domain.port.out.AppointmentRepositoryPort;
import org.ups.citasalud.domain.port.out.PatientRepositoryPort;
import org.ups.citasalud.domain.port.out.TimeSlotRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookAppointmentUseCaseImpl implements BookAppointmentUseCase {

    private final PatientRepositoryPort patientRepository;
    private final TimeSlotRepositoryPort timeSlotRepository;
    private final AppointmentRepositoryPort appointmentRepository;
    private final AppointmentEventPublisherPort eventPublisher;

    @Override
    @Transactional
    public Appointment execute(UUID patientId, UUID timeSlotId) {
        patientRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException(patientId));

        TimeSlot timeSlot = timeSlotRepository.findByIdForUpdate(timeSlotId)
                .orElseThrow(() -> new TimeSlotNotAvailableException(timeSlotId));

        timeSlot.book();
        timeSlotRepository.save(timeSlot);

        Appointment appointment = new Appointment(
                UUID.randomUUID(),
                patientId,
                timeSlot.getDoctorId(),
                timeSlotId,
                AppointmentStatus.CONFIRMED,
                Instant.now()
        );
        Appointment saved = appointmentRepository.save(appointment);

        eventPublisher.publish(new AppointmentConfirmedEvent(
                saved.id(),
                patientId,
                timeSlot.getDoctorId(),
                timeSlot.getDate(),
                timeSlot.getStartTime()
        ));

        return saved;
    }
}
