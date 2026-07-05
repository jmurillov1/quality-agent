package org.ups.citasalud.adapter.in.web;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.ups.citasalud.domain.model.TimeSlotStatus;
import org.ups.citasalud.domain.port.in.CheckAvailabilityUseCase;
import org.ups.citasalud.domain.port.out.DoctorRepositoryPort;
import org.ups.citasalud.generated.api.DoctorsApi;
import org.ups.citasalud.generated.model.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class DoctorsController implements DoctorsApi {

    private final DoctorRepositoryPort doctorRepository;
    private final CheckAvailabilityUseCase checkAvailabilityUseCase;

    @Override
    public ResponseEntity<PagedDoctors> listDoctors(String specialty, Integer page, Integer size) {
        var pageable = PageRequest.of(
                page != null ? page : 0,
                size != null ? size : 20
        );
        var result = doctorRepository.findBySpecialty(specialty, pageable);
        var response = new PagedDoctors();
        response.setContent(result.getContent().stream().map(doc -> {
            var ds = new DoctorSummary();
            ds.setId(doc.id());
            ds.setName(doc.name());
            ds.setSpecialty(doc.specialty());
            return ds;
        }).toList());
        response.setCurrentPage(result.getNumber());
        response.setTotalPages(result.getTotalPages());
        response.setTotalElements((int) result.getTotalElements());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<DoctorAvailability> checkAvailability(UUID doctorId, LocalDate date) {
        var timeSlots = checkAvailabilityUseCase.execute(doctorId, date);
        var response = new DoctorAvailability();
        response.setDoctorId(doctorId);
        response.setDate(date);
        response.setTimeSlots(timeSlots.stream().map(ts -> {
            var r = new TimeSlotResponse();
            r.setId(ts.getId());
            r.setStartTime(ts.getStartTime().toString());
            r.setEndTime(ts.getEndTime().toString());
            r.setAvailable(ts.getStatus() == TimeSlotStatus.AVAILABLE);
            return r;
        }).toList());
        return ResponseEntity.ok(response);
    }
}
