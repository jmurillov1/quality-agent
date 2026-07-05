package org.ups.citasalud.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface WhatsAppNotificationJpaRepository extends JpaRepository<WhatsAppNotificationJpaEntity, UUID> {

    @Query("""
        SELECT n FROM WhatsAppNotificationJpaEntity n
        WHERE n.sendStatus IN ('PENDING', 'FAILED')
          AND n.attempts < 3
          AND (n.lastAttemptAt IS NULL OR n.lastAttemptAt <= :cutoff)
        """)
    List<WhatsAppNotificationJpaEntity> findRetryable(@Param("cutoff") Instant cutoff);
}
