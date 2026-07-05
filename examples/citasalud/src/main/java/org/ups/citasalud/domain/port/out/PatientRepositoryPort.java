package org.ups.citasalud.domain.port.out;

import org.ups.citasalud.domain.model.Patient;

import java.util.Optional;
import java.util.UUID;

public interface PatientRepositoryPort {
    Optional<Patient> findById(UUID id);
}
