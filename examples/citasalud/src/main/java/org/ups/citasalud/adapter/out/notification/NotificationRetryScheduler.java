package org.ups.citasalud.adapter.out.notification;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.ups.citasalud.adapter.out.persistence.WhatsAppNotificationJpaAdapter;
import org.ups.citasalud.adapter.out.persistence.WhatsAppNotificationJpaEntity;
import org.ups.citasalud.adapter.out.persistence.WhatsAppNotificationJpaRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationRetryScheduler {

    private static final long[] RETRY_DELAYS_MINUTES = {1L, 5L, 30L};

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.whatsapp-from}")
    private String fromNumber;

    private final WhatsAppNotificationJpaAdapter notificationJpaAdapter;

    @Scheduled(fixedDelay = 60_000)
    public void retryPendingNotifications() {
        Instant cutoff = Instant.now().minus(1, ChronoUnit.MINUTES);
        var pending = notificationJpaAdapter.findRetryable(cutoff);

        for (var notification : pending) {
            int attempts = notification.getAttempts();
            if (attempts >= RETRY_DELAYS_MINUTES.length) {
                continue;
            }
            long requiredDelayMinutes = RETRY_DELAYS_MINUTES[attempts];
            Instant lastAttempt = notification.getLastAttemptAt() != null
                    ? notification.getLastAttemptAt()
                    : notification.getCreatedAt();

            if (lastAttempt.isAfter(Instant.now().minus(requiredDelayMinutes, ChronoUnit.MINUTES))) {
                continue;
            }

            sendAndUpdate(notification);
        }
    }

    private void sendAndUpdate(org.ups.citasalud.domain.model.WhatsAppNotification notification) {
        try {
            Twilio.init(accountSid, authToken);
            Message.creator(
                    new PhoneNumber("whatsapp:" + notification.getDestinationNumber()),
                    new PhoneNumber(fromNumber),
                    notification.getContent()
            ).create();
            notification.markSent();
            log.info("Reintento exitoso para notificación {}", notification.getId());
        } catch (Exception e) {
            notification.markFailed();
            log.warn("Reintento fallido para notificación {}: {}", notification.getId(), e.getMessage());
        }
        notificationJpaAdapter.save(notification);
    }
}
