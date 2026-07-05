package org.ups.citasalud.adapter.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DoctorJpaRepository extends JpaRepository<DoctorJpaEntity, UUID> {
    Page<DoctorJpaEntity> findBySpecialtyContainingIgnoreCase(String specialty, Pageable pageable);
}
