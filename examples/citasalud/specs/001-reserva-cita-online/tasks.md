---
description: "Lista de tareas para la implementación de Reserva de Cita en Línea 24/7"
---

# Tareas: Reserva de Cita en Línea 24/7

**Entrada**: Documentos de diseño en `specs/001-reserva-cita-online/`

**Prerrequisitos**: plan.md ✅ | spec.md ✅ | research.md ✅ | data-model.md ✅ | contracts/ ✅

**Pruebas**: Incluidas — BDD obligatorio según Principio II de la Constitución.
Los tests se escriben PRIMERO y deben FALLAR antes de implementar (Rojo-Verde-Refactorizar).

**Política de idiomas (Principio VI)**:
- Código fuente, clases, métodos, campos, rutas API → **inglés**
- Comentarios de documentación, mensajes de usuario → **español**

## Formato: `[ID] [P?] [Historia?] Descripción con ruta de archivo`

- **[P]**: Puede ejecutarse en paralelo (archivos distintos, sin dependencias pendientes)
- **[Historia]**: Historia de usuario a la que pertenece la tarea (US1, US2)
- Incluir ruta de archivo exacta en cada descripción

## Convenciones de Rutas

- Código fuente: `src/main/java/org/ups/citasalud/`
- Pruebas: `src/test/java/org/ups/citasalud/`
- Recursos: `src/main/resources/`
- Migraciones: `src/main/resources/db/migration/`

---

## Fase 1: Configuración (Infraestructura Compartida)

**Propósito**: Inicialización del proyecto con Arquitectura Limpia, herramientas de calidad
y generación de código desde el contrato OpenAPI.

- [X] T001 Crear proyecto Gradle con estructura de Arquitectura Limpia en `src/main/java/org/ups/citasalud/` (capas: `domain/`, `application/`, `adapter/`, `infrastructure/`)
- [X] T002 Configurar `build.gradle` con dependencias: Spring Boot 4.1.x, Spring Data JPA, Spring Validation, PostgreSQL driver, Flyway, JUnit 5, AssertJ, Mockito, Testcontainers, Twilio SDK
- [X] T003 [P] Configurar plugin Gradle `org.openapi.generator` en `build.gradle` apuntando a `specs/001-reserva-cita-online/contracts/openapi.yml` con `interfaceOnly=true` y `outputDir=build/generated/sources/openapi`
- [X] T004 [P] Configurar plugin Gradle `jacoco` en `build.gradle`: tarea `test` con agente habilitado; tareas `jacocoTestReport` y `jacocoTestCoverageVerification` vinculadas a `check`; umbrales mínimos `INSTRUCTION` ≥ 0.80 global y por clase > 0.80; excluir `**/generated/**`
- [X] T005 [P] Configurar ArchUnit 1.3.x en `build.gradle` (dependencia `test`) y crear prueba de arquitectura en `src/test/java/org/ups/citasalud/ArchitectureTest.java` que valide que ninguna clase de `domain` o `application` importa de `adapter` o `infrastructure`
- [X] T006 [P] Crear `src/main/resources/application.yaml` con configuración base de datasource, JPA (DDL `validate`), Flyway y servidor (`port: 8080`)
- [X] T007 Verificar que `./gradlew compileJava` genera interfaces OpenAPI bajo `build/generated/sources/openapi/` sin errores

---

## Fase 2: Fundacional (Prerrequisitos Bloqueantes)

**Propósito**: Infraestructura de dominio y persistencia que todas las historias de usuario
necesitan antes de poder implementarse.

**⚠️ CRÍTICO**: Ninguna historia de usuario puede iniciar hasta completar esta fase.

