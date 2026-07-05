<!--
SYNC IMPACT REPORT
==================
Cambio de versión: 1.1.0 → 1.1.1
Principios modificados: Ninguno (aclaración no semántica)
Secciones añadidas: Ninguna
Secciones eliminadas: Ninguna
Correcciones:
  - Sección "Tecnologías y Herramientas" y compuerta de "Construcción verde": referencias a
    Maven reemplazadas por Gradle para reflejar la herramienta de construcción real del proyecto.
Plantillas actualizadas: Ninguna (no afecta plan-template.md)
TODOs pendientes: Ninguno
-->

# Constitución CitaSalud

## Principios Fundamentales

### I. Arquitectura Limpia

Toda funcionalidad DEBE implementarse siguiendo la Arquitectura Limpia definida por Robert C. Martin.
El código se organiza en capas concéntricas con reglas estrictas de dependencia:

- **Entidades** (capa más interna): Objetos de dominio y reglas de negocio esenciales. Sin dependencias externas.
- **Casos de Uso**: Lógica de negocio específica de la aplicación que orquesta entidades. Sin dependencias de frameworks o infraestructura.
- **Adaptadores de Interfaz**: Controladores, presentadores, gateways — traducen entre la capa de casos de uso y los formatos externos.
- **Frameworks y Drivers** (capa más externa): Spring Boot, JPA, REST, mensajería — todos intercambiables y reemplazables.

**Reglas**:
- Las flechas de dependencia DEBEN apuntar únicamente hacia adentro. Ninguna capa interna puede importar una capa externa.
- Las entidades y los casos de uso NO DEBEN depender de anotaciones de Spring, JPA ni de ninguna clase de framework.
- Cada caso de uso DEBE ser una clase independiente con un único punto de entrada `execute()` / `handle()`.
- Cualquier violación DEBE justificarse en la tabla de Seguimiento de Complejidad del plan.

### II. Estrategia de Pruebas BDD

Todas las pruebas DEBEN seguir las convenciones de Desarrollo Guiado por Comportamiento (BDD)
usando la estructura Dado/Cuando/Entonces.

**Niveles de prueba requeridos**:
- **Pruebas unitarias**: Cada caso de uso, entidad y servicio de dominio DEBE tener pruebas unitarias. Los mocks se permiten únicamente en el límite de la capa de casos de uso (puertos/gateways).
- **Pruebas de integración**: Cada adaptador (repositorio, controlador REST, cliente externo) DEBE tener pruebas de integración contra infraestructura real (BD en memoria o Testcontainers).
- **Pruebas funcionales/E2E**: Cada historia de usuario DEBE tener al menos una prueba funcional que valide el slice vertical completo de extremo a extremo.

**Regla de formato**: Los nombres de los métodos de prueba DEBEN seguir el patrón
`dado_<contexto>_cuando_<accion>_entonces_<resultado>` o una convención BDD descriptiva equivalente.
No se permiten nombres vagos como `testCreate()` o `test1()`.

**Regla de orden**: Las pruebas DEBEN escribirse primero y confirmarse como fallidas antes de comenzar la implementación (Rojo-Verde-Refactorizar).

### III. Buenas Prácticas de Programación

Todo el código DEBE cumplir los siguientes principios — las violaciones requieren justificación explícita:

- **SOLID**:
  - Responsabilidad Única: cada clase tiene exactamente una razón para cambiar.
  - Abierto/Cerrado: las clases están abiertas para extensión y cerradas para modificación.
  - Sustitución de Liskov: los subtipos DEBEN ser completamente sustituibles por sus tipos base.
  - Segregación de Interfaces: ningún cliente debe depender de interfaces que no utiliza.
  - Inversión de Dependencias: depender de abstracciones, no de concreciones; inyectar en la raíz de composición.
- **YAGNI**: No se escribe código especulativo. Cada clase, método y atributo DEBE responder a un requisito actual y documentado.
- **DRY**: La lógica duplicada más de una vez DEBE extraerse. La duplicación accidental (mismas palabras, conceptos distintos) NO DEBE colapsarse.

**Nomenclatura y claridad**: El código DEBE ser autoexplicativo. Los comentarios sólo se permiten para explicar restricciones o invariantes no obvios, no para describir lo que hace el código.

### IV. API First con OpenAPI

Toda superficie de API DEBE diseñarse primero mediante contrato antes de comenzar cualquier implementación.

- Un **contrato OpenAPI 3.x** (`openapi.yml`) DEBE existir en `specs/[###-feature]/contracts/`
  antes de iniciar la fase de implementación.
- Los stubs de servidor y los SDKs de cliente DEBEN generarse a partir del contrato usando
  **openapi-generator** (plugin de Gradle/Maven o CLI). No se permiten firmas de controladores escritas
  a mano que difieran de las interfaces generadas.
- El contrato es la única fuente de verdad. Si el contrato cambia, el generador DEBE ejecutarse
  nuevamente y los archivos generados actualizarse antes de hacer merge.
- Los cambios que rompan la compatibilidad de un contrato publicado DEBEN incrementar la versión
  mayor de la API y documentarse en una guía de migración.

