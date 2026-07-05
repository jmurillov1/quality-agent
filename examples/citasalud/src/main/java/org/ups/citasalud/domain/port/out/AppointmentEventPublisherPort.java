package org.ups.citasalud.domain.port.out;

import org.ups.citasalud.domain.model.AppointmentConfirmedEvent;

public interface AppointmentEventPublisherPort {
    void publish(AppointmentConfirmedEvent event);
}
