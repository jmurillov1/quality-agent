# Especificación de Funcionalidad: Reserva de Cita en Línea 24/7

**Rama**: `001-reserva-cita-online`

**Creado**: 2026-06-27

**Estado**: Borrador

**Épica**: E-01 | **Historia**: US-01 | **Estimación**: 8 pts

**Entrada**: Como paciente, quiero reservar una cita en línea en cualquier momento del día,
para no tener que llamar durante mi horario de almuerzo ni acumular intentos fallidos.

---

## Escenarios de Usuario y Pruebas *(obligatorio)*

<!--
  Alineación con Constitución — Principio II (Pruebas BDD):
  - Los escenarios de aceptación DEBEN usar la estructura Dado/Cuando/Entonces.
  - Cada escenario se convierte en la especificación de al menos una prueba funcional.
  - Los nombres de métodos de prueba DEBEN seguir: dado_<contexto>_cuando_<accion>_entonces_<resultado>.
  - Las pruebas DEBEN escribirse primero y confirmarse como fallidas antes de la implementación.
-->

### Historia de Usuario 1 — Reserva exitosa fuera de horario de atención (Prioridad: P1)

Un paciente accede al portal de citas en cualquier momento del día (incluidos fines de semana
y fuera del horario de atención telefónica), selecciona un médico, elige una fecha y una
franja horaria disponible y confirma la reserva. El sistema registra la cita y envía una
confirmación al paciente por WhatsApp.

**Por qué esta prioridad**: Es el flujo de valor principal de la épica: habilitar la reserva
autónoma 24/7 es el objetivo central. Sin este camino feliz no hay producto.

**Prueba independiente**: Se puede probar completamente creando un paciente de prueba, navegando
el calendario de disponibilidad, confirmando una cita y verificando que la cita aparece
en el sistema y que se dispara la notificación WhatsApp (o su stub en entorno de prueba).

**Escenarios de aceptación**:

1. **Dado** que el paciente está autenticado y accede al sistema fuera del horario de
   atención telefónica (ej. 23:00),
   **Cuando** elige un médico disponible, selecciona una fecha y franja horaria libre y
   confirma la reserva,
   **Entonces** la cita queda registrada con estado "Confirmada", el paciente recibe un
   mensaje de confirmación por WhatsApp con los detalles (médico, fecha, hora, sede) y la
   franja queda bloqueada para otros pacientes.

2. **Dado** que el paciente está autenticado,
   **Cuando** consulta la lista de sus citas,
   **Entonces** la cita recién reservada aparece con estado "Confirmada" y los datos
   completos del médico y horario.

---

### Historia de Usuario 2 — Intento de reserva en franja no disponible (Prioridad: P2)

Un paciente intenta seleccionar una franja horaria que ya está ocupada (reservada por otro
paciente o bloqueada por el médico). El sistema le informa que no está disponible y le
facilita elegir otra opción.

**Por qué esta prioridad**: Protege la integridad del calendario y evita dobles reservas;
es el camino alternativo más probable durante el uso normal.

**Prueba independiente**: Se puede probar creando una cita previa en una franja específica y
luego intentando reservar la misma franja con otro paciente, verificando que el sistema
rechaza la segunda reserva y presenta alternativas.

**Escenarios de aceptación**:

1. **Dado** que una franja horaria de un médico ya está ocupada,
   **Cuando** un paciente intenta seleccionar esa franja y confirmarla,
   **Entonces** el sistema muestra la franja como no disponible (bloqueada visualmente),
   no permite confirmar la reserva y presenta un mensaje invitando al paciente a elegir
   otra franja u otro médico.

2. **Dado** que todas las franjas de un médico en una fecha están ocupadas,
   **Cuando** el paciente visualiza el calendario de ese médico para esa fecha,
   **Entonces** todas las franjas aparecen marcadas como no disponibles y el sistema
   sugiere al paciente buscar disponibilidad en otra fecha o con otro médico.

---

### Casos Extremos

- ¿Qué ocurre si la notificación WhatsApp falla después de confirmar la cita?
  → La cita queda registrada igualmente; el sistema reintenta la notificación de forma
  asíncrona y registra el fallo para reintentos (la reserva no se revierte por un fallo
  de notificación).
- ¿Qué ocurre si dos pacientes intentan reservar la misma franja simultáneamente?
  → El sistema garantiza exclusión mutua: sólo una reserva prosperará; la otra recibirá
  el mensaje de franja no disponible.
- ¿Qué pasa si el paciente pierde la conexión durante la confirmación?
  → La cita sólo se registra si el servidor procesa la confirmación completa; una
  respuesta de error o timeout debe indicar al paciente que reintente.

---

## Requisitos *(obligatorio)*

### Requisitos Funcionales

- **RF-001**: El sistema DEBE permitir al paciente autenticado consultar la disponibilidad
  de franjas horarias de médicos por especialidad, médico y fecha en cualquier momento del
  día los 7 días de la semana.
- **RF-002**: El sistema DEBE permitir al paciente reservar una franja horaria disponible
  confirmando médico, fecha y hora.
