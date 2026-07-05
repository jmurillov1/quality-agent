package org.ups.citasalud.domain.port.out;

import org.ups.citasalud.domain.model.WhatsAppNotification;

public interface WhatsAppNotificationPort {
    void send(WhatsAppNotification notification);
}
