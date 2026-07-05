package org.ups.citasalud.adapter.out.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.ups.citasalud.adapter.out.persistence.PatientJpaRepository;
import org.ups.citasalud.domain.model.AppointmentConfirmedEvent;
import org.ups.citasalud.domain.model.NotificationStatus;
import org.ups.citasalud.domain.model.WhatsAppNotification;
import org.ups.citasalud.domain.port.out.WhatsAppNotificationPort;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppointmentNotificationListener {

    private final WhatsAppNotificationPort whatsAppNotificationPort;
    private final PatientJpaRepository patientJpaRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAppointmentConfirmed(AppointmentConfirmedEvent event) {
        String patientPhone = patientJpaRepository.findById(event.patientId())
                .map(p -> p.getPhone())
                .orElse(null);

        if (patientPhone == null) {
            log.warn("No se encontró teléfono para paciente {} — notificación omitida", event.patientId());
            return;
        }

        String content = String.format(
                "✅ Su cita ha sido confirmada para el %s a las %s. Médico: Dr./Dra. CitaSalud.",
                event.date(), event.startTime()
        );

        var notification = new WhatsAppNotification(
                UUID.randomUUID(),
                event.appointmentId(),
                patientPhone,
                content,
                NotificationStatus.PENDING,
                0,
                java.time.Instant.now(),
                null
        );

        whatsAppNotificationPort.send(notification);
    }
}
