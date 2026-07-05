package org.ups.citasalud.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PatientJpaRepository extends JpaRepository<PatientJpaEntity, UUID> {
}