- [X] T008 [P] Crear entidad de dominio `Patient` en `src/main/java/org/ups/citasalud/domain/model/Patient.java` (campos: `id`, `name`, `phone`, `email`, `registrationDate`; sin anotaciones de framework)
- [X] T009 [P] Crear entidad de dominio `Location` en `src/main/java/org/ups/citasalud/domain/model/Location.java` (campos: `id`, `name`, `address`; sin anotaciones de framework)
- [X] T010 [P] Crear entidad de dominio `Doctor` en `src/main/java/org/ups/citasalud/domain/model/Doctor.java` (campos: `id`, `name`, `specialty`, `locationId`; sin anotaciones de framework)
- [X] T011 [P] Crear enum `TimeSlotStatus` en `src/main/java/org/ups/citasalud/domain/model/TimeSlotStatus.java` con valores `AVAILABLE` y `BOOKED`
- [X] T012 [P] Crear entidad de dominio `TimeSlot` en `src/main/java/org/ups/citasalud/domain/model/TimeSlot.java` (campos: `id`, `doctorId`, `date`, `startTime`, `endTime`, `status: TimeSlotStatus`; validación `endTime > startTime`; método `book()` que cambia estado a `BOOKED`; sin anotaciones de framework)
- [X] T013 [P] Crear enum `AppointmentStatus` en `src/main/java/org/ups/citasalud/domain/model/AppointmentStatus.java` con valores `CONFIRMED` y `CANCELLED`
- [X] T014 Crear entidad de dominio `Appointment` en `src/main/java/org/ups/citasalud/domain/model/Appointment.java` (campos: `id`, `patientId`, `doctorId`, `timeSlotId`, `status: AppointmentStatus`, `createdAt`; sin anotaciones de framework) — depende de T013
- [X] T015 [P] Crear enum `NotificationStatus` en `src/main/java/org/ups/citasalud/domain/model/NotificationStatus.java` con valores `PENDING`, `SENT` y `FAILED`
- [X] T016 Crear entidad de dominio `WhatsAppNotification` en `src/main/java/org/ups/citasalud/domain/model/WhatsAppNotification.java` (campos: `id`, `appointmentId`, `destinationNumber`, `content`, `sendStatus: NotificationStatus`, `attempts`, `createdAt`, `lastAttemptAt`; sin anotaciones de framework) — depende de T015
- [X] T017 [P] Crear evento de dominio `AppointmentConfirmedEvent` en `src/main/java/org/ups/citasalud/domain/model/AppointmentConfirmedEvent.java` (campos: `appointmentId`, `patientPhone`, `doctorName`, `date`, `startTime`, `locationName`)
- [X] T018 [P] Crear excepción de dominio `TimeSlotNotAvailableException` en `src/main/java/org/ups/citasalud/domain/exception/TimeSlotNotAvailableException.java`
- [X] T019 [P] Crear excepción de dominio `AppointmentNotFoundException` en `src/main/java/org/ups/citasalud/domain/exception/AppointmentNotFoundException.java`
- [X] T069 [P] Crear excepción de dominio `PatientNotFoundException` en `src/main/java/org/ups/citasalud/domain/exception/PatientNotFoundException.java`
- [X] T020 [P] Crear puerto de salida `DoctorRepositoryPort` en `src/main/java/org/ups/citasalud/domain/port/out/DoctorRepositoryPort.java` (métodos: `findById(UUID)`, `findBySpecialty(String, Pageable)`)
- [X] T021 [P] Crear puerto de salida `TimeSlotRepositoryPort` en `src/main/java/org/ups/citasalud/domain/port/out/TimeSlotRepositoryPort.java` (métodos: `findByDoctorIdAndDate(UUID, LocalDate)`, `findByIdForUpdate(UUID)`, `save(TimeSlot)`)
- [X] T022 [P] Crear puerto de salida `AppointmentRepositoryPort` en `src/main/java/org/ups/citasalud/domain/port/out/AppointmentRepositoryPort.java` (métodos: `save(Appointment)`, `findById(UUID)`, `findByPatientId(UUID, Pageable)`)
- [X] T023 [P] Crear puerto de salida `WhatsAppNotificationPort` en `src/main/java/org/ups/citasalud/domain/port/out/WhatsAppNotificationPort.java` (método: `send(WhatsAppNotification)`)
- [X] T068 [P] Crear puerto de salida `PatientRepositoryPort` en `src/main/java/org/ups/citasalud/domain/port/out/PatientRepositoryPort.java` (método: `findById(UUID): Optional<Patient>`)
- [X] T024 Crear migración Flyway `V1__create_schema.sql` en `src/main/resources/db/migration/` con tablas: `location`, `doctor`, `patient`, `time_slot`, `appointment`, `whatsapp_notification` según `data-model.md`
- [X] T025 Crear migración Flyway `V2__seed_data.sql` en `src/main/resources/db/migration/` con datos de prueba: 2 sedes, 5 médicos, 1 paciente, franjas horarias para la semana siguiente
- [X] T026 Verificar `./gradlew test --tests "*ArchitectureTest*"` pasa con cero violaciones de capas

