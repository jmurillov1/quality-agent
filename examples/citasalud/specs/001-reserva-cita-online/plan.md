# Plan de Implementación: Reserva de Cita en Línea 24/7

**Rama**: `001-reserva-cita-online` | **Fecha**: 2026-06-27 | **Spec**: [spec.md](spec.md)

**Entrada**: Especificación de funcionalidad en `specs/001-reserva-cita-online/spec.md`

## Resumen

Habilitar al paciente para que pueda reservar una cita médica en línea en cualquier momento
del día (24/7), sin depender del horario de atención telefónica. El sistema expone una API
REST diseñada mediante contrato OpenAPI 3.x, implementada con Arquitectura Limpia (Clean
Architecture) sobre Java 21 + Spring Boot 4.x. La disponibilidad de franjas horarias se
consulta en tiempo real; la concurrencia se controla con bloqueo pesimista (`SELECT FOR UPDATE`); la confirmación
dispara una notificación asíncrona por WhatsApp.

## Contexto Técnico

**Lenguaje/Versión**: Java 25 (toolchain 25, `release=21`) + Spring Boot 4.1.x

**Dependencias principales**:
- Spring Web (REST), Spring Data JPA, Spring Validation
- H2 (driver: `h2`)
- Flyway (migraciones de base de datos)
- Gradle plugin `org.openapi.generator` 7.12.x (generación de stubs desde contrato)
- JUnit 5 + AssertJ + Mockito (pruebas unitarias)
- Testcontainers 1.20.x + PostgreSQL module (pruebas de integración)
- Twilio SDK (cliente WhatsApp Business API)
- Gradle plugin `jacoco` (cobertura)
- ArchUnit 1.3.x (guardián de arquitectura)

**Almacenamiento**: PostgreSQL 15 — base de datos relacional con soporte de bloqueo pesimista
(`SELECT FOR UPDATE`) para control de concurrencia en franjas horarias.

**Pruebas**: JUnit 5 + AssertJ + Mockito (unitarias); Spring Boot Test + Testcontainers
(integración); JUnit 5 con nomenclatura BDD Dado/Cuando/Entonces (funcionales).

**Plataforma objetivo**: Servidor Linux — servicio REST desplegable como JAR o contenedor Docker.

**Tipo de proyecto**: Servicio web (API REST backend)

**Objetivos de rendimiento**:
- Consulta de disponibilidad: ≤ 500 ms p95 bajo carga normal
- Confirmación de reserva: ≤ 1 000 ms p95 (sin contar latencia de notificación WhatsApp)
- Notificación WhatsApp entregada en ≤ 30 s en el 95 % de los casos

> **Nota de verificación**: Estos objetivos de percentil (p95) son metas de rendimiento en
> producción; no cuentan con un test de carga dedicado dentro de `tasks.md` (fuera de alcance de
> este US por tamaño de equipo/escala inicial — ver `Escala/Alcance`). Se recomienda validarlos
> con una herramienta de carga (p. ej. Gatling/JMeter) como iniciativa de Deuda Técnica antes de
> escalar por encima de las 500 reservas concurrentes asumidas.

**Restricciones**:
- Disponibilidad ≥ 99.5 % mensual (24/7). Verificación: fuera del alcance de las pruebas
  automatizadas de esta funcionalidad; se sostiene mediante monitoreo de producción
  (`/actuator/health`, alertas de infraestructura) y topología de despliegue, no mediante tests.
- Notificación WhatsApp entregada en ≤ 30 s en el 95 % de los casos (CE-003). Verificación:
  los tests automatizados validan que el estado transiciona a `SENT`/`FAILED` y que el scheduler
  reintenta correctamente; el umbral de latencia real se valida en monitoreo de producción, no
  con un test de percentil dedicado.
- Cero dobles reservas bajo escrituras concurrentes (exclusión mutua garantizada por BD)
- Código generado por openapi-generator excluido de métricas JaCoCo
- Sin dependencias de frameworks en capas de dominio y casos de uso

**Escala/Alcance**: hasta 500 reservas concurrentes en hora pico; catálogo inicial de ~50 médicos
y ~10 especialidades.

## Comprobación de Constitución

*COMPUERTA: Debe aprobarse antes de la investigación de Fase 0. Re-verificar tras el diseño de Fase 1.*

