-- Datos de prueba para CitaSalud
-- ── Sedes ─────────────────────────────────────────────────────────────────────
INSERT INTO location (id, name, address) VALUES
    ('a1000000-0000-0000-0000-000000000001', 'Sede Norte',     'Cra 15 # 80-45, Bogotá'),
    ('a1000000-0000-0000-0000-000000000002', 'Sede Chapinero', 'Cra 13 # 58-20, Bogotá');

-- ── Médicos ───────────────────────────────────────────────────────────────────
INSERT INTO doctor (id, name, specialty, location_id) VALUES
    ('b1000000-0000-0000-0000-000000000001', 'Dr. Carlos Ramírez', 'Medicina General', 'a1000000-0000-0000-0000-000000000001'),
    ('b1000000-0000-0000-0000-000000000002', 'Dra. Ana Torres',    'Medicina General', 'a1000000-0000-0000-0000-000000000001'),
    ('b1000000-0000-0000-0000-000000000003', 'Dr. Luis Peña',      'Pediatría',        'a1000000-0000-0000-0000-000000000002'),
    ('b1000000-0000-0000-0000-000000000004', 'Dra. María Gómez',   'Cardiología',      'a1000000-0000-0000-0000-000000000002'),
    ('b1000000-0000-0000-0000-000000000005', 'Dr. Jorge Vargas',   'Dermatología',     'a1000000-0000-0000-0000-000000000001');

-- ── Pacientes de prueba ───────────────────────────────────────────────────────
INSERT INTO patient (id, name, phone, email, registration_date) VALUES
    ('c1000000-0000-0000-0000-000000000001', 'Juanita Pérez', '+573001234567', 'juanita@ejemplo.com', NOW()),
    ('c1000000-0000-0000-0000-000000000002', 'Pedro López',   '+573009876543', 'pedro@ejemplo.com',   NOW());

-- ── Franjas horarias (próximos días para Dr. Carlos Ramírez) ─────────────────
INSERT INTO time_slot (id, doctor_id, date, start_time, end_time, status) VALUES
    ('d1000000-0000-0000-0000-000000000001', 'b1000000-0000-0000-0000-000000000001', DATEADD(DAY, 1, CURRENT_DATE), '08:00', '08:30', 'AVAILABLE'),
    ('d1000000-0000-0000-0000-000000000002', 'b1000000-0000-0000-0000-000000000001', DATEADD(DAY, 1, CURRENT_DATE), '08:30', '09:00', 'AVAILABLE'),
    ('d1000000-0000-0000-0000-000000000003', 'b1000000-0000-0000-0000-000000000001', DATEADD(DAY, 1, CURRENT_DATE), '09:00', '09:30', 'AVAILABLE'),
    ('d1000000-0000-0000-0000-000000000004', 'b1000000-0000-0000-0000-000000000001', DATEADD(DAY, 2, CURRENT_DATE), '10:00', '10:30', 'AVAILABLE'),
    ('d1000000-0000-0000-0000-000000000005', 'b1000000-0000-0000-0000-000000000001', DATEADD(DAY, 2, CURRENT_DATE), '10:30', '11:00', 'AVAILABLE'),
    ('d1000000-0000-0000-0000-000000000006', 'b1000000-0000-0000-0000-000000000002', DATEADD(DAY, 1, CURRENT_DATE), '14:00', '14:30', 'AVAILABLE'),
    ('d1000000-0000-0000-0000-000000000007', 'b1000000-0000-0000-0000-000000000002', DATEADD(DAY, 1, CURRENT_DATE), '14:30', '15:00', 'AVAILABLE');