**Punto de control**: Dominio puro compilable, esquema de BD listo, guardián de arquitectura verde.

---

## Fase 3: Historia de Usuario 1 — Reserva Exitosa (Prioridad: P1) 🎯 MVP

**Objetivo**: Un paciente autenticado puede consultar disponibilidad y confirmar una cita;
recibe confirmación por WhatsApp; la franja queda bloqueada.

**Prueba independiente**: `POST /api/v1/appointments` retorna `201` con `status=CONFIRMED`;
`GET /api/v1/doctors/{id}/time-slots` muestra la franja con `available=false`;
`GET /api/v1/patients/{id}/appointments` lista la cita.

### Pruebas BDD para Historia 1 — ESCRIBIR PRIMERO, CONFIRMAR QUE FALLAN ⚠️

- [X] T027 [P] [US1] Crear prueba funcional `BookAppointmentFunctionalTest` en `src/test/java/org/ups/citasalud/functional/BookAppointmentFunctionalTest.java` con escenario: `dado_paciente_autenticado_cuando_reserva_franja_disponible_entonces_cita_confirmada_y_whatsapp_enviado` (MockMvc slice completo, stub Twilio con WireMock)
- [X] T028 [P] [US1] Crear prueba funcional `CheckAvailabilityFunctionalTest` en `src/test/java/org/ups/citasalud/functional/CheckAvailabilityFunctionalTest.java` con escenario: `dado_medico_con_franjas_cuando_consulta_disponibilidad_entonces_retorna_franjas_con_estado_correcto`
- [X] T029 [P] [US1] Crear prueba de integración `AppointmentJpaAdapterIntegrationTest` en `src/test/java/org/ups/citasalud/adapter/out/persistence/AppointmentJpaAdapterIntegrationTest.java` (Testcontainers PostgreSQL; valida persistencia y unicidad de `time_slot_id`)
- [X] T030 [P] [US1] Crear prueba de integración `TimeSlotJpaAdapterIntegrationTest` en `src/test/java/org/ups/citasalud/adapter/out/persistence/TimeSlotJpaAdapterIntegrationTest.java` (Testcontainers; valida `SELECT FOR UPDATE` y constraint `uq_time_slot`)
- [X] T031 [P] [US1] Crear prueba unitaria `BookAppointmentUseCaseTest` en `src/test/java/org/ups/citasalud/application/usecase/BookAppointmentUseCaseTest.java` con escenarios: `dado_franja_disponible_cuando_reserva_entonces_cita_confirmada_y_evento_publicado` y `dado_franja_ocupada_cuando_reserva_entonces_lanza_TimeSlotNotAvailableException`
- [X] T032 [P] [US1] Crear prueba unitaria `CheckAvailabilityUseCaseTest` en `src/test/java/org/ups/citasalud/application/usecase/CheckAvailabilityUseCaseTest.java` con escenario: `dado_medico_existente_cuando_consulta_disponibilidad_entonces_retorna_franjas`

### Implementación Historia 1 (después de confirmar que pruebas FALLAN)

