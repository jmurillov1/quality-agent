package org.ups.citasalud.domain.model;

import org.junit.jupiter.api.Test;
import org.ups.citasalud.domain.exception.TimeSlotNotAvailableException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class TimeSlotTest {

    @Test
    void dado_franja_disponible_cuando_book_entonces_estado_cambia_a_booked() {
        var slot = available();
        slot.book();
        assertThat(slot.getStatus()).isEqualTo(TimeSlotStatus.BOOKED);
    }

    @Test
    void dado_franja_ocupada_cuando_intenta_book_entonces_lanza_TimeSlotNotAvailableException() {
        var slot = booked();
        assertThatThrownBy(slot::book)
                .isInstanceOf(TimeSlotNotAvailableException.class);
    }

    @Test
    void dado_endTime_anterior_a_startTime_cuando_crea_franja_entonces_lanza_IllegalArgumentException() {
        assertThatThrownBy(() -> new TimeSlot(
                UUID.randomUUID(), UUID.randomUUID(),
                LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(9, 0),
                TimeSlotStatus.AVAILABLE))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private TimeSlot available() {
        return new TimeSlot(
                UUID.randomUUID(), UUID.randomUUID(),
                LocalDate.now().plusDays(1),
                LocalTime.of(9, 0), LocalTime.of(9, 30),
                TimeSlotStatus.AVAILABLE
        );
    }

    private TimeSlot booked() {
        return new TimeSlot(
                UUID.randomUUID(), UUID.randomUUID(),
                LocalDate.now().plusDays(1),
                LocalTime.of(9, 0), LocalTime.of(9, 30),
                TimeSlotStatus.BOOKED
        );
    }
}