- **RF-003**: Una vez confirmada la reserva, el sistema DEBE registrar la cita con estado
  "Confirmada" y bloquear la franja para que no sea seleccionable por otros pacientes.
- **RF-004**: El sistema DEBE enviar una notificación de confirmación al paciente por
  WhatsApp con los datos de la cita (médico, especialidad, fecha, hora, sede) inmediatamente
  después de confirmar la reserva.
- **RF-005**: El sistema DEBE mostrar las franjas ya ocupadas como no disponibles e impedir
  su selección durante el flujo de reserva.
- **RF-006**: El sistema DEBE garantizar que dos reservas simultáneas sobre la misma franja
  sólo resulten en una cita confirmada (control de concurrencia).
- **RF-007**: El sistema DEBE registrar la cita en el historial de citas del paciente,
  consultable desde su perfil.
- **RF-008**: Si la notificación WhatsApp falla, el sistema DEBE registrar el fallo y
  reintentar la notificación de forma asíncrona sin revertir la reserva.

### Entidades Clave

- **`Patient`** — Paciente: Persona registrada en el sistema que puede autenticarse y reservar citas.
  Atributos clave: identificador, nombre, número de teléfono (WhatsApp), historial de citas.
- **`Doctor`** — Médico: Profesional de salud con una o más especialidades y un calendario de
  disponibilidad. Atributos clave: identificador, nombre, especialidad(es), sede.
- **`Location`** — Sede: Ubicación física donde el médico atiende. Atributos clave: identificador,
  nombre, dirección. Se asigna automáticamente a la cita según el médico seleccionado (ver Supuestos).
- **`TimeSlot`** — Franja Horaria: Intervalo de tiempo en el calendario de un médico que puede
  estar disponible u ocupado. Atributos clave: médico, fecha, hora inicio, hora fin, estado
  (`AVAILABLE` / `BOOKED`).
- **`Appointment`** — Cita: Reserva confirmada que asocia un paciente con una franja horaria.
  Atributos clave: paciente, médico, franja, estado (`CONFIRMED` / `CANCELLED`), marca de
  tiempo de creación.
- **`WhatsAppNotification`** — Notificación WhatsApp: Registro de comunicación enviado al
  paciente tras confirmarse una cita. Atributos clave: cita, número destino, contenido,
  estado de envío (`PENDING` / `SENT` / `FAILED`), intentos.

---

## Criterios de Éxito *(obligatorio)*

### Resultados Medibles

- **CE-001**: Un paciente puede completar el proceso completo de búsqueda y reserva de una
  cita en menos de 3 minutos desde que inicia sesión.
- **CE-002**: El sistema está disponible para recibir reservas el 100 % del tiempo (24/7),
  con un objetivo de disponibilidad ≥ 99.5 % mensual.
- **CE-003**: El 100 % de las citas confirmadas generan una notificación WhatsApp; el 95 %
  de las notificaciones se entregan en menos de 30 segundos tras la confirmación.
- **CE-004**: Cero dobles reservas en la misma franja horaria bajo carga concurrente
  (integridad del calendario garantizada al 100 %).
- **CE-005**: Los pacientes abandonan el flujo de reserva por franjas no disponibles en
  menos del 10 % de los intentos totales (gracias a una visualización clara de disponibilidad).
- **CE-006**: El 90 % de los pacientes que inician el flujo de reserva lo completan con
  éxito en el primer intento.

---

## Supuestos

- El paciente ya cuenta con una cuenta activa en el sistema y puede autenticarse; el
  registro y la autenticación de pacientes son funcionalidades preexistentes o se construyen
  en otra épica.
- **Riesgo aceptado y diferido**: dado que la autenticación se asume externa a este US, la API
  actual (`AppointmentsController`) no implementa autenticación ni autorización (no hay
  `spring-boot-starter-security` ni verificación de que el llamante sea el `patientId` consultado).
  Esto implica un riesgo de tipo IDOR (cualquier llamante puede leer el historial de citas de
  cualquier paciente conociendo su `patientId`). Este endpoint DEBE protegerse (verificar que el
  paciente autenticado sólo pueda consultar su propio historial) como parte de la épica de
  autenticación antes de exponer la API fuera de una red de confianza; no se resuelve en este US.
- El catálogo de médicos, especialidades y sedes ya existe en el sistema o se proveerá
  como dato inicial de prueba; la gestión del catálogo está fuera del alcance de esta épica.
- La gestión del calendario de disponibilidad de médicos (creación y bloqueo de franjas)
  es responsabilidad del personal administrativo y se asume disponible; la interfaz
  administrativa no es parte de este US.
- La integración con WhatsApp se realiza a través de la API oficial de WhatsApp Business
  (o un proveedor equivalente como Twilio); las credenciales del proveedor se gestionarán
  como configuración de entorno.
- El sistema operará bajo carga normal de una clínica mediana (estimado: hasta 500 reservas
  concurrentes en hora pico); requisitos de hiper-escala no aplican en esta versión.
- El idioma de la interfaz y las notificaciones es español colombiano.
- La sede se asigna automáticamente según el médico seleccionado; el paciente no elige sede
  directamente en este flujo.
