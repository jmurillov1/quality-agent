package org.ups.citasalud.domain.model;

import java.time.Instant;
import java.util.UUID;

public class WhatsAppNotification {

    private final UUID id;
    private final UUID appointmentId;
    private final String destinationNumber;
    private final String content;
    private NotificationStatus sendStatus;
    private int attempts;
    private final Instant createdAt;
    private Instant lastAttemptAt;

    public WhatsAppNotification(UUID id, UUID appointmentId, String destinationNumber,
                                String content, NotificationStatus sendStatus,
                                int attempts, Instant createdAt, Instant lastAttemptAt) {
        this.id = id;
        this.appointmentId = appointmentId;
        this.destinationNumber = destinationNumber;
        this.content = content;
        this.sendStatus = sendStatus;
        this.attempts = attempts;
        this.createdAt = createdAt;
        this.lastAttemptAt = lastAttemptAt;
    }

    public UUID getId()                    { return id; }
    public UUID getAppointmentId()         { return appointmentId; }
    public String getDestinationNumber()   { return destinationNumber; }
    public String getContent()             { return content; }
    public NotificationStatus getSendStatus() { return sendStatus; }
    public int getAttempts()               { return attempts; }
    public Instant getCreatedAt()          { return createdAt; }
    public Instant getLastAttemptAt()      { return lastAttemptAt; }

    public void markSent() {
        this.sendStatus = NotificationStatus.SENT;
        this.attempts++;
        this.lastAttemptAt = Instant.now();
    }

    public void markFailed() {
        this.attempts++;
        this.lastAttemptAt = Instant.now();
        this.sendStatus = (this.attempts >= 3)
                ? NotificationStatus.FAILED
                : NotificationStatus.PENDING;
    }
}
