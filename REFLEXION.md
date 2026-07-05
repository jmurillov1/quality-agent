# Reflexión — Quality & Governance Agent (caso: citasalud)

**Caso:** verificación de un servicio Spring Boot de reserva de citas médicas
(arquitectura hexagonal, Spec Kit) contra tres pilares no negociables: pruebas,
seguridad y criterios de aceptación.

## ¿Qué cambió en tu forma de "dar por terminado" el código cuando el veredicto lo decidió un gate determinista en vez de tu propio criterio?

Al auditar `citasalud` la primera vez, mi lectura del código me hubiera dejado
tranquilo con un vistazo superficial: 60 de 63 pruebas pasaban, la cobertura
reportada rondaba el 98%, y el caso de uso de reserva "se veía" razonable. El
gate no permite ese tipo de conformidad parcial — no hay un punto intermedio
entre `APROBADO` y `BLOQUEADO`. Al ejecutar `./gradlew test jacocoTestReport`
de verdad (no asumir que "ya deben pasar"), aparecieron 3 fallos concretos, y
el cruce contra el `spec.md` señaló que **RF-006** (concurrencia: solo una
reserva debe prosperar sobre la misma franja) no tenía respaldo real. El gate
me obligó a rastrear la causa raíz exacta —`BookAppointmentUseCaseImpl.java:37`
llamaba a `findById` en vez de `findByIdForUpdate`, ignorando el lock
pesimista que el propio adaptador ya exponía— en vez de conformarme con "la
mayoría de las pruebas pasan". "Terminado" dejó de ser una impresión y pasó a
ser una condición verificable: o las 63 pruebas pasan y cada FR tiene una
prueba que lo demuestra, o no está terminado, sin importar cuán razonable
parezca el código a simple vista.

## ¿Qué pilar te costó más dejar en verde —pruebas, seguridad o criterios—, y por qué?

El pilar de **criterios** fue el más difícil, precisamente porque es el único
que no se resuelve corriendo un comando: exige leer el `spec.md` línea por
línea y decidir, para cada `RF-xxx`, si existe una prueba que lo ejercite de
verdad y no solo un test con un nombre parecido. La condición de carrera de
RF-006 es el ejemplo claro: `ConcurrentBookingFunctionalTest` ya existía y
tenía un nombre que sugería cobertura de concurrencia, pero antes de la
corrección **fallaba** (esperaba `conflict == 1` y obtenía `0`), lo cual
demuestra por qué la regla es "sin prueba que lo demuestre, nunca `cumple`" y
no "si existe un test con ese nombre, `cumple`". Pruebas y seguridad, en
cambio, se resolvieron con evidencia mecánica (JaCoCo, Semgrep) que no admite
interpretación; criterios exige el juicio de leer la prueba y el requisito, y
ahí es donde es más fácil, bajo presión, dar por cumplido algo que solo "se
parece" a estar cubierto.

## ¿Para qué te serviría un gate de Definition of Done (y el escaneo automático de seguridad vía MCP) en tu equipo real?

En un equipo real, la presión de fecha de entrega es exactamente el momento en
que "sabemos que falta ese test pero lo agregamos después" se vuelve la norma,
y ese "después" casi nunca llega. Un gate que bloquea el merge —no que lo
recomienda— si la cobertura cae del umbral, si Semgrep encuentra una crítica,
o si un requisito del spec no tiene prueba, saca esa decisión de la
negociación humana del momento y la convierte en una condición objetiva
verificada antes de integrar. El escaneo de seguridad vía MCP (Semgrep) es
igual de valioso porque no depende de que alguien se acuerde de correrlo
manualmente antes de cada release: corre como parte del mismo flujo que ya se
ejecuta para verificar pruebas y criterios, con la misma autoridad. La lección
concreta de este ejercicio —que "la mayoría de las pruebas pasan" no es lo
mismo que "el requisito de concurrencia está cubierto"— es exactamente el tipo
de brecha silenciosa que un gate determinista, y no la buena voluntad del
equipo, está diseñado para atrapar.

## Cierre

Si repitiera el ejercicio, agregaría al `spec.md` una traza explícita
`RF-xxx → nombre del test` (en vez de inferirla por búsqueda) para que el
cruce criterios↔pruebas no dependa de que el auditor adivine correctamente
qué test corresponde a qué requisito.