### V. Métricas de Cobertura y Calidad

La cobertura es una compuerta de calidad obligatoria aplicada por **JaCoCo**.

- **Cobertura por clase**: cada clase DEBE alcanzar **> 80 %** de cobertura de líneas y ramas.
- **Cobertura global**: la cobertura agregada del proyecto DEBE ser **≥ 80 %** de líneas.
- JaCoCo DEBE configurarse para **fallar la construcción** cuando alguno de los umbrales no se cumpla.
- Los reportes de cobertura DEBEN generarse en cada ejecución de CI y almacenarse como artefactos de construcción.
- El código generado (salida de openapi-generator) DEBE excluirse de la medición de cobertura
  mediante la configuración `<excludes>` de JaCoCo.
- La cobertura no es un sustituto de la calidad de las pruebas; las pruebas DEBEN verificar
  comportamientos significativos, no simplemente ejecutar líneas.

### VI. Política de Idiomas

El proyecto mantiene una separación estricta de idiomas según el tipo de artefacto:

**Código fuente e identificadores — INGLÉS obligatorio**:
- Nombres de clases, interfaces, métodos, variables, constantes y paquetes.
- Rutas de API (paths, operationIds, nombres de parámetros y esquemas OpenAPI).
- Comentarios técnicos inline en el código.
- Mensajes de log internos y códigos de error del sistema.
- Mensajes de commit de Git.

**Documentación y comunicación — ESPAÑOL obligatorio**:
- Archivos de documentación (README, guías, quickstart, CLAUDE.md).
- Especificaciones de funcionalidad (spec.md, plan.md, research.md, data-model.md).
- Contratos OpenAPI: campo `description` de paths, operaciones, parámetros y esquemas.
- Historias de usuario y escenarios BDD (Dado/Cuando/Entonces).
- Mensajes de error visibles al usuario final.
- Respuestas y explicaciones del asistente de desarrollo.
- Checklists, reportes y manuales de validación.

**Regla de verificación**: En code review se DEBE rechazar cualquier PR que mezcle idiomas
dentro de un mismo artefacto (ej. variables en español en código Java, o explicaciones en
inglés en un spec.md).

## Tecnologías y Herramientas

**Lenguaje/Runtime**: Java 17+ con Spring Boot 3.x.

**Framework de pruebas**: JUnit 5 + AssertJ + Mockito para pruebas unitarias; Spring Boot Test +
Testcontainers para pruebas de integración.

**Soporte BDD**: Cucumber (opcional para pruebas funcionales) o JUnit 5 con nomenclatura
descriptiva Dado/Cuando/Entonces.

**Generación de API**: `openapi-generator-maven-plugin` — la generación está configurada para
producir interfaces Spring bajo `target/generated-sources`; las clases de implementación residen
en `src/main/`.

**Cobertura**: Plugin JaCoCo — tareas `test` (con agente habilitado) y `jacocoTestReport` +
`jacocoTestCoverageVerification` vinculadas a `check`. Umbrales mínimos establecidos según el Principio V.

**Construcción**: Gradle con versiones de plugins forzadas. Sin dependencias snapshot en ramas liberadas.

## Flujo de Desarrollo y Compuertas de Calidad

Las siguientes compuertas DEBEN superarse antes de hacer merge de una rama de funcionalidad:

1. **Contrato existente**: `openapi.yml` presente y válido (lint con swagger-cli u openapi-lint).
2. **Pruebas escritas primero**: El historial de Git DEBE mostrar commits de prueba anteriores a los commits de implementación para cada historia de usuario.
3. **Construcción verde**: `./gradlew check` pasa con cero fallos de prueba.
4. **Compuerta de cobertura**: El goal `check` de JaCoCo reporta éxito (≥ 80 % global, > 80 % por clase).
5. **Guardián de arquitectura**: ArchUnit o prueba equivalente confirma que no hay importaciones de capas externas en capas internas.
6. **Revisión de código**: Al menos una revisión por pares que apruebe el cumplimiento de los Principios I–VI.

Cualquier omisión de compuerta DEBE documentarse como un ticket de Deuda Técnica con un plan de remediación.

## Gobernanza

- Esta constitución reemplaza todas las convenciones de codificación y acuerdos informales previos.
- Las enmiendas requieren: propuesta escrita, justificación de motivación, incremento de versión según política SemVer y actualización de todas las plantillas dependientes en el mismo commit.
- **Política de versionado**:
  - MAYOR — eliminación, redefinición de principios o cambio de gobernanza incompatible hacia atrás.
  - MENOR — nuevo principio o sección añadido, o expansión material de la guía.
  - PARCHE — aclaraciones, redacción, corrección de errores tipográficos, refinamientos no semánticos.
- El cumplimiento se revisa al inicio de cada fase del plan (compuerta Constitution Check en plan.md).
- La sección Constitution Check de la plantilla de plan DEBE listar cada principio con estado PASA/FALLA/N/A antes de que se permita avanzar a la investigación de la Fase 0.

**Versión**: 1.1.1 | **Ratificado**: 2026-06-27 | **Última modificación**: 2026-07-05
