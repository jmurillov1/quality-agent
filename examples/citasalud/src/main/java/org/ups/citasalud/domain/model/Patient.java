package org.ups.citasalud.domain.model;

import java.time.Instant;
import java.util.UUID;

public record Patient(
        UUID id,
        String name,
        String phone,
        String email,
        Instant registrationDate
) {}
