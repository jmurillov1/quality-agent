package org.ups.citasalud.domain.model;

import java.util.UUID;

public record Doctor(
        UUID id,
        String name,
        String specialty,
        UUID locationId
) {}
