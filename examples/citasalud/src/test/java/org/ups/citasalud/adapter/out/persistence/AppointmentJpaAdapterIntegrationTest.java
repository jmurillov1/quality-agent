package org.ups.citasalud.adapter.out.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.ups.citasalud.domain.model.Appointment;
import org.ups.citasalud.domain.model.AppointmentStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import(AppointmentJpaAdapter.class)
class AppointmentJpaAdapterIntegrationTest {

    @Autowired AppointmentJpaAdapter adapter;
    @Autowired AppointmentJpaRepository appointmentRepo;
    @Autowired TimeSlotJpaRepository timeSlotRepo;
    @Autowired DoctorJpaRepository doctorRepo;
    @Autowired PatientJpaRepository patientRepo;
    @Autowired LocationJpaRepository locationRepo;

    @Test
    void dado_appointment_cuando_guarda_entonces_persiste_y_recupera() {
        var ids = seedRequiredData();
        var appointment = new Appointment(
                UUID.randomUUID(), ids.patientId(), ids.doctorId(),
                ids.timeSlotId(), AppointmentStatus.CONFIRMED, Instant.now()
        );

        var saved = adapter.save(appointment);
        var found = adapter.findById(saved.id());

        assertThat(found).isPresent();
        assertThat(found.get().status()).isEqualTo(AppointmentStatus.CONFIRMED);
    }

    @Test
    void dado_dos_citas_para_misma_franja_cuando_guarda_segunda_entonces_viola_unicidad() {
        var ids = seedRequiredData();
        var first = new Appointment(
                UUID.randomUUID(), ids.patientId(), ids.doctorId(),
                ids.timeSlotId(), AppointmentStatus.CONFIRMED, Instant.now()
        );
        adapter.save(first);

        var second = new Appointment(
                UUID.randomUUID(), ids.patientId(), ids.doctorId(),
                ids.timeSlotId(), AppointmentStatus.CONFIRMED, Instant.now()
        );

        assertThatThrownBy(() -> adapter.save(second))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void dado_citas_de_paciente_cuando_busca_por_patientId_sin_status_entonces_retorna_todas() {
        var ids = seedRequiredData();
        adapter.save(new Appointment(
                UUID.randomUUID(), ids.patientId(), ids.doctorId(),
                ids.timeSlotId(), AppointmentStatus.CONFIRMED, Instant.now()
        ));

        var page = adapter.findByPatientId(ids.patientId(), null, PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).status()).isEqualTo(AppointmentStatus.CONFIRMED);
    }

    @Test
    void dado_citas_de_paciente_cuando_busca_por_patientId_con_status_entonces_filtra() {
        var ids = seedRequiredData();
        adapter.save(new Appointment(
                UUID.randomUUID(), ids.patientId(), ids.doctorId(),
                ids.timeSlotId(), AppointmentStatus.CONFIRMED, Instant.now()
        ));

        var page = adapter.findByPatientId(ids.patientId(), AppointmentStatus.CANCELLED, PageRequest.of(0, 10));

        assertThat(page.getContent()).isEmpty();
    }

    private record TestIds(UUID patientId, UUID doctorId, UUID timeSlotId) {}

    private TestIds seedRequiredData() {
        var location = locationRepo.save(new LocationJpaEntity(UUID.randomUUID(), "Sede", "Dirección"));
        var doctor = doctorRepo.save(new DoctorJpaEntity(UUID.randomUUID(), "Dr. Test", "General", location));
        var patient = patientRepo.save(new PatientJpaEntity(UUID.randomUUID(), "Test", "+1234567890", null, Instant.now()));
        var slot = timeSlotRepo.save(new TimeSlotJpaEntity(
                UUID.randomUUID(), doctor.getId(),
                LocalDate.now().plusDays(1),
                LocalTime.of(9, 0), LocalTime.of(9, 30), "AVAILABLE"
        ));
        return new TestIds(patient.getId(), doctor.getId(), slot.getId());
    }
}
