# Guía de Validación: Reserva de Cita en Línea 24/7

**Fecha**: 2026-06-27 | **Plan**: [plan.md](plan.md)

Esta guía describe cómo validar que la funcionalidad está correctamente implementada,
desde la configuración del entorno hasta la verificación de cada historia de usuario.

---

## Prerrequisitos

| Requisito | Versión mínima | Verificar con |
|-----------|---------------|---------------|
| Java | 21 | `java -version` |
| Docker | 24.x | `docker -version` |
| Docker Compose | 2.x | `docker compose version` |

> El proyecto usa Gradle Wrapper (`./gradlew`) — no es necesario instalar Gradle manualmente.

> Docker es necesario para levantar PostgreSQL y el stub de Twilio en pruebas de integración
> via Testcontainers. No se necesita instalación manual de PostgreSQL.

---

## Configuración Inicial

### 1. Clonar e instalar dependencias

```bash
# Desde la raíz del proyecto
./gradlew build -x test
```

El plugin Gradle `org.openapi.generator` genera los stubs en `build/generated/sources/openapi/`
durante esta fase. Verificar que no haya errores de compilación.

### 2. Variables de entorno para desarrollo local

Crear `src/main/resources/application-local.yml` con:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/citasalud
    username: citasalud
    password: citasalud
twilio:
  account-sid: ${TWILIO_ACCOUNT_SID}
  auth-token: ${TWILIO_AUTH_TOKEN}
  whatsapp-from: whatsapp:+14155238886
```

> Para pruebas de integración, Testcontainers levanta PostgreSQL automáticamente;
> no es necesario configurar la URL de base de datos manualmente.

---

## Ejecutar las Pruebas

### Pruebas unitarias

```bash
./gradlew test --tests "*Test"
```

Resultado esperado: todos los tests pasan; ningún test con nombre `test1()` o `testCreate()`.

### Pruebas de integración (requiere Docker)

```bash
./gradlew test --tests "*IntegrationTest"
```

Testcontainers levanta un contenedor PostgreSQL efímero. Verificar en el log que aparece:
`Started PostgreSQLContainer`.

### Pruebas funcionales (requiere Docker)

```bash
./gradlew test --tests "*FunctionalTest"
```

Cada prueba funcional cubre una historia de usuario completa end-to-end.

### Todas las pruebas + reporte JaCoCo

```bash
./gradlew check
```

Al finalizar, el reporte de cobertura se genera en `build/reports/jacoco/test/html/index.html`.
La construcción FALLA si cobertura global < 80 % o si alguna clase < 80 %.

---

## Escenarios de Validación Manual

### US-1: Reserva exitosa fuera de horario de atención

**Prerrequisito**: Base de datos con datos de prueba (V2__seed_data.sql cargado).

**Pasos**:

1. Consultar médicos disponibles:
   ```
   GET /api/v1/doctors?specialty=Medicina+General
   ```
   Resultado esperado: `200 OK` con lista de médicos; tomar nota de un `doctorId`.

2. Consultar disponibilidad para fecha futura:
   ```
   GET /api/v1/doctors/{doctorId}/time-slots?date=2026-07-15
   ```
   Resultado esperado: `200 OK` con franjas; al menos una con `"available": true`.
   Tomar nota del `id` de una franja disponible (`timeSlotId`).

3. Reservar la cita:
   ```
   POST /api/v1/appointments
   Content-Type: application/json
   {
     "patientId": "<uuid-paciente-prueba>",
     "timeSlotId": "<timeSlotId>"
   }
   ```
   Resultado esperado: `201 Created` con cuerpo `AppointmentResponse` donde `status = "CONFIRMED"`.

4. Verificar registro en historial:
   ```
   GET /api/v1/patients/{patientId}/appointments
   ```
   Resultado esperado: la cita recién creada aparece con `status = "CONFIRMED"`.

5. Verificar que la franja quedó ocupada:
   ```
   GET /api/v1/doctors/{doctorId}/time-slots?date=2026-07-15
   ```
   Resultado esperado: la franja reservada aparece con `"available": false`.

---

### US-2: Intento de reserva en franja no disponible

**Prerrequisito**: Completar el escenario US-1 con una franja ya ocupada.

**Pasos**:

1. Intentar reservar la misma franja con otro paciente:
   ```
   POST /api/v1/appointments
   Content-Type: application/json
   {
     "patientId": "<uuid-otro-paciente>",
     "timeSlotId": "<timeSlotId-ya-ocupado>"
   }
   ```
   Resultado esperado: `409 Conflict` con cuerpo:
   ```json
   {
     "code": "TIME_SLOT_NOT_AVAILABLE",
     "message": "La franja horaria seleccionada ya fue reservada. Por favor elija otra."
   }
   ```

2. Verificar que NO se creó una segunda cita:
   ```
   GET /api/v1/patients/{uuid-otro-paciente}/appointments
   ```
   Resultado esperado: lista vacía (ninguna cita para este paciente).

---

### Validación de concurrencia

**Propósito**: Confirmar que bajo escrituras simultáneas sólo una reserva prospera (RF-006).

Ejecutar la prueba funcional de concurrencia incluida en el proyecto:

```bash
./gradlew test --tests "*dado_dos_pacientes_cuando_reservan_misma_franja_simultaneamente_entonces_solo_una_cita_se_confirma*"
```

Resultado esperado: una reserva retorna `201`, la otra retorna `409`. Cero citas duplicadas
en la tabla `appointment`.

---

## Verificar Reporte de Cobertura

Tras ejecutar `./gradlew check`, abrir el reporte:

```
build/reports/jacoco/test/html/index.html
```

Verificar:
- **Cobertura total de instrucciones** ≥ 80 %
- **Cobertura por clase** > 80 % en cada clase de dominio y caso de uso
- Las clases bajo `generated/` no aparecen en el reporte (excluidas por configuración)

---

## Referencias

- Contrato API: [contracts/openapi.yml](contracts/openapi.yml)
- Modelo de datos: [data-model.md](data-model.md)
- Investigación técnica: [research.md](research.md)
- Constitución del proyecto: [.specify/memory/constitution.md](../../.specify/memory/constitution.md)
