# Modelo de Datos: Reserva de Cita en Línea 24/7

**Fecha**: 2026-06-27 | **Plan**: [plan.md](plan.md)

> **Política de idiomas (Principio VI)**: Los nombres de clase, campo y tabla son identificadores
> de código → **inglés**. Las descripciones y reglas de negocio son documentación → **español**.

---

## Entidades de Dominio

### `Patient` — Paciente

Persona registrada en el sistema con capacidad de reservar citas.

| Campo (Java) | Tipo | Restricciones | Descripción |
|---|---|---|---|
| `id` | `UUID` | PK, NOT NULL | Identificador único |
| `name` | `String` | NOT NULL, max 150 | Nombre completo |
| `phone` | `String` | NOT NULL, max 20, único | Número WhatsApp (formato E.164) |
| `email` | `String` | max 255, único | Correo electrónico (opcional) |
| `registrationDate` | `Instant` | NOT NULL | Marca de tiempo de creación de cuenta |

**Reglas de negocio**:
- `phone` debe ser único en el sistema.
- La autenticación del paciente es prerrequisito externo (fuera de este US).

---

### `Doctor` — Médico

Profesional de salud con disponibilidad de agenda.

| Campo (Java) | Tipo | Restricciones | Descripción |
|---|---|---|---|
| `id` | `UUID` | PK, NOT NULL | Identificador único |
| `name` | `String` | NOT NULL, max 150 | Nombre completo |
| `specialty` | `String` | NOT NULL, max 100 | Especialidad médica principal |
| `locationId` | `UUID` | FK → `Location`, NOT NULL | Sede donde atiende |

**Reglas de negocio**:
- Un médico pertenece a exactamente una sede.
- El catálogo de médicos es de solo lectura para este US (gestionado por administración).

---

### `Location` — Sede

Ubicación física donde el médico atiende.

| Campo (Java) | Tipo | Restricciones | Descripción |
|---|---|---|---|
| `id` | `UUID` | PK, NOT NULL | Identificador único |
| `name` | `String` | NOT NULL, max 150 | Nombre de la sede |
| `address` | `String` | NOT NULL, max 300 | Dirección física |

---

### `TimeSlot` — Franja Horaria

Intervalo de tiempo en el calendario de un médico.

| Campo (Java) | Tipo | Restricciones | Descripción |
|---|---|---|---|
| `id` | `UUID` | PK, NOT NULL | Identificador único |
| `doctorId` | `UUID` | FK → `Doctor`, NOT NULL | Médico al que pertenece |
| `date` | `LocalDate` | NOT NULL | Fecha de la franja |
| `startTime` | `LocalTime` | NOT NULL | Hora de inicio |
| `endTime` | `LocalTime` | NOT NULL | Hora de fin |
| `status` | `TimeSlotStatus` | NOT NULL | `AVAILABLE` \| `BOOKED` |

**Restricción de base de datos**: `UNIQUE (doctor_id, date, start_time)` — garantiza que no
existan dos franjas duplicadas para el mismo médico en la misma fecha y hora.

**Reglas de negocio**:
- `endTime` DEBE ser posterior a `startTime`.
- El estado cambia de `AVAILABLE` a `BOOKED` de forma atómica durante la reserva usando
  `SELECT FOR UPDATE` para prevenir dobles reservas.
- Una franja con estado `BOOKED` no puede ser seleccionada por otro paciente.

**Transiciones de estado**:
```
AVAILABLE ──book()──→ BOOKED
BOOKED ──cancel()──→ AVAILABLE   (fuera del alcance de este US)
```

---

### `Appointment` — Cita

Reserva confirmada que asocia un paciente con una franja horaria.

| Campo (Java) | Tipo | Restricciones | Descripción |
|---|---|---|---|
| `id` | `UUID` | PK, NOT NULL | Identificador único |
| `patientId` | `UUID` | FK → `Patient`, NOT NULL | Paciente que reservó |
| `doctorId` | `UUID` | FK → `Doctor`, NOT NULL | Médico de la cita |
| `timeSlotId` | `UUID` | FK → `TimeSlot`, UNIQUE, NOT NULL | Franja reservada (1:1) |
| `status` | `AppointmentStatus` | NOT NULL | `CONFIRMED` \| `CANCELLED` |
| `createdAt` | `Instant` | NOT NULL | Marca de tiempo de confirmación |

