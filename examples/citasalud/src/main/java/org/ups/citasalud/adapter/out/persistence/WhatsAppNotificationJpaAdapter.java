package org.ups.citasalud.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.ups.citasalud.domain.model.NotificationStatus;
import org.ups.citasalud.domain.model.WhatsAppNotification;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WhatsAppNotificationJpaAdapter {

    private final WhatsAppNotificationJpaRepository repository;

    public WhatsAppNotification save(WhatsAppNotification notification) {
        var entity = toEntity(notification);
        return toDomain(repository.save(entity));
    }

    public List<WhatsAppNotification> findRetryable(Instant cutoff) {
        return repository.findRetryable(cutoff)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    public WhatsAppNotificationJpaEntity toEntity(WhatsAppNotification n) {
        return new WhatsAppNotificationJpaEntity(
                n.getId(),
                n.getAppointmentId(),
                n.getDestinationNumber(),
                n.getContent(),
                n.getSendStatus().name(),
                n.getAttempts(),
                n.getCreatedAt(),
                n.getLastAttemptAt()
        );
    }

    public WhatsAppNotification toDomain(WhatsAppNotificationJpaEntity e) {
        return new WhatsAppNotification(
                e.getId(),
                e.getAppointmentId(),
                e.getDestinationNumber(),
                e.getContent(),
                NotificationStatus.valueOf(e.getSendStatus()),
                e.getAttempts(),
                e.getCreatedAt(),
                e.getLastAttemptAt()
        );
    }
}
