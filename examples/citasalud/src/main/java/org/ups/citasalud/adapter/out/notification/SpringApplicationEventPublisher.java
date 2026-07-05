package org.ups.citasalud.adapter.out.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.ups.citasalud.domain.model.AppointmentConfirmedEvent;
import org.ups.citasalud.domain.port.out.AppointmentEventPublisherPort;

@Component
@RequiredArgsConstructor
public class SpringApplicationEventPublisher implements AppointmentEventPublisherPort {

    private final ApplicationEventPublisher publisher;

    @Override
    public void publish(AppointmentConfirmedEvent event) {
        publisher.publishEvent(event);
    }
}
