package org.ups.citasalud.domain.model;

import org.ups.citasalud.domain.exception.TimeSlotNotAvailableException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public class TimeSlot {

    private final UUID id;
    private final UUID doctorId;
    private final LocalDate date;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private TimeSlotStatus status;

    public TimeSlot(UUID id, UUID doctorId, LocalDate date, LocalTime startTime,
                    LocalTime endTime, TimeSlotStatus status) {
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("endTime debe ser posterior a startTime");
        }
        this.id = id;
        this.doctorId = doctorId;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }

    public void book() {
        if (status != TimeSlotStatus.AVAILABLE) {
            throw new TimeSlotNotAvailableException(id);
        }
        this.status = TimeSlotStatus.BOOKED;
    }

    public UUID getId()           { return id; }
    public UUID getDoctorId()     { return doctorId; }
    public LocalDate getDate()    { return date; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public TimeSlotStatus getStatus() { return status; }
}
