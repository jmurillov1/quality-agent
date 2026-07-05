package org.ups.citasalud.application.usecase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ups.citasalud.domain.model.TimeSlot;
import org.ups.citasalud.domain.model.TimeSlotStatus;
import org.ups.citasalud.domain.port.out.TimeSlotRepositoryPort;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckAvailabilityUseCaseTest {

    @Mock TimeSlotRepositoryPort timeSlotRepository;

    @InjectMocks
    CheckAvailabilityUseCaseImpl useCase;

    @Test
    void dado_medico_existente_cuando_consulta_disponibilidad_entonces_retorna_franjas() {
        UUID doctorId = UUID.randomUUID();
        LocalDate date = LocalDate.now().plusDays(1);
        var slot = new TimeSlot(
                UUID.randomUUID(), doctorId, date,
                LocalTime.of(9, 0), LocalTime.of(9, 30), TimeSlotStatus.AVAILABLE
        );
        when(timeSlotRepository.findByDoctorIdAndDate(doctorId, date)).thenReturn(List.of(slot));

        List<TimeSlot> result = useCase.execute(doctorId, date);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(TimeSlotStatus.AVAILABLE);
    }
}
