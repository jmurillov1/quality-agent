-- ── Tabla: location ──────────────────────────────────────────────────────────
CREATE TABLE location (
    id      UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
    name    VARCHAR(150) NOT NULL,
    address VARCHAR(300) NOT NULL
);

-- ── Tabla: doctor ─────────────────────────────────────────────────────────────
CREATE TABLE doctor (
    id          UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
    name        VARCHAR(150) NOT NULL,
    specialty   VARCHAR(100) NOT NULL,
    location_id UUID NOT NULL REFERENCES location(id)
);

-- ── Tabla: patient ────────────────────────────────────────────────────────────
CREATE TABLE patient (
    id                UUID      PRIMARY KEY DEFAULT RANDOM_UUID(),
    name              VARCHAR(150) NOT NULL,
    phone             VARCHAR(20)  NOT NULL UNIQUE,
    email             VARCHAR(255) UNIQUE,
    registration_date TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ── Tabla: time_slot ─────────────────────────────────────────────────────────
CREATE TABLE time_slot (
    id         UUID        PRIMARY KEY DEFAULT RANDOM_UUID(),
    doctor_id  UUID        NOT NULL REFERENCES doctor(id),
    date       DATE        NOT NULL,
    start_time TIME        NOT NULL,
    end_time   TIME        NOT NULL,
    status     VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    CONSTRAINT uq_time_slot  UNIQUE (doctor_id, date, start_time),
    CONSTRAINT chk_time      CHECK  (end_time > start_time),
    CONSTRAINT chk_ts_status CHECK  (status IN ('AVAILABLE', 'BOOKED'))
);

-- ── Tabla: appointment ───────────────────────────────────────────────────────
CREATE TABLE appointment (
    id           UUID        PRIMARY KEY DEFAULT RANDOM_UUID(),
    patient_id   UUID        NOT NULL REFERENCES patient(id),
    doctor_id    UUID        NOT NULL REFERENCES doctor(id),
    time_slot_id UUID        NOT NULL UNIQUE REFERENCES time_slot(id),
    status       VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED',
    created_at   TIMESTAMP   NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_appt_status CHECK (status IN ('CONFIRMED', 'CANCELLED'))
);

-- ── Tabla: whatsapp_notification ─────────────────────────────────────────────
CREATE TABLE whatsapp_notification (
    id                 UUID        PRIMARY KEY DEFAULT RANDOM_UUID(),
    appointment_id     UUID        NOT NULL REFERENCES appointment(id),
    destination_number VARCHAR(20) NOT NULL,
    content            VARCHAR(4000) NOT NULL,
    send_status        VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    attempts           INTEGER     NOT NULL DEFAULT 0,
    created_at         TIMESTAMP   NOT NULL DEFAULT NOW(),
    last_attempt_at    TIMESTAMP,
    CONSTRAINT chk_notif_status CHECK (send_status IN ('PENDING', 'SENT', 'FAILED'))
);
