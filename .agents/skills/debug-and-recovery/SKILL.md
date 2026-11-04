# Skill: debug-and-recovery
**Versión**: 1.0
**Contexto**: Formas Perfectas Android

## 1. Propósito
Proporcionar un procedimiento operativo sistemático para diagnosticar la causa raíz de fallos técnicos y proponer correcciones quirúrgicas que respeten la arquitectura MVVM y los contratos de datos del proyecto.

## 2. Activación de la Skill
### Cuándo utilizarla:
*   Al recibir un reporte de error, crash o comportamiento inesperado del usuario.
*   Ante errores de compilación o fallos en el grafo de dependencias de Hilt.
*   Cuando una petición API devuelve códigos de error (4xx, 5xx) o datos inconsistentes.
*   Si la UI no reacciona correctamente ante cambios de estado o navegación.

### Cuándo NO utilizarla:
*   Para implementar nuevas funcionalidades desde cero (usar `android-feature-flow`).
*   Para tareas de limpieza de código sin errores asociados.

## 3. Fase de Recopilación de Evidencia
Antes de formular una hipótesis, se DEBE recolectar:
1.  **Logs**: Consultar Logcat y las herramientas de diagnóstico disponibles en el entorno, filtrando por el `applicationId` o etiquetas relevantes (ej: `OkHttp`, `Hilt`, `LoginViewModel`).
2.  **Código**: Leer los archivos de la ruta crítica: `Screen` -> `ViewModel` -> `Repository` -> `ApiService`.
3.  **Estado de UI**: Si el error es visual, utilizar las herramientas de inspección de UI disponibles para verificar la jerarquía de vistas y el foco.

## 4. Procedimiento de Diagnóstico por Capas
El agente debe rastrear el error siguiendo el flujo inverso o directo según el síntoma:

### Capa 1: UI / Compose (Síntoma: Crash visual, la vista no actualiza)
*   Verificar si el Composable observa el `StateFlow` o `mutableStateOf` correcto.
*   Comprobar si hay efectos secundarios (`LaunchedEffect`) disparándose en bucle.
*   Revisar si el error ocurre al renderizar un estado específico (`Loading`, `Error`, `Empty`).

### Capa 2: ViewModel (Síntoma: Lógica de negocio errónea, estados bloqueados)
*   Inspeccionar el bloque `try-catch` dentro del `viewModelScope`.
*   Verificar si se está notificando el error al `UiState`.
*   Comprobar si las dependencias inyectadas en el constructor son las correctas.

### Capa 3: Repository / API (Síntoma: Datos nulos, errores de red)
*   **Contrato DTO vs JSON**: Comparar los campos del `JSON` del backend con las anotaciones `@Json(name = "...")` del DTO en `domain/model`.
*   **Headers**: Verificar en `AuthInterceptor.kt` que se esté enviando `X-Platform: 3`.
*   **Auth**: Si el error es `401`, revisar el flujo en `TokenAuthenticator.kt` y el estado de los tokens en `TokenStorage`.

### Capa 4: Backend / SQL Server (Síntoma: HTTP 500, error de persistencia)
*   Identificar el endpoint en `ApiService.kt`.
*   Determinar si el error proviene de la lógica del Controller ASP.NET o de un **Stored Procedure**.
*   **Importante**: Si el error es de base de datos, no intentar modificar el SQL sin autorización expresa.

## 5. Formulación de Hipótesis y Propuesta
Una vez identificada la capa, el agente debe:
1.  Explicar la **Causa Raíz** (ej: "El DTO espera un Int pero el backend envía un String").
2.  Diferenciar el **Síntoma** (ej: "La app se cierra al abrir Mis Citas").
3.  Proponer la solución siguiendo el principio de **Cambios Mínimos**.

## 6. Protocolo de Modificación (PREGUNTAR PRIMERO)
El proceso debe seguir estrictamente estas etapas:
*   **A. Diagnóstico sin cambios**: Investigación profunda y lectura de archivos sin modificar nada.
*   **B. Propuesta de solución**: Explicar al usuario qué se encontró y qué se planea corregir.
*   **C. Modificación autorizada**: Ejecutar la corrección solo después de recibir autorización explícita del usuario. Que un cambio sea pequeño, de sintaxis, imports o lógica simple NO constituye por sí mismo una excepción a PREGUNTAR PRIMERO.
*   **D. Validación posterior**: Verificar el resultado del cambio mediante las herramientas del entorno.

## 7. Validación Posterior
Tras aplicar la corrección, es obligatorio:
1.  Verificar que el código compile.
2.  Verificar mediante las herramientas de diagnóstico disponibles que el problema identificado haya sido corregido y, cuando corresponda, confirmar que las peticiones API involucradas funcionan correctamente.
3.  Asegurar que no se hayan introducido efectos secundarios en componentes relacionados (**Cero Omisiones**).