- [X] T033 [P] [US1] Crear puerto de entrada `CheckAvailabilityUseCase` en `src/main/java/org/ups/citasalud/domain/port/in/CheckAvailabilityUseCase.java` (método: `execute(UUID doctorId, LocalDate date): List<TimeSlot>`)
- [X] T034 [P] [US1] Crear puerto de entrada `BookAppointmentUseCase` en `src/main/java/org/ups/citasalud/domain/port/in/BookAppointmentUseCase.java` (método: `execute(UUID patientId, UUID timeSlotId): Appointment`)
- [X] T035 [P] [US1] Crear puerto de entrada `GetPatientAppointmentsUseCase` en `src/main/java/org/ups/citasalud/domain/port/in/GetPatientAppointmentsUseCase.java` (método: `execute(UUID patientId, AppointmentStatus status, Pageable pageable): Page<Appointment>`)
- [X] T036 [US1] Implementar `CheckAvailabilityUseCaseImpl` en `src/main/java/org/ups/citasalud/application/usecase/CheckAvailabilityUseCaseImpl.java` — depende de T033, T020, T021
- [X] T037 [US1] Implementar `BookAppointmentUseCaseImpl` en `src/main/java/org/ups/citasalud/application/usecase/BookAppointmentUseCaseImpl.java`: anotar método `execute()` con `@Transactional` (propagación REQUIRED); validar existencia de paciente vía `PatientRepositoryPort` (lanzar `PatientNotFoundException` si no existe); obtener franja con `findByIdForUpdate` (`SELECT FOR UPDATE`); validar `AVAILABLE`, cambiar a `BOOKED`; crear `Appointment`; publicar `AppointmentConfirmedEvent` — depende de T034, T021, T022, T068, T069
- [X] T038 [US1] Implementar `GetPatientAppointmentsUseCaseImpl` en `src/main/java/org/ups/citasalud/application/usecase/GetPatientAppointmentsUseCaseImpl.java` — depende de T035, T022
- [X] T039 [P] [US1] Implementar entidades JPA para `TimeSlot` en `src/main/java/org/ups/citasalud/adapter/out/persistence/TimeSlotJpaEntity.java` (anotaciones JPA, mapeo a tabla `time_slot`)
- [X] T040 [P] [US1] Implementar entidades JPA para `Appointment` en `src/main/java/org/ups/citasalud/adapter/out/persistence/AppointmentJpaEntity.java` (anotaciones JPA, mapeo a tabla `appointment`)
- [X] T041 [P] [US1] Implementar entidades JPA para `Doctor` y `Location` en `src/main/java/org/ups/citasalud/adapter/out/persistence/DoctorJpaEntity.java` y `LocationJpaEntity.java`
- [X] T063 [P] [US1] Implementar entidad JPA `PatientJpaEntity` en `src/main/java/org/ups/citasalud/adapter/out/persistence/PatientJpaEntity.java` (anotaciones JPA, mapeo a tabla `patient`)
- [X] T064 [US1] Implementar `PatientJpaAdapter` en `src/main/java/org/ups/citasalud/adapter/out/persistence/PatientJpaAdapter.java` implementando puerto `PatientRepositoryPort` (método `findById(UUID): Optional<Patient>`); necesario para validar existencia de paciente en `BookAppointmentUseCaseImpl` — depende de T063
- [X] T065 [P] [US1] Implementar entidad JPA `WhatsAppNotificationJpaEntity` en `src/main/java/org/ups/citasalud/adapter/out/persistence/WhatsAppNotificationJpaEntity.java` (anotaciones JPA, mapeo a tabla `whatsapp_notification`)
- [X] T066 [US1] Implementar `WhatsAppNotificationJpaAdapter` en `src/main/java/org/ups/citasalud/adapter/out/persistence/WhatsAppNotificationJpaAdapter.java` para persistir y consultar registros de `WhatsAppNotification`; usado por `TwilioWhatsAppAdapter` para guardar estado `SENT`/`FAILED` y por el scheduler de reintentos — depende de T065
- [X] T042 [US1] Implementar `TimeSlotJpaAdapter` en `src/main/java/org/ups/citasalud/adapter/out/persistence/TimeSlotJpaAdapter.java` implementando `TimeSlotRepositoryPort`; usar `@Lock(LockModeType.PESSIMISTIC_WRITE)` en `findByIdForUpdate` — depende de T039, T021
- [X] T043 [US1] Implementar `AppointmentJpaAdapter` en `src/main/java/org/ups/citasalud/adapter/out/persistence/AppointmentJpaAdapter.java` implementando `AppointmentRepositoryPort` — depende de T040, T022
- [X] T044 [US1] Implementar `DoctorJpaAdapter` en `src/main/java/org/ups/citasalud/adapter/out/persistence/DoctorJpaAdapter.java` implementando `DoctorRepositoryPort` — depende de T041, T020
- [X] T045 [US1] Implementar `TwilioWhatsAppAdapter` en `src/main/java/org/ups/citasalud/adapter/out/notification/TwilioWhatsAppAdapter.java` implementando `WhatsAppNotificationPort`; leer credenciales de `application.yaml`; persistir `WhatsAppNotification` con estado `SENT` o `FAILED`
- [X] T046 [US1] Implementar listener de evento `AppointmentNotificationListener` en `src/main/java/org/ups/citasalud/adapter/out/notification/AppointmentNotificationListener.java`: anotar con `@Async` para que la notificación no bloquee la respuesta HTTP; escuchar `AppointmentConfirmedEvent`; llamar `WhatsAppNotificationPort.send()`
- [X] T067 [US1] Implementar scheduler de reintentos `NotificationRetryScheduler` en `src/main/java/org/ups/citasalud/adapter/out/notification/NotificationRetryScheduler.java` con `@Scheduled`; consultar notificaciones con `sendStatus=PENDING` o `sendStatus=FAILED` y `attempts < 3`; reintentar con retroceso exponencial (1 min, 5 min, 30 min); actualizar `attempts` y `lastAttemptAt`; marcar `FAILED` definitivo tras 3 intentos — cubre RF-008; depende de T066
- [X] T047 [US1] Implementar `AppointmentsController` en `src/main/java/org/ups/citasalud/adapter/in/web/AppointmentsController.java` implementando `AppointmentsApi` (generada por openapi-generator del tag `appointments`); anotar parámetro request con `@Valid`; cubrir operaciones: `bookAppointment` → `BookAppointmentUseCase`, `getAppointment` → `AppointmentRepositoryPort.findById`, `listPatientAppointments` → `GetPatientAppointmentsUseCase`; manejar `TimeSlotNotAvailableException` → `409`
- [X] T070 [US1] Añadir prueba BDD `dado_request_con_patientId_nulo_cuando_book_appointment_entonces_400_validation_failed` en `src/test/java/org/ups/citasalud/adapter/in/web/AppointmentsControllerTest.java`; verificar que `MethodArgumentNotValidException` es capturada por `GlobalExceptionHandler` y retorna `{"code":"VALIDATION_FAILED"}`
- [X] T048 [US1] Implementar `DoctorsController` en `src/main/java/org/ups/citasalud/adapter/in/web/DoctorsController.java` implementando `DoctorsApi` (generada por openapi-generator del tag `doctors`); cubrir operaciones: `listDoctors` → `DoctorRepositoryPort.findBySpecialty`, `checkAvailability` → `CheckAvailabilityUseCase`
- [X] T049 [US1] Crear raíz de composición `UseCaseConfig` en `src/main/java/org/ups/citasalud/infrastructure/config/UseCaseConfig.java`: beans `@Bean` para cada `UseCaseImpl` inyectando los puertos correspondientes; anotar con `@EnableAsync` y `@EnableScheduling` para habilitar el listener asíncrono (T046) y el scheduler de reintentos (T067)
- [X] T050 [US1] Crear manejador global de excepciones `GlobalExceptionHandler` en `src/main/java/org/ups/citasalud/adapter/in/web/GlobalExceptionHandler.java` (`@RestControllerAdvice`): mapear `TimeSlotNotAvailableException` → `409 TIME_SLOT_NOT_AVAILABLE`, `AppointmentNotFoundException` → `404 RESOURCE_NOT_FOUND`, `PatientNotFoundException` → `404 PATIENT_NOT_FOUND`, `MethodArgumentNotValidException` → `400 VALIDATION_FAILED`, `ConstraintViolationException` → `400 VALIDATION_FAILED`

