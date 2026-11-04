# Skill: android-feature-flow
**Versión**: 1.0
**Contexto**: Implementación Vertical de Funcionalidades (Android + Backend)

## 1. Propósito
Guiar la implementación o extensión de funcionalidades completas en el ecosistema de Formas Perfectas, asegurando una integración vertical sin omisiones en las capas pertinentes y respetando estrictamente la arquitectura real y los patrones existentes.

## 2. Activación de la Skill
Se activa al recibir solicitudes de:
*   Creación de nuevas funcionalidades o pantallas.
*   Extensión de lógica de negocio existente que afecte múltiples capas.
*   Implementación de flujos que requieran nuevos endpoints o persistencia.

## 3. Principios de Implementación Real
*   **Consistencia de Patrón**: Antes de crear un Repository, verificar si la feature usa `Interface + Impl` o `Clase Directa`. Mantener el patrón detectado.
*   **Arquitectura Real**: Respetar MVVM en Android. Mantener la ausencia actual de UseCases y no introducirlos salvo que el usuario lo autorice explícitamente. En Backend, respetar la arquitectura `Controllers + Data` (sin Service layer).
*   **Navegación Selectiva**: Solo registrar una ruta en `Constants.Routes` y `AppNav.kt` cuando la funcionalidad requiera navegación independiente dentro del NavHost existente. Los Composables internos, componentes, diálogos, tabs o contenidos pertenecientes a una pantalla existente no requieren necesariamente una nueva ruta.
*   **Reutilización**: Priorizar el uso de componentes Compose existentes, temas (`AppDynamicTheme`, `TemaStore`) y utilidades del proyecto.
*   **Genericidad**: Mantener el enfoque multiempresa; evitar hardcodear datos de negocios específicos.
*   **Modelos**: Los DTOs y modelos de dominio pueden continuar coexistiendo en `domain/model` mientras no exista una autorización explícita para realizar una refactorización formal.

## 4. Flujo Operativo Paso a Paso

### Fase 1: Análisis de Pre-implementación
Antes de escribir código, el agente DEBE investigar y reportar:
1.  **Funcionalidades Similares**: ¿Qué feature existente puede servir de modelo?
2.  **Patrón de Repository**: ¿Cómo está implementado el acceso a datos en la feature relacionada?
3.  **Contratos**: ¿Existen ya los DTOs o endpoints necesarios? (Apoyarse en `api-contract-maintenance`).

### Fase 2: Protocolo PREGUNTAR PRIMERO
Detenerse y presentar al usuario:
*   **Hallazgos**: Qué se puede reutilizar y qué capas se ven afectadas.
*   **Propuesta**: Estructura de la solución y lista de archivos afectados/nuevos.
*   **Riesgos**: Posibles conflictos con rutas existentes o dependencias Hilt.
**Esperar autorización explícita antes de modificar.**

### Fase 3: Ejecución Vertical (Cero Omisiones)
Al implementar, se deben determinar primero las capas realmente afectadas y trabajar sobre ellas siguiendo las dependencias existentes, sin implementar capas que no sean necesarias. Verificar según corresponda:
1.  **Capa de Datos**: Backend (Controllers/Data) y DTOs Moshi (@JsonClass).
2.  **Capa de Dominio/Acceso**: Repository (respetando el patrón de la feature) y configuración Hilt.
3.  **Capa de Lógica**: ViewModel gestionando estados (`Loading`, `Success`, `Error`) y eventos.
4.  **Capa de Presentación**: Screens/Composables en Compose y registro de rutas si la navegación independiente es requerida.

### Fase 4: Integración de Seguridad y Headers
*   Verificar que toda petición autenticada use `TokenStorage`.
*   Asegurar que se incluya `X-Platform: 3` mediante el interceptor.

## 5. Relación con otras Skills
*   **`api-contract-maintenance`**: Debe invocarse para validar y proteger la comunicación Android ↔ Backend.
*   **`debug-and-recovery`**: Debe invocarse si durante la validación aparecen comportamientos inesperados o errores técnicos.

## 6. Validación Final
1.  Confirmar compilación exitosa y ausencia de errores de Hilt.
2.  Verificar que el flujo de navegación (si aplica) sea correcto.
3.  Comprobar que los estados de UI (especialmente Error y Empty) funcionen.
4.  Asegurar que no se realizaron refactorizaciones no autorizadas en archivos no relacionados.
