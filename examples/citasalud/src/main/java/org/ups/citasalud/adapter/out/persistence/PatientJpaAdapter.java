package org.ups.citasalud.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.ups.citasalud.domain.model.Patient;
import org.ups.citasalud.domain.port.out.PatientRepositoryPort;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PatientJpaAdapter implements PatientRepositoryPort {

    private final PatientJpaRepository repository;

    @Override
    public Optional<Patient> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    private Patient toDomain(PatientJpaEntity e) {
        return new Patient(
                e.getId(),
                e.getName(),
                e.getPhone(),
                e.getEmail(),
                e.getRegistrationDate()
        );
    }
}