**Punto de control**: `./gradlew check` verde; `BookAppointmentFunctionalTest` y `CheckAvailabilityFunctionalTest` pasan; cobertura de Historia 1 ≥ 80 %.

---

## Fase 4: Historia de Usuario 2 — Franja No Disponible (Prioridad: P2)

**Objetivo**: Cuando la franja ya está ocupada el sistema rechaza la reserva con `409` y
presenta mensaje claro; el paciente puede elegir otra franja.

**Prueba independiente**: Crear reserva previa en una franja; intentar reservar la misma
franja → `409 TIME_SLOT_NOT_AVAILABLE`; franja sigue `available=false` para todos.

### Pruebas BDD para Historia 2 — ESCRIBIR PRIMERO, CONFIRMAR QUE FALLAN ⚠️

- [X] T051 [P] [US2] Crear prueba funcional `TimeSlotConflictFunctionalTest` en `src/test/java/org/ups/citasalud/functional/TimeSlotConflictFunctionalTest.java` con escenarios:
  `dado_franja_ocupada_cuando_otro_paciente_intenta_reservar_entonces_409_time_slot_not_available` y
  `dado_todas_franjas_ocupadas_cuando_consulta_disponibilidad_entonces_todas_available_false`
- [X] T052 [P] [US2] Crear prueba funcional de concurrencia `ConcurrentBookingFunctionalTest` en `src/test/java/org/ups/citasalud/functional/ConcurrentBookingFunctionalTest.java` con escenario:
  `dado_dos_pacientes_cuando_reservan_misma_franja_simultaneamente_entonces_solo_una_cita_se_confirma`
  (lanzar dos threads, verificar exactamente un `201` y un `409`, cero duplicados en tabla `appointment`)
