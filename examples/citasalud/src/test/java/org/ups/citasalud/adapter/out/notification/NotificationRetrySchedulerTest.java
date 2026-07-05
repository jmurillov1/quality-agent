package org.ups.citasalud.adapter.out.notification;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.api.v2010.account.MessageCreator;
import com.twilio.type.PhoneNumber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.ups.citasalud.adapter.out.persistence.WhatsAppNotificationJpaAdapter;
import org.ups.citasalud.domain.model.NotificationStatus;
import org.ups.citasalud.domain.model.WhatsAppNotification;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationRetrySchedulerTest {

    @Mock
    WhatsAppNotificationJpaAdapter notificationJpaAdapter;

    NotificationRetryScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new NotificationRetryScheduler(notificationJpaAdapter);
        ReflectionTestUtils.setField(scheduler, "accountSid", "ACtest");
        ReflectionTestUtils.setField(scheduler, "authToken",  "testtoken");
        ReflectionTestUtils.setField(scheduler, "fromNumber", "whatsapp:+14155238886");
    }

    @Test
    void dado_sin_notificaciones_pendientes_cuando_scheduler_ejecuta_entonces_no_llama_twilio() {
        when(notificationJpaAdapter.findRetryable(any())).thenReturn(List.of());

        scheduler.retryPendingNotifications();

        verify(notificationJpaAdapter, never()).save(any());
    }

    @Test
    void dado_notificacion_con_3_intentos_fallidos_cuando_scheduler_ejecuta_entonces_no_reintenta() {
        var notification = failedNotificationWithAttempts(3);
        when(notificationJpaAdapter.findRetryable(any())).thenReturn(List.of(notification));

        scheduler.retryPendingNotifications();

        verify(notificationJpaAdapter, never()).save(any());
    }

    @Test
    void dado_notificacion_pending_lista_para_reintento_cuando_scheduler_ejecuta_entonces_reintenta_y_guarda() {
        var notification = pendingNotificationDueForRetry();
        when(notificationJpaAdapter.findRetryable(any())).thenReturn(List.of(notification));
        MessageCreator creatorMock = mock(MessageCreator.class);

        try (MockedStatic<Twilio> twilioMock = mockStatic(Twilio.class);
             MockedStatic<Message> messageMock = mockStatic(Message.class)) {
            messageMock.when(() -> Message.creator(any(PhoneNumber.class), any(PhoneNumber.class), any(String.class))).thenReturn(creatorMock);
            when(creatorMock.create()).thenReturn(mock(Message.class));

            scheduler.retryPendingNotifications();
        }

        verify(notificationJpaAdapter).save(any());
    }

    @Test
    void dado_notificacion_pending_cuya_ventana_de_reintento_no_ha_pasado_cuando_scheduler_ejecuta_entonces_no_reintenta() {
        var notification = new WhatsAppNotification(
                UUID.randomUUID(), UUID.randomUUID(), "+573001234567", "Test content",
                NotificationStatus.PENDING, 0, Instant.now(), Instant.now()
        );
        when(notificationJpaAdapter.findRetryable(any())).thenReturn(List.of(notification));

        scheduler.retryPendingNotifications();

        verify(notificationJpaAdapter, never()).save(any());
    }

    private WhatsAppNotification pendingNotificationDueForRetry() {
        return new WhatsAppNotification(
                UUID.randomUUID(), UUID.randomUUID(), "+573001234567", "Test content",
                NotificationStatus.PENDING, 0,
                Instant.now().minus(2, ChronoUnit.HOURS),
                Instant.now().minus(2, ChronoUnit.HOURS)
        );
    }

    private WhatsAppNotification failedNotificationWithAttempts(int attempts) {
        return new WhatsAppNotification(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "+573001234567",
                "Test content",
                NotificationStatus.FAILED,
                attempts,
                Instant.now().minus(2, ChronoUnit.HOURS),
                Instant.now().minus(35, ChronoUnit.MINUTES)
        );
    }
}
