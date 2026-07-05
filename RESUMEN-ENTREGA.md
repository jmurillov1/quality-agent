# Resumen de la entrega — Quality & Governance Agent (caso: citasalud)

**Repositorio:** https://github.com/jmurillov1/quality-agent

**Caso elegido:** verificación de calidad de `citasalud` — reserva de citas
médicas (ediciones de agenda, disponibilidad de doctores, confirmación y
notificación por WhatsApp), arquitectura hexagonal, para un servicio ya
implementado que hoy necesita pasar el gate de Definition of Done antes de
integrarse.

## Insumo recibido

El servicio Spring Boot `citasalud` ya construido, colocado en
`examples/citasalud/`:

- `src/main/java/org/ups/citasalud/` — dominio, casos de uso, adaptadores
  (web, persistencia JPA, notificación WhatsApp).
- `src/test/java/...` — 63 pruebas (unitarias, funcionales con
  Testcontainers, ArchUnit).
- `specs/001-reserva-cita-online/spec.md` — Spec Kit: Functional Requirements
  (`RF-xxx`), Acceptance Scenarios y Edge Cases.
- `.specify/memory/constitution.md` — umbral de cobertura (≥ 80%).

## Equipo de agentes configurado

- `CLAUDE.md` — constitución: cero invención, "el gate manda, no el modelo",
  "devolver es éxito", idioma español.
- Skill `quality` — Definition of Done, umbrales, esquema canónico de
  `verification.json`, cómo se leen los criterios del `spec.md`.
- Dos subagentes en `.claude/agents/`: `auditor` (rol central, cita evidencia
  por pilar) y `security-reviewer` (usa el MCP de Semgrep).
- Comandos `/quality:verify`, `/quality:review-api`, `/quality:review-architecture`
  y `/quality:generate-report`.
- Hook `quality-gate.py` (`PreToolUse`, matcher `Write|Edit`), registrado en
  `.claude/settings.json`, que custodia la escritura de `verification.json`
  verificando pruebas + seguridad + criterios contra el `spec.md`.
- `.mcp.json` — conexión al servidor MCP de Semgrep (pilar de Seguridad).

## Flujo de entrega ejecutado

1. `/quality:verify examples/citasalud` (Auditor) → ejecuta
   `./gradlew test jacocoTestReport`, delega en `security-reviewer` para
   Semgrep, y cruza cada `RF-xxx` del `spec.md` contra la prueba que lo cubre.
   **Primera corrida — BLOQUEADO:** 3 de 63 pruebas fallan; **RF-002, RF-003
   y RF-006 (concurrencia) en `incumple`**.
   Paso vigilado por el gate — bloqueó la escritura de `verification.json`
   citando la causa raíz exacta: `BookAppointmentUseCaseImpl.java:37` usaba
   `findById(...)` en vez de `findByIdForUpdate(...)`, sin adquirir el lock
   pesimista que el adaptador JPA ya exponía, así que dos reservas
   concurrentes sobre la misma franja podían prosperar ambas.
2. Corrección del código real (no del JSON): se cambió la llamada a
   `findByIdForUpdate`.
3. `/quality:verify examples/citasalud` de nuevo → **APROBADO:** 63/63
   pruebas, cobertura 98.5% (≥ 80%), 0 críticas/secretos vía Semgrep MCP
   (2 "high" informativos por falta de autenticación en los controladores,
   ya documentados como riesgo aceptado/diferido en el propio `spec.md`), y
   **10/10 Functional Requirements en `cumple`**, incluido RF-006 verificado
   por `ConcurrentBookingFunctionalTest` con dos hilos reales.
4. `/quality:generate-report examples/citasalud` → `report.html`, reporte
   visual que aplica la misma regla del gate (nunca recalcula la verdad por
   su cuenta).

Todo en `examples/citasalud/quality-output/`.

## Demostración del gate (obligatorio)

El bloqueo no fue simulado: fue el resultado real de auditar el código tal
como estaba. El hook `quality-gate.py` interceptó la escritura de
`verification.json` en la primera corrida y devolvió `exit 2`, señalando por
pilar qué fallaba (pruebas, y RF-006 de concurrencia sin cumplir). Al
corregir el código y volver a correr `/quality:verify`, el gate escribió el
archivo y devolvió `exit 0`.

**Video de la demo (bloqueo → corrección → aprobación):** https://youtu.be/edk2huJmQgo

[![Ver video de la demo del gate](https://img.youtube.com/vi/edk2huJmQgo/maxresdefault.jpg)](https://youtu.be/edk2huJmQgo)

El archivo original también queda en el repo como respaldo:
[`quality-agent-dod-gate-evidence.mp4`](./quality-agent-dod-gate-evidence.mp4).

## Verificación de trazabilidad

Se contrastó el cruce de tres `RF-xxx` contra su prueba concreta: **RF-006**
(concurrencia) contra `ConcurrentBookingFunctionalTest` (dos hilos reales,
`created == 1`, `conflict == 1`, `appointmentRepo.count() == 1`); **RF-002 y
RF-003** contra `BookAppointmentUseCaseTest`; y el pilar de Seguridad contra
el escaneo del subagente `security-reviewer` (52 archivos de
`src/main/java`, 0 hallazgos críticos de Semgrep, 2 "high" informativos con
archivo:línea). Ningún criterio quedó en `cumple` sin la prueba que lo
demuestra; donde la evidencia era insuficiente, el auditor usó `incumple`,
nunca una aprobación optimista.

## Reflexión

[`REFLEXION.md`](./REFLEXION.md) — responde qué cambió en la forma de dar por
terminado el código cuando el veredicto lo decide un gate determinista en vez
del propio criterio, qué pilar costó más dejar en verde y por qué, y para qué
serviría un gate de Definition of Done (y el escaneo automático de seguridad
vía MCP) en un equipo real.
