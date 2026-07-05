package org.ups.citasalud.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class WhatsAppNotificationTest {

    @Test
    void dado_notificacion_pending_cuando_markSent_entonces_estado_sent_y_attempts_incrementa() {
        var notification = pending();

        notification.markSent();

        assertThat(notification.getSendStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(notification.getAttempts()).isEqualTo(1);
        assertThat(notification.getLastAttemptAt()).isNotNull();
    }

    @Test
    void dado_menos_de_3_intentos_cuando_markFailed_entonces_permanece_pending() {
        var notification = pending();

        notification.markFailed();

        assertThat(notification.getSendStatus()).isEqualTo(NotificationStatus.PENDING);
        assertThat(notification.getAttempts()).isEqualTo(1);
    }

    @Test
    void dado_3_intentos_fallidos_cuando_markFailed_entonces_estado_failed_definitivo() {
        var notification = pending();

        notification.markFailed();
        notification.markFailed();
        notification.markFailed();

        assertThat(notification.getSendStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(notification.getAttempts()).isEqualTo(3);
    }

    @Test
    void dado_notificacion_cuando_consulta_atributos_entonces_retorna_valores_originales() {
        var id = UUID.randomUUID();
        var appointmentId = UUID.randomUUID();
        var createdAt = Instant.now();
        var notification = new WhatsAppNotification(
                id, appointmentId, "+573001234567", "contenido",
                NotificationStatus.PENDING, 0, createdAt, null
        );

        assertThat(notification.getId()).isEqualTo(id);
        assertThat(notification.getAppointmentId()).isEqualTo(appointmentId);
        assertThat(notification.getDestinationNumber()).isEqualTo("+573001234567");
        assertThat(notification.getContent()).isEqualTo("contenido");
        assertThat(notification.getCreatedAt()).isEqualTo(createdAt);
        assertThat(notification.getLastAttemptAt()).isNull();
    }

    private WhatsAppNotification pending() {
        return new WhatsAppNotification(
                UUID.randomUUID(), UUID.randomUUID(), "+573001234567", "contenido",
                NotificationStatus.PENDING, 0, Instant.now(), null
        );
    }
}
