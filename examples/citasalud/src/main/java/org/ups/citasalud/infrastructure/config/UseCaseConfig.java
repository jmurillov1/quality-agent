package org.ups.citasalud.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableAsync
@EnableScheduling
public class UseCaseConfig {
    // Los beans de casos de uso son gestionados directamente por Spring
    // vía @Service en cada UseCaseImpl; esta clase sólo activa @Async y @Scheduled.
}
