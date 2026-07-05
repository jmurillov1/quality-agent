package org.ups.citasalud.adapter.out.notification;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.ups.citasalud.adapter.out.persistence.WhatsAppNotificationJpaAdapter;
import org.ups.citasalud.domain.model.NotificationStatus;
import org.ups.citasalud.domain.model.WhatsAppNotification;
import org.ups.citasalud.domain.port.out.WhatsAppNotificationPort;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TwilioWhatsAppAdapter implements WhatsAppNotificationPort {

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.whatsapp-from}")
    private String fromNumber;

    private final WhatsAppNotificationJpaAdapter notificationJpaAdapter;

    @Override
    public void send(WhatsAppNotification notification) {
        var record = new WhatsAppNotification(
                UUID.randomUUID(),
                notification.getAppointmentId(),
                notification.getDestinationNumber(),
                notification.getContent(),
                NotificationStatus.PENDING,
                0,
                Instant.now(),
                null
        );

        try {
            Twilio.init(accountSid, authToken);
            Message.creator(
                    new PhoneNumber("whatsapp:" + notification.getDestinationNumber()),
                    new PhoneNumber(fromNumber),
                    notification.getContent()
            ).create();
            record.markSent();
            log.info("WhatsApp enviado a {}", notification.getDestinationNumber());
        } catch (Exception e) {
            record.markFailed();
            log.warn("Fallo al enviar WhatsApp a {}: {}", notification.getDestinationNumber(), e.getMessage());
        }

        notificationJpaAdapter.save(record);
    }
}
