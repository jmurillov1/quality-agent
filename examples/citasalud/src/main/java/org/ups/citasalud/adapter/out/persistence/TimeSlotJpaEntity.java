package org.ups.citasalud.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "time_slot")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TimeSlotJpaEntity {

    @Id
    private UUID id;

    @Column(name = "doctor_id", nullable = false)
    private UUID doctorId;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(nullable = false, length = 20)
    private String status;
}
