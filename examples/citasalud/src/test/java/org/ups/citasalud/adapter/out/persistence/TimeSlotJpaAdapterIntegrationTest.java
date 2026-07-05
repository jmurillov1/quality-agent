package org.ups.citasalud.adapter.out.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.ups.citasalud.domain.model.TimeSlotStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TimeSlotJpaAdapter.class)
class TimeSlotJpaAdapterIntegrationTest {

    @Autowired TimeSlotJpaAdapter adapter;
    @Autowired TimeSlotJpaRepository repository;
    @Autowired DoctorJpaRepository doctorRepository;
    @Autowired LocationJpaRepository locationRepository;

    @Test
    void dado_franja_disponible_cuando_busca_por_doctor_y_fecha_entonces_la_retorna() {
        var doctor = persistDoctor();
        repository.save(new TimeSlotJpaEntity(
                UUID.randomUUID(), doctor.getId(),
                LocalDate.now().plusDays(1),
                LocalTime.of(9, 0), LocalTime.of(9, 30), "AVAILABLE"
        ));

        var result = adapter.findByDoctorIdAndDate(doctor.getId(), LocalDate.now().plusDays(1));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(TimeSlotStatus.AVAILABLE);
    }

    @Test
    void dado_franja_disponible_cuando_findByIdForUpdate_entonces_retorna_con_lock() {
        var doctor = persistDoctor();
        var slot = repository.save(new TimeSlotJpaEntity(
                UUID.randomUUID(), doctor.getId(),
                LocalDate.now().plusDays(1),
                LocalTime.of(10, 0), LocalTime.of(10, 30), "AVAILABLE"
        ));

        var found = adapter.findByIdForUpdate(slot.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo(TimeSlotStatus.AVAILABLE);
    }

    private DoctorJpaEntity persistDoctor() {
        var location = locationRepository.save(new LocationJpaEntity(UUID.randomUUID(), "Sede Test", "Calle 1"));
        return doctorRepository.save(new DoctorJpaEntity(UUID.randomUUID(), "Dr. Test", "General", location));
    }
}
