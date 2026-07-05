# Resumen de la entrega — Quality & Governance Agent (caso: citasalud)

**Repositorio:** https://github.com/jmurillov1/quality-agent

**Caso elegido:** verificación de calidad de `citasalud`, un servicio Spring
Boot de reserva de citas médicas (arquitectura hexagonal), ya implementado,
contra los tres pilares no negociables: pruebas, seguridad y criterios de
aceptación.

## Qué se subió

- El proyecto completo del Quality & Governance Agent: `.claude/` (subagentes
  `auditor` y `security-reviewer`, skill `quality`, comandos `/quality:*`, el
  hook determinista `.claude/hooks/quality-gate.py`) más `.mcp.json` (conexión
  al servidor MCP de Semgrep).
- El servicio verificado `examples/citasalud/` completo: `specs/` (Spec Kit,
  con los Functional Requirements RF-xxx), `.specify/`, `src/main` y
  `src/test`.
- Los artefactos generados por el agente:
  `examples/citasalud/quality-output/verification.json` (evidencia citada,
  archivo:línea) y `quality-output/report.html` (reporte visual, generado con
  `.claude/scripts/build-report.py` a partir del JSON — nunca recalcula la
  verdad por su cuenta).

## Flujo de verificación ejecutado

1. `/quality:verify examples/citasalud` → el orquestador delega en el
   subagente `auditor` (y este en `security-reviewer` para Semgrep).
2. **Primera corrida — BLOQUEADO:** `./gradlew test jacocoTestReport` con
   3 de 63 pruebas fallando; el cruce contra `spec.md` marcó
   **RF-002, RF-003 y RF-006 (concurrencia) en `incumple`**. Causa raíz citada
   por el auditor: `BookAppointmentUseCaseImpl.java:37` usaba
   `timeSlotRepository.findById(...)` en vez de `findByIdForUpdate(...)`, sin
   adquirir el lock pesimista que el propio adaptador JPA ya exponía — dos
   reservas concurrentes sobre la misma franja podían prosperar ambas.
3. Corrección del código real (no del JSON): se cambió la llamada a
   `findByIdForUpdate`.
4. **Segunda corrida — APROBADO:** `./gradlew clean test jacocoTestReport` con
   **63/63 pruebas**, cobertura 98.5% (≥ 80%), 0 críticas/secretos vía Semgrep
   MCP (2 "high" informativos por falta de autenticación en los controladores,
   ya documentados como riesgo aceptado/diferido en el propio `spec.md`), y
   **10/10 Functional Requirements en `cumple`**, incluido RF-006 verificado
   por `ConcurrentBookingFunctionalTest` con dos hilos reales.
5. `/quality:generate-report examples/citasalud` → `report.html`.

## Demostración del gate (obligatorio)

El bloqueo no fue simulado: fue el resultado real de correr el auditor sobre
el código tal como estaba, con un requisito de concurrencia (RF-006) sin
cumplir por un bug real de bloqueo optimista/pesimista. El gate
(`.claude/hooks/quality-gate.py`) intercepta la escritura de
`verification.json` y decide pasa/bloquea leyendo los tres pilares — nunca el
modelo por su cuenta.

**Video de la demo (bloqueo → corrección → aprobación):**
https://youtu.be/edk2huJmQgo

## Verificación de trazabilidad

Cada hallazgo en `verification.json` cita su fuente exacta: el comando de
Gradle ejecutado, el XML de JaCoCo con fecha posterior a la corrida, el
subagente `security-reviewer` con el conteo de archivos escaneados por
Semgrep, y para cada `RF-xxx` la prueba concreta (archivo y, cuando aplica,
línea) que lo demuestra. Ningún criterio se marcó `cumple` sin la prueba que
lo respalda; donde la evidencia era insuficiente, el auditor usó `incumple`,
nunca una aprobación optimista.

## Reflexión

[`REFLEXION.md`](./REFLEXION.md) — responde qué cambió en la forma de dar por
terminado el código cuando el veredicto lo decide un gate determinista, qué
pilar costó más dejar en verde y por qué, y para qué serviría este tipo de
gobernanza ejecutable en un equipo real.
