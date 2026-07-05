# Investigación: Reserva de Cita en Línea 24/7

**Fecha**: 2026-06-27 | **Plan**: [plan.md](plan.md)

---

## 1. Control de Concurrencia en Reserva de Franjas Horarias

**Decisión**: Bloqueo pesimista (`SELECT … FOR UPDATE`) sobre la fila `franja_horaria` durante
la transacción de reserva, combinado con una restricción `UNIQUE` en base de datos sobre
`(medico_id, fecha, hora_inicio)` como red de seguridad.

**Rationale**: El modelo de datos tiene una cardinalidad baja de conflictos reales (dos
pacientes raramente compiten por el mismo médico/hora exacta), pero el impacto de una doble
reserva es crítico. El bloqueo pesimista a nivel de fila evita la condición de carrera sin
comprometer el throughput en escenarios de baja contención. La restricción UNIQUE actúa como
última línea de defensa a nivel de base de datos.

**Alternativas consideradas**:
- *Bloqueo optimista (versión/timestamp)*: Descartado porque el ciclo read-conflict-retry en
  alta contención puntual (hora pico) degrada la experiencia del usuario con errores visibles.
- *Redis / lock distribuido*: Descartado — añade una dependencia de infraestructura no
  justificada para la escala objetivo (500 concurrentes).

---

## 2. Estrategia de Notificación WhatsApp

**Decisión**: Notificación asíncrona mediante un evento de dominio publicado tras confirmar la
cita. Un listener (adaptador de salida) consume el evento e invoca la API de WhatsApp Business
vía Twilio SDK. Si falla, se registra el intento en `notificacion_whatsapp` y un scheduler
reintenta hasta 3 veces con retroceso exponencial (1 min, 5 min, 30 min).

**Rationale**:
- La reserva no debe fallar por un problema transitorio del proveedor de mensajería.
- El desacoplamiento mediante evento garantiza que la transacción de reserva sea atómica e
  independiente del envío del mensaje.
- Twilio es el proveedor con mayor cobertura en Colombia y soporte oficial de WhatsApp Business.

**Alternativas consideradas**:
- *Notificación síncrona inline*: Descartado — cualquier latencia o fallo de Twilio impacta
  directamente el tiempo de respuesta de la reserva y la experiencia del usuario.
- *Meta Cloud API directa*: Viable pero requiere aprobación de cuenta Business verificada que
  puede demorar; Twilio abstrae esa complejidad.

---

## 3. Generación de API con openapi-generator

**Decisión**: Usar `openapi-generator-maven-plugin` en modo `spring` con opción
`interfaceOnly=true`. Los controladores implementan las interfaces generadas; los modelos de
request/response viven en `target/generated-sources` y se excluyen de JaCoCo y de control de
versiones (`.gitignore`).

**Rationale**: `interfaceOnly=true` evita que el generador sobrescriba la implementación en
cada build. Los DTOs generados se reutilizan directamente, eliminando mapeo manual y
cumpliendo el principio DRY.

**Alternativas consideradas**:
- *Generación de cliente desde spec*: Complementario, no reemplaza — se puede añadir en el
  futuro para pruebas de contrato.
- *SpringDoc / Springfox (code-first)*: Descartado — invierte el flujo API First; la
  especificación quedaría subordinada al código.

---

## 4. Framework y Versión Java

**Decisión**: Java 21 LTS (`release=21`, toolchain 25) + Spring Boot 4.1.x.

**Rationale**: Durante la implementación se adoptó Java 21 LTS en lugar de Java 17 — Spring Boot
4.x requiere Java 17 como mínimo, pero el equipo optó por Java 21 para aprovechar mejoras de
rendimiento (virtual threads, GC) disponibles antes del sprint siguiente, evitando una migración
posterior. Gradle (no Maven) se usó como herramienta de construcción por preferencia del equipo
y mejor soporte incremental de builds.

**Alternativas consideradas**:
- *Java 17 LTS*: Descartado en favor de Java 21 LTS por las razones anteriores.
- *Quarkus / Micronaut*: Descartados por menor ecosistema de soporte en el equipo y mayor curva
  de aprendizaje frente a Spring Boot.
- *Maven*: Descartado en favor de Gradle — mejor caché incremental de build y sintaxis DSL más
  concisa para el equipo.

---

## 5. Base de Datos

**Decisión**: PostgreSQL 15.

**Rationale**: Soporte robusto de `SELECT FOR UPDATE`, `UNIQUE` constraints, JSONB para
metadatos opcionales, y amplia disponibilidad en proveedores cloud (AWS RDS, Azure Database).
Flyway gestiona las migraciones de esquema de forma reproducible.

**Alternativas consideradas**:
- *MySQL/MariaDB*: Viable; `SELECT FOR UPDATE` soportado, pero menor expressividad SQL y tipos
  de datos.
- *H2 (sólo tests)*: Usado en pruebas unitarias ligeras; Testcontainers con PostgreSQL real para
  pruebas de integración garantiza paridad con producción.

---

## 6. Arquitectura de Pruebas BDD

**Decisión**: JUnit 5 con nomenclatura `dado_<contexto>_cuando_<accion>_entonces_<resultado>` en
inglés técnico o español (consistencia por módulo). Cucumber es **opcional** — únicamente si el
equipo de producto requiere escenarios `.feature` legibles por negocio; de lo contrario, las
pruebas JUnit 5 son suficientes y mantienen el stack más simple (YAGNI).

**Niveles**:
| Nivel | Herramientas | Scope | Ubicación |
|-------|-------------|-------|-----------|
| Unitario | JUnit 5 + Mockito + AssertJ | Use cases, entidades, lógica de dominio | `test/…/domain/`, `test/…/application/` |
| Integración | Spring Boot Test + Testcontainers | Adaptadores JPA, controladores REST | `test/…/adapter/` |
| Funcional | Spring Boot Test (MockMvc o RestAssured) | Slice completo por historia de usuario | `test/…/functional/` |

**Alternativas consideradas**:
- *Cucumber obligatorio*: Descartado para MVP — añade overhead de configuración sin beneficio
  claro si el equipo no tiene rol QA dedicado.
- *RestAssured para todos los niveles*: Sólo en funcionales; MockMvc es suficiente para
  pruebas de integración de controladores.
