# Reflexión — Quality & Governance Agent (caso: citasalud)

**Caso:** verificación de un servicio Spring Boot de reserva de citas médicas
(arquitectura hexagonal, Spec Kit) contra tres pilares no negociables: pruebas,
seguridad y criterios de aceptación.

## ¿Qué cambió en tu forma de "dar por terminado" el código cuando el veredicto lo decidió un gate determinista en vez de tu propio criterio?

La primera vez que auditué `citasalud`, hubiera defendido a capa y espada el
resultado inicial: 60 de 63 pruebas pasaban, la cobertura pasaba el 98%, y el
caso de uso de reserva "se veía" razonable. El gate no se conforma con eso, no
admite ningún tipo de conformidad parcial — no hay medias tintas, como se
dice, entre `APROBADO` y `BLOQUEADO`. Al correr `./gradlew test
jacocoTestReport` de verdad (nunca asumir que "ya deben pasar"), surgieron 3
fallos específicos, y el cruce contra el `spec.md` determinó que **RF-006**
(concurrencia: solo una reserva debe prosperar sobre la misma franja) no
tenía respaldo real. El gate me obligó a rastrear la causa raíz exacta
—`BookAppointmentUseCaseImpl.java:37` llamaba a `findById` en vez de
`findByIdForUpdate`, ignorando el lock pesimista que el propio adaptador ya
exponía— en vez de conformarme con "la mayoría de las pruebas pasan".
"Terminado" dejó de ser una impresión y pasó a ser una condición verificable:
o las 63 pruebas pasan y cada FR tiene asignada una prueba que lo demuestre,
o no llega a estar terminado, sin importar qué tan bien se vea el código a
simple vista.

## ¿Qué pilar te costó más dejar en verde —pruebas, seguridad o criterios—, y por qué?

El pilar de **criterios** fue el más complicado, porque este no se resuelve
con un comando: exige leer el `spec.md` línea a línea y decidir, para cada
`RF-xxx`, si hay una prueba que lo respalde de verdad y no solo un test con un
nombre similar. La condición de carrera de RF-006 es el ejemplo claro:
`ConcurrentBookingFunctionalTest` ya existía y tenía un nombre que daba por
hecho la cobertura de concurrencia, pero antes de la corrección **fallaba**
(esperaba `conflict == 1` y obtenía `0`), lo cual demuestra por qué la regla
es "sin una prueba que lo demuestre, nunca `cumple`" y no "si existe un test
con ese nombre, `cumple`". Los pilares de Pruebas y Seguridad, en cambio, se
resolvieron con evidencia directa (JaCoCo, Semgrep) que no admite nada a la
interpretación; los criterios exigen el juicio de leer la prueba y el
requisito, y ahí es donde es más fácil, bajo presión, dar por hecho algo que
solo "se parece" a estar cubierto.

## ¿Para qué te serviría un gate de Definition of Done (y el escaneo automático de seguridad vía MCP) en tu equipo real?

En mi equipo, la presión de entregar algo es precisamente el momento en que
"sabemos que falta una prueba pero lo dejamos para luego" se vuelve lo
normal, y ese "luego" casi nunca llega. Un gate que bloquea el merge —no que
lo recomienda— si la cobertura cae del umbral, si Semgrep encuentra una
crítica, o si un requisito del spec no tiene prueba, quita la decisión del
factor humano del momento y la convierte en un requisito verificado antes de
integrar. El escaneo de seguridad vía MCP (Semgrep) es igual de poderoso
porque no depende del factor humano: se ejecuta automáticamente como parte
del mismo flujo de trabajo que ya se hace para pasar pruebas y criterios, con
la misma autoridad. Lo más valioso que deja constancia este ejercicio —que
"la mayoría de las pruebas pasan" no es igual que "el requisito de
concurrencia está cubierto"— es exactamente el tipo de falla silenciosa que
un gate determinista, y no la buena voluntad del equipo, está diseñado para
atrapar.

## Cierre

Si repitiera el ejercicio, agregaría al `spec.md` una traza explícita
`RF-xxx → nombre del test` (en vez de inferirla por búsqueda) para que el
cruce criterios↔pruebas no dependa de que el auditor adivine correctamente
qué test corresponde a qué requisito.
