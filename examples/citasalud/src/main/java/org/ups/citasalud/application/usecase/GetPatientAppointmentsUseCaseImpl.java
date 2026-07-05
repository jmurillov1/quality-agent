package org.ups.citasalud.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.ups.citasalud.domain.model.Appointment;
import org.ups.citasalud.domain.model.AppointmentStatus;
import org.ups.citasalud.domain.port.in.GetPatientAppointmentsUseCase;
import org.ups.citasalud.domain.port.out.AppointmentRepositoryPort;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetPatientAppointmentsUseCaseImpl implements GetPatientAppointmentsUseCase {

    private final AppointmentRepositoryPort appointmentRepository;

    @Override
    public Page<Appointment> execute(UUID patientId, AppointmentStatus status, Pageable pageable) {
        return appointmentRepository.findByPatientId(patientId, status, pageable);
    }
}
