package org.ups.citasalud.domain.model;

import java.util.UUID;

public record Location(
        UUID id,
        String name,
        String address
) {}