- [X] T053 [P] [US2] Crear prueba unitaria `TimeSlotTest` en `src/test/java/org/ups/citasalud/domain/model/TimeSlotTest.java` con escenario:
  `dado_franja_ocupada_cuando_intenta_book_entonces_lanza_TimeSlotNotAvailableException`

### Implementación Historia 2 (después de confirmar que pruebas FALLAN)

> La lógica de rechazo ya está parcialmente en `BookAppointmentUseCaseImpl` (T037).
> Esta fase refuerza y valida el comportamiento de frontera.

- [X] T054 [US2] Agregar validación en `TimeSlot.book()` (`src/main/java/org/ups/citasalud/domain/model/TimeSlot.java`): lanzar `TimeSlotNotAvailableException` si `status != AVAILABLE` — refuerza invariante de dominio
- [X] T055 [US2] Verificar que `GlobalExceptionHandler` (T050) ya maneja `TimeSlotNotAvailableException` → `409`; ajustar mensaje de respuesta si es necesario en `src/main/java/org/ups/citasalud/adapter/in/web/GlobalExceptionHandler.java`
- [X] T056 [US2] Verificar que `TimeSlotJpaAdapter.findByIdForUpdate` usa `@Lock(LockModeType.PESSIMISTIC_WRITE)` y que el constraint `uq_time_slot` de Flyway V1 está activo; añadir prueba de integración de constraint en `TimeSlotJpaAdapterIntegrationTest`

**Punto de control**: `ConcurrentBookingFunctionalTest` y `TimeSlotConflictFunctionalTest` pasan;
cero duplicados en `appointment` bajo concurrencia; `./gradlew check` verde.

---

## Fase Final: Pulido y Aspectos Transversales

**Propósito**: Mejoras que afectan a ambas historias de usuario.

- [X] T071 [P] Crear prueba unitaria `NotificationRetrySchedulerTest` en `src/test/java/org/ups/citasalud/adapter/out/notification/NotificationRetrySchedulerTest.java` con escenarios:
  `dado_notificacion_pending_cuando_scheduler_ejecuta_entonces_reintenta_y_marca_sent` y
  `dado_notificacion_con_3_intentos_fallidos_cuando_scheduler_ejecuta_entonces_marca_failed_definitivo`
