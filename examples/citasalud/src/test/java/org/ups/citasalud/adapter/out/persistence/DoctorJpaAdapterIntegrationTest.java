package org.ups.citasalud.adapter.out.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(DoctorJpaAdapter.class)
class DoctorJpaAdapterIntegrationTest {

    @Autowired DoctorJpaAdapter adapter;
    @Autowired DoctorJpaRepository doctorRepository;
    @Autowired LocationJpaRepository locationRepository;

    @Test
    void dado_medico_existente_cuando_busca_por_id_entonces_lo_retorna() {
        var doctor = persistDoctor("Dr. Existente", "Cardiología");

        var found = adapter.findById(doctor.getId());

        assertThat(found).isPresent();
        assertThat(found.get().name()).isEqualTo("Dr. Existente");
        assertThat(found.get().specialty()).isEqualTo("Cardiología");
    }

    @Test
    void dado_medico_inexistente_cuando_busca_por_id_entonces_retorna_vacio() {
        var found = adapter.findById(UUID.randomUUID());

        assertThat(found).isEmpty();
    }

    @Test
    void dado_especialidad_cuando_busca_por_especialidad_entonces_filtra_resultados() {
        persistDoctor("Dr. Uno", "Pediatría");
        persistDoctor("Dr. Dos", "Cardiología");

        var result = adapter.findBySpecialty("Pediatría", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("Dr. Uno");
    }

    @Test
    void dado_especialidad_nula_cuando_busca_por_especialidad_entonces_retorna_todos() {
        persistDoctor("Dr. Uno", "Pediatría");
        persistDoctor("Dr. Dos", "Cardiología");

        var result = adapter.findBySpecialty(null, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
    }

    private DoctorJpaEntity persistDoctor(String name, String specialty) {
        var location = locationRepository.save(new LocationJpaEntity(UUID.randomUUID(), "Sede", "Dirección"));
        return doctorRepository.save(new DoctorJpaEntity(UUID.randomUUID(), name, specialty, location));
    }
}