**Restricción de base de datos**: `UNIQUE (time_slot_id)` — refuerza que una franja
sólo puede tener una cita activa.

**Reglas de negocio**:
- Una cita sólo se crea si la franja pasa de `AVAILABLE` a `BOOKED` en la misma transacción.
- Al crear la cita se publica un evento `AppointmentConfirmedEvent` para disparar la notificación.

**Transiciones de estado**:
```
(creación) ──→ CONFIRMED
CONFIRMED ──cancel()──→ CANCELLED   (fuera del alcance de este US)
```

---

### `WhatsAppNotification` — Notificación WhatsApp

Registro del intento de notificación al paciente tras confirmar una cita.

| Campo (Java) | Tipo | Restricciones | Descripción |
|---|---|---|---|
| `id` | `UUID` | PK, NOT NULL | Identificador único |
| `appointmentId` | `UUID` | FK → `Appointment`, NOT NULL | Cita asociada |
| `destinationNumber` | `String` | NOT NULL, max 20 | Número WhatsApp destino (E.164) |
| `content` | `String` | NOT NULL | Texto del mensaje enviado |
| `sendStatus` | `NotificationStatus` | NOT NULL | `PENDING` \| `SENT` \| `FAILED` |
| `attempts` | `Integer` | NOT NULL, default 0 | Número de intentos realizados |
| `createdAt` | `Instant` | NOT NULL | Marca de tiempo de creación del registro |
| `lastAttemptAt` | `Instant` | nullable | Marca de tiempo del último intento |

**Reglas de negocio**:
- El registro se crea con estado `PENDING` al publicarse el evento `AppointmentConfirmedEvent`.
- Máximo 3 reintentos con retroceso exponencial (1 min, 5 min, 30 min).
- Después de 3 fallos el estado queda `FAILED` y se registra para revisión manual.
- El fallo en la notificación NO revierte la cita.

---

## Diagrama de Relaciones

```
Location ──1:N──► Doctor ──1:N──► TimeSlot
                                      │
                                      │ 1:1
                                      ▼
Patient ───────────────────────► Appointment
                                      │
                                      │ 1:N
                                      ▼
                            WhatsAppNotification
```

---

## Esquema de Base de Datos (referencia para Flyway V1)

```sql
-- Tabla: location
CREATE TABLE location (
    id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name    VARCHAR(150) NOT NULL,
    address VARCHAR(300) NOT NULL
);

-- Tabla: doctor
CREATE TABLE doctor (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(150) NOT NULL,
    specialty   VARCHAR(100) NOT NULL,
    location_id UUID NOT NULL REFERENCES location(id)
);

-- Tabla: patient
CREATE TABLE patient (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name              VARCHAR(150) NOT NULL,
    phone             VARCHAR(20)  NOT NULL UNIQUE,
    email             VARCHAR(255) UNIQUE,
    registration_date TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- Tabla: time_slot
CREATE TABLE time_slot (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    doctor_id   UUID        NOT NULL REFERENCES doctor(id),
    date        DATE        NOT NULL,
    start_time  TIME        NOT NULL,
    end_time    TIME        NOT NULL,
    status      VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    CONSTRAINT uq_time_slot UNIQUE (doctor_id, date, start_time),
    CONSTRAINT chk_time     CHECK  (end_time > start_time),
    CONSTRAINT chk_ts_status CHECK (status IN ('AVAILABLE', 'BOOKED'))
);

-- Tabla: appointment
CREATE TABLE appointment (
    id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id   UUID        NOT NULL REFERENCES patient(id),
    doctor_id    UUID        NOT NULL REFERENCES doctor(id),
    time_slot_id UUID        NOT NULL UNIQUE REFERENCES time_slot(id),
    status       VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED',
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_appt_status CHECK (status IN ('CONFIRMED', 'CANCELLED'))
);

-- Tabla: whatsapp_notification
CREATE TABLE whatsapp_notification (
    id                 UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    appointment_id     UUID        NOT NULL REFERENCES appointment(id),
    destination_number VARCHAR(20) NOT NULL,
    content            TEXT        NOT NULL,
    send_status        VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    attempts           INTEGER     NOT NULL DEFAULT 0,
    created_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
    last_attempt_at    TIMESTAMPTZ,
    CONSTRAINT chk_notif_status CHECK (send_status IN ('PENDING', 'SENT', 'FAILED'))
);
```