- [X] T072 [P] Añadir dependencia `spring-boot-starter-actuator` en `build.gradle`; configurar en `application.yaml` `management.endpoints.web.exposure.include: health,info`; añadir prueba de integración que llama `GET /actuator/health` y verifica `{"status":"UP"}` — cubre CE-002
- [X] T057 [P] Actualizar documentación en `specs/001-reserva-cita-online/quickstart.md` si algún paso de validación cambió durante la implementación
- [X] T058 Revisar cobertura por clase: ejecutar `./gradlew check` y abrir `build/reports/jacoco/test/html/index.html`; añadir pruebas unitarias adicionales en `src/test/java/org/ups/citasalud/` para clases con cobertura < 80 %
- [X] T059 [P] Ejecutar prueba de arquitectura `./gradlew test --tests "*ArchitectureTest*"` y confirmar PASA sin violaciones de capas
- [X] T060 [P] Revisar cumplimiento SOLID/YAGNI/DRY en `src/main/java/org/ups/citasalud/`: verificar que ninguna clase tiene más de una razón para cambiar; eliminar cualquier código especulativo
- [X] T061 Ejecutar `./gradlew check` completo (todas las pruebas + JaCoCo `check`); confirmar cobertura global ≥ 80 % y cobertura por clase > 80 %
- [X] T062 [P] Validar contrato OpenAPI con swagger-cli: `swagger-cli validate specs/001-reserva-cita-online/contracts/openapi.yml`; corregir cualquier advertencia

---

## Dependencias y Orden de Ejecución

### Dependencias entre Fases

- **Fase 1 — Configuración**: Sin dependencias — iniciar inmediatamente
- **Fase 2 — Fundacional**: Depende de Fase 1 completa — **BLOQUEA** ambas historias
- **Historia 1 (Fase 3)**: Depende de Fase 2 completa
- **Historia 2 (Fase 4)**: Depende de Fase 3 completa (reutiliza `BookAppointmentUseCaseImpl`)
- **Pulido (Fase Final)**: Depende de ambas historias completas

### Dependencias Dentro de Historia 1

```
Pruebas (T027-T032) → deben FALLAR antes de implementar
Puertos de entrada (T033-T035) → antes de implementar casos de uso
Entidades JPA (T039-T041) → antes de adaptadores de persistencia
Adaptadores (T042-T046) → antes de controladores
Controladores (T047-T048) → antes de integración final
Raíz de composición (T049) → al final de la implementación
```

### Oportunidades de Paralelismo

**Fase 2** — Pueden ejecutarse en paralelo:
```
T008-T023 (todas las entidades de dominio y puertos) — archivos distintos
T024-T025 (migraciones Flyway) — ejecutar tras T008-T023
```

**Fase 3** — Pueden ejecutarse en paralelo:
```
Grupo A: T027-T032 (pruebas BDD — todas en paralelo)
Grupo B: T033-T035 (puertos de entrada — todos en paralelo)
Grupo C: T039-T041 (entidades JPA — todas en paralelo)
```

---

## Estrategia de Implementación

### MVP (sólo Historia 1)

1. Completar Fase 1: Configuración
2. Completar Fase 2: Fundacional (⚠️ crítico — bloquea todo)
3. Completar Fase 3: Historia 1 — **DETENER y VALIDAR**
   - `./gradlew check` verde
   - `BookAppointmentFunctionalTest` pasa
   - `GET /api/v1/doctors/{id}/time-slots` devuelve disponibilidad real
   - `POST /api/v1/appointments` crea cita y dispara WhatsApp
4. Demo con Historia 1 completa

### Entrega Incremental

1. Configuración + Fundacional → base lista
2. Historia 1 → probar independientemente → demo MVP
3. Historia 2 → probar independientemente → demo completo
4. Pulido → `./gradlew check` final con JaCoCo ≥ 80 %

---

## Notas

- `[P]` = archivos distintos, sin dependencias pendientes en la misma fase
- `[USx]` conecta cada tarea con su historia de usuario para trazabilidad
- Las pruebas BDD **deben fallar** antes de escribir la implementación
- Cada historia de usuario es independientemente completable y verificable
- Confirmar cobertura JaCoCo después de cada historia antes de avanzar
- Los nombres de clases, métodos y campos en el código deben estar en **inglés**
