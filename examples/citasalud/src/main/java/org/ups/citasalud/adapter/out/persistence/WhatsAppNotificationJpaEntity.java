package org.ups.citasalud.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "whatsapp_notification")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WhatsAppNotificationJpaEntity {

    @Id
    private UUID id;

    @Column(name = "appointment_id", nullable = false)
    private UUID appointmentId;

    @Column(name = "destination_number", nullable = false, length = 20)
    private String destinationNumber;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "send_status", nullable = false, length = 20)
    private String sendStatus;

    @Column(nullable = false)
    private int attempts;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "last_attempt_at")
    private Instant lastAttemptAt;
}
