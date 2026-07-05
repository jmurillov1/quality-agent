package org.ups.citasalud.adapter.out.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.ups.citasalud.domain.model.NotificationStatus;
import org.ups.citasalud.domain.model.WhatsAppNotification;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(WhatsAppNotificationJpaAdapter.class)
class WhatsAppNotificationJpaAdapterIntegrationTest {

    @Autowired WhatsAppNotificationJpaAdapter adapter;

    @Test
    void dado_notificacion_pending_cuando_guarda_entonces_persiste_correctamente() {
        var notification = new WhatsAppNotification(
                UUID.randomUUID(), UUID.randomUUID(), "+573001234567", "Su cita fue confirmada",
                NotificationStatus.PENDING, 0, Instant.now(), null
        );

        var saved = adapter.save(notification);

        assertThat(saved.getId()).isEqualTo(notification.getId());
        assertThat(saved.getSendStatus()).isEqualTo(NotificationStatus.PENDING);
    }

    @Test
    void dado_notificacion_pending_vencida_cuando_busca_reintentables_entonces_la_incluye() {
        var overdue = new WhatsAppNotification(
                UUID.randomUUID(), UUID.randomUUID(), "+573001234567", "contenido",
                NotificationStatus.PENDING, 1,
                Instant.now().minus(2, ChronoUnit.HOURS),
                Instant.now().minus(2, ChronoUnit.HOURS)
        );
        adapter.save(overdue);

        var result = adapter.findRetryable(Instant.now().minus(1, ChronoUnit.MINUTES));

        assertThat(result).extracting(WhatsAppNotification::getId).contains(overdue.getId());
    }

    @Test
    void dado_notificacion_con_3_intentos_cuando_busca_reintentables_entonces_no_la_incluye() {
        var exhausted = new WhatsAppNotification(
                UUID.randomUUID(), UUID.randomUUID(), "+573001234567", "contenido",
                NotificationStatus.FAILED, 3,
                Instant.now().minus(2, ChronoUnit.HOURS),
                Instant.now().minus(2, ChronoUnit.HOURS)
        );
        adapter.save(exhausted);

        var result = adapter.findRetryable(Instant.now().minus(1, ChronoUnit.MINUTES));

        assertThat(result).extracting(WhatsAppNotification::getId).doesNotContain(exhausted.getId());
    }
}
