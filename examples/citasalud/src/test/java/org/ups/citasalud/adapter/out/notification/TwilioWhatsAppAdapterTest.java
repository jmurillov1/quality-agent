package org.ups.citasalud.adapter.out.notification;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.api.v2010.account.MessageCreator;
import com.twilio.type.PhoneNumber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.ups.citasalud.adapter.out.persistence.WhatsAppNotificationJpaAdapter;
import org.ups.citasalud.domain.model.NotificationStatus;
import org.ups.citasalud.domain.model.WhatsAppNotification;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TwilioWhatsAppAdapterTest {

    @Mock WhatsAppNotificationJpaAdapter notificationJpaAdapter;

    TwilioWhatsAppAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new TwilioWhatsAppAdapter(notificationJpaAdapter);
        ReflectionTestUtils.setField(adapter, "accountSid", "ACtest");
        ReflectionTestUtils.setField(adapter, "authToken", "testtoken");
        ReflectionTestUtils.setField(adapter, "fromNumber", "whatsapp:+14155238886");
    }

    @Test
    void dado_envio_exitoso_cuando_send_entonces_guarda_notificacion_como_sent() {
        var notification = notification();
        MessageCreator creatorMock = mock(MessageCreator.class);

        try (MockedStatic<Twilio> twilioMock = mockStatic(Twilio.class);
             MockedStatic<Message> messageMock = mockStatic(Message.class)) {
            messageMock.when(() -> Message.creator(any(PhoneNumber.class), any(PhoneNumber.class), anyString())).thenReturn(creatorMock);
            when(creatorMock.create()).thenReturn(mock(Message.class));

            adapter.send(notification);

            twilioMock.verify(() -> Twilio.init("ACtest", "testtoken"));
        }

        verify(notificationJpaAdapter).save(argThat(n -> n.getSendStatus() == NotificationStatus.SENT));
    }

    @Test
    void dado_fallo_de_envio_cuando_send_entonces_guarda_notificacion_como_pending_o_failed() {
        var notification = notification();
        MessageCreator creatorMock = mock(MessageCreator.class);

        try (MockedStatic<Twilio> twilioMock = mockStatic(Twilio.class);
             MockedStatic<Message> messageMock = mockStatic(Message.class)) {
            messageMock.when(() -> Message.creator(any(PhoneNumber.class), any(PhoneNumber.class), anyString())).thenReturn(creatorMock);
            when(creatorMock.create()).thenThrow(new RuntimeException("Twilio no disponible"));

            adapter.send(notification);
        }

        verify(notificationJpaAdapter).save(argThat(n ->
                n.getSendStatus() == NotificationStatus.PENDING || n.getSendStatus() == NotificationStatus.FAILED));
    }

    private WhatsAppNotification notification() {
        return new WhatsAppNotification(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "+573001234567",
                "Su cita fue confirmada",
                NotificationStatus.PENDING,
                0,
                Instant.now(),
                null
        );
    }
}