| Principio | Estado | Notas |
|-----------|--------|-------|
| I. Arquitectura Limpia — capas definidas, flechas de dependencia hacia adentro | PASA | Estructura de paquetes domain / application / adapter / infrastructure definida en estructura de proyecto |
| II. BDD Testing — pruebas unitarias + integración + funcionales planificadas, orden Rojo-Verde-Refactorizar | PASA | Tres niveles de prueba definidos; nomenclatura `dado_cuando_entonces` obligatoria |
| III. SOLID / YAGNI / DRY — sin código especulativo, sin lógica duplicada | PASA | Solo se implementan RF-001..RF-008 del spec; revisar en code review |
| IV. API First — openapi.yml existe antes de la implementación; openapi-generator configurado | PASA | `contracts/openapi.yml` generado en Fase 1 de este plan; plugin `org.openapi.generator` configurado en `build.gradle` |
| V. Cobertura — compuerta JaCoCo configurada (>80% por clase, ≥80% global); código generado excluido | PASA | Exclusión de `**/generated/**` en JaCoCo `<excludes>`; tarea `jacocoTestCoverageVerification` vinculada a `check` |
| VI. Política de Idiomas — código/identificadores en inglés; documentación/comunicación en español | PASA | Clases, métodos y rutas API en inglés; spec, plan, quickstart y OpenAPI descriptions en español |

*Sin violaciones — tabla de Seguimiento de Complejidad no aplica.*

## Estructura del Proyecto

### Documentación (esta funcionalidad)

```text
specs/001-reserva-cita-online/
├── plan.md              # Este archivo
├── research.md          # Fase 0 — decisiones técnicas y rationale
├── data-model.md        # Fase 1 — entidades, atributos, relaciones
├── quickstart.md        # Fase 1 — guía de validación ejecutable
├── contracts/
│   └── openapi.yml      # Fase 1 — contrato API First
└── tasks.md             # Fase 2 — generado por /speckit-tasks
```

### Código Fuente (raíz del repositorio)

```text
src/
├── main/
│   └── java/org/ups/citasalud/
│       ├── domain/
│       │   ├── model/               # Entidades puras de dominio (sin anotaciones de framework)
│       │   │   ├── Patient.java
│       │   │   ├── Doctor.java
│       │   │   ├── Location.java
│       │   │   ├── TimeSlot.java
│       │   │   ├── Appointment.java
│       │   │   └── WhatsAppNotification.java
│       │   ├── port/
│       │   │   ├── in/              # Puertos de entrada (interfaces de casos de uso)
│       │   │   │   ├── CheckAvailabilityUseCase.java
│       │   │   │   ├── BookAppointmentUseCase.java
│       │   │   │   └── GetPatientAppointmentsUseCase.java
│       │   │   └── out/             # Puertos de salida (interfaces de repositorio/notificación)
│       │   │       ├── DoctorRepositoryPort.java
│       │   │       ├── TimeSlotRepositoryPort.java
│       │   │       ├── AppointmentRepositoryPort.java
│       │   │       ├── PatientRepositoryPort.java
│       │   │       └── WhatsAppNotificationPort.java
│       │   └── exception/           # Excepciones de dominio
│       │       ├── TimeSlotNotAvailableException.java
│       │       ├── AppointmentNotFoundException.java
│       │       └── PatientNotFoundException.java
│       ├── application/
│       │   └── usecase/             # Implementaciones de casos de uso
│       │       ├── CheckAvailabilityUseCaseImpl.java
│       │       ├── BookAppointmentUseCaseImpl.java
│       │       └── GetPatientAppointmentsUseCaseImpl.java
│       ├── adapter/
│       │   ├── in/
│       │   │   └── web/             # Controladores REST (implementan interfaces generadas por OpenAPI)
│       │   │       ├── AppointmentsController.java
│       │   │       └── DoctorsController.java
│       │   └── out/
│       │       ├── persistence/     # Adaptadores JPA (implementan puertos de salida)
│       │       │   ├── DoctorJpaAdapter.java
│       │       │   ├── TimeSlotJpaAdapter.java
│       │       │   ├── AppointmentJpaAdapter.java
│       │       │   ├── PatientJpaAdapter.java
│       │       │   └── WhatsAppNotificationJpaAdapter.java
│       │       └── notification/    # Adaptador WhatsApp (implementa WhatsAppNotificationPort)
│       │           ├── TwilioWhatsAppAdapter.java
│       │           ├── AppointmentNotificationListener.java
│       │           └── NotificationRetryScheduler.java
│       └── infrastructure/
│           └── config/              # Configuración Spring — raíz de composición
│               └── UseCaseConfig.java
├── main/resources/
│   ├── application.yaml
│   └── db/migration/                # Scripts Flyway
│       ├── V1__create_schema.sql
│       └── V2__seed_data.sql
└── test/
    └── java/org/ups/citasalud/
        ├── domain/                  # Pruebas unitarias (entidades y casos de uso)
        ├── adapter/                 # Pruebas de integración (Testcontainers)
        └── functional/             # Pruebas funcionales E2E (slice completo)
```

**Decisión de estructura**: Proyecto único (opción 1) — API REST backend sin frontend incluido
en este sprint. La estructura refleja la Arquitectura Limpia con cuatro capas explícitas.

## Seguimiento de Complejidad

> *Sin violaciones activas — sección vacía.*
