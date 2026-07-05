package org.ups.citasalud.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.ups.citasalud.domain.model.Doctor;
import org.ups.citasalud.domain.port.out.DoctorRepositoryPort;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DoctorJpaAdapter implements DoctorRepositoryPort {

    private final DoctorJpaRepository repository;

    @Override
    public Optional<Doctor> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Page<Doctor> findBySpecialty(String specialty, Pageable pageable) {
        if (specialty == null || specialty.isBlank()) {
            return repository.findAll(pageable).map(this::toDomain);
        }
        return repository.findBySpecialtyContainingIgnoreCase(specialty, pageable).map(this::toDomain);
    }

    private Doctor toDomain(DoctorJpaEntity e) {
        return new Doctor(
                e.getId(),
                e.getName(),
                e.getSpecialty(),
                e.getLocation().getId()
        );
    }
}
