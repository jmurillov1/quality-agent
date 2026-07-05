package org.ups.citasalud.adapter.in.web;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.ups.citasalud.domain.exception.AppointmentNotFoundException;
import org.ups.citasalud.domain.model.Appointment;
import org.ups.citasalud.domain.model.TimeSlot;
import org.ups.citasalud.domain.port.in.BookAppointmentUseCase;
import org.ups.citasalud.domain.port.in.GetPatientAppointmentsUseCase;
import org.ups.citasalud.domain.port.out.AppointmentRepositoryPort;
import org.ups.citasalud.domain.port.out.DoctorRepositoryPort;
import org.ups.citasalud.domain.port.out.TimeSlotRepositoryPort;
import org.ups.citasalud.generated.api.AppointmentsApi;
import org.ups.citasalud.generated.model.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class AppointmentsController implements AppointmentsApi {

    private final BookAppointmentUseCase bookAppointmentUseCase;
    private final GetPatientAppointmentsUseCase getPatientAppointmentsUseCase;
    private final AppointmentRepositoryPort appointmentRepository;
    private final TimeSlotRepositoryPort timeSlotRepository;
    private final DoctorRepositoryPort doctorRepository;

    @Override
    public ResponseEntity<AppointmentResponse> bookAppointment(@Valid BookAppointmentRequest request) {
        Appointment appointment = bookAppointmentUseCase.execute(
                request.getPatientId(),
                request.getTimeSlotId()
        );
        return ResponseEntity
                .created(URI.create("/api/v1/appointments/" + appointment.id()))
                .body(toAppointmentResponse(appointment));
    }

    @Override
    public ResponseEntity<AppointmentResponse> getAppointment(UUID appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException(appointmentId));
        return ResponseEntity.ok(toAppointmentResponse(appointment));
    }

    @Override
    public ResponseEntity<PagedAppointments> listPatientAppointments(
            UUID patientId, org.ups.citasalud.generated.model.AppointmentStatus status,
            Integer page, Integer size) {
        var domainStatus = (status != null)
                ? org.ups.citasalud.domain.model.AppointmentStatus.valueOf(status.name())
                : null;
        var pageable = PageRequest.of(
                page != null ? page : 0,
                size != null ? size : 20,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        var pageResult = getPatientAppointmentsUseCase.execute(patientId, domainStatus, pageable);
        var response = new PagedAppointments();
        response.setContent(pageResult.getContent().stream().map(this::toAppointmentResponse).toList());
        response.setCurrentPage(pageResult.getNumber());
        response.setTotalPages(pageResult.getTotalPages());
        response.setTotalElements((int) pageResult.getTotalElements());
        return ResponseEntity.ok(response);
    }

    private AppointmentResponse toAppointmentResponse(Appointment a) {
        var response = new AppointmentResponse();
        response.setId(a.id());
        response.setPatientId(a.patientId());
        response.setStatus(org.ups.citasalud.generated.model.AppointmentStatus.valueOf(a.status().name()));
        response.setCreatedAt(java.time.LocalDateTime.ofInstant(a.createdAt(), java.time.ZoneOffset.UTC));

        timeSlotRepository.findById(a.timeSlotId()).ifPresent(ts -> {
            response.setTimeSlot(toTimeSlotResponse(ts));
        });
        doctorRepository.findById(a.doctorId()).ifPresent(doc -> {
            var doctorSummary = new DoctorSummary();
            doctorSummary.setId(doc.id());
            doctorSummary.setName(doc.name());
            doctorSummary.setSpecialty(doc.specialty());
            response.setDoctor(doctorSummary);
        });
        return response;
    }

    private TimeSlotResponse toTimeSlotResponse(TimeSlot ts) {
        var r = new TimeSlotResponse();
        r.setId(ts.getId());
        r.setStartTime(ts.getStartTime().toString());
        r.setEndTime(ts.getEndTime().toString());
        r.setAvailable(ts.getStatus() == org.ups.citasalud.domain.model.TimeSlotStatus.AVAILABLE);
        return r;
    }
}
