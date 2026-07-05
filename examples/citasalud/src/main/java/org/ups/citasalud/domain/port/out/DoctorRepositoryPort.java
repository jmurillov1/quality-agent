package org.ups.citasalud.domain.port.out;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.ups.citasalud.domain.model.Doctor;

import java.util.Optional;
import java.util.UUID;

public interface DoctorRepositoryPort {
    Optional<Doctor> findById(UUID id);
    Page<Doctor> findBySpecialty(String specialty, Pageable pageable);
}
